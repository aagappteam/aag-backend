package aagapp_backend.handler;

import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.services.gameservice.LudoGameService;
import aagapp_backend.services.gameservice.LudoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameWebSocketHandler {

    @Autowired
    private LudoGameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game/rollDice")
    public void rollDice(GameMessage gameMessage) {
        try {
            Long roomId = gameMessage.getRoomId();
            Long playerId = gameMessage.getPlayerId();

            if (!gameService.isPlayerTurn(roomId, playerId)) {
                sendJsonResponse(roomId, "error", "It's not your turn!", null);
                return;
            }

            LudoResponse response = gameService.rollDice(roomId, playerId);
            Long nextPlayerId = gameService.getValidNextPlayerId(gameService.getRoomById(roomId));

            Map<String, Object> data = new HashMap<>();
            data.put("playerId", playerId);
            data.put("rolledNumber", response.getDiceRoll());
            data.put("nextPlayerId", nextPlayerId);

            sendJsonResponse(roomId, "success", "Player " + playerId + " rolled a " + response.getDiceRoll(), data);
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(gameMessage.getRoomId(), "error", "Unexpected error: " + e.getMessage(), null);
        }
    }

    @MessageMapping("/game/moveToken")
    public void moveToken(GameMessage gameMessage) {
        try {
            Long roomId = gameMessage.getRoomId();
            Long playerId = gameMessage.getPlayerId();

            if (!gameService.isGameInProgress(roomId)) {
                sendJsonResponse(roomId, "error", "Game is not in progress or invalid room.", null);
                return;
            }

            if (gameService.isGameCompleted(roomId)) {
                sendJsonResponse(roomId, "error", "Game is already completed.", null);
                return;
            }

            if (!gameService.isPlayerInGame(roomId, playerId)) {
                sendJsonResponse(roomId, "error", "Player is not part of this game.", null);
                return;
            }

            String moveResponse = gameService.moveToken(
                    roomId,
                    gameMessage.getGameId(),
                    playerId,
                    gameMessage.getTokenId(),
                    gameMessage.getNewPosition()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("playerId", playerId);
            data.put("tokenId", gameMessage.getTokenId());
            data.put("newPosition", gameMessage.getNewPosition());

            sendJsonResponse(roomId, "success", moveResponse, data);
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(gameMessage.getRoomId(), "error", "An error occurred: " + e.getMessage(), null);
        }
    }

    @MessageMapping("/game/disconnect")
    public void handleDisconnect(GameMessage gameMessage) {
        try {
            Long playerId = gameMessage.getPlayerId();
            Long roomId = gameMessage.getRoomId();

            if (!gameService.isGameInProgress(roomId)) {
                sendJsonResponse(roomId, "error", "Game is not in progress.", null);
                return;
            }

            boolean isPlayerRemoved = gameService.markPlayerAsLoser(roomId, playerId);
            if (!isPlayerRemoved) {
                sendJsonResponse(roomId, "error", "Player not found in this game.", null);
                return;
            }

            sendJsonResponse(roomId, "info", "Player " + playerId + " has disconnected.", null);

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(gameMessage.getRoomId(), "error", "An error occurred: " + e.getMessage(), null);
        }
    }

    private void sendJsonResponse(Long roomId, String status, String message, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }

        messagingTemplate.convertAndSend("/topic/game/" + roomId, response);
    }

    public static class GameMessage {
        private String action;
        private Long gameId;
        private Long roomId;
        private Long playerId;
        private String tokenId;
        private int newPosition;

        // Getters and setters
        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Long getRoomId() {
            return roomId;
        }

        public void setGameId(Long gameId) {
            this.gameId = gameId;
        }

        public Long getGameId() {
            return gameId;
        }

        public void setRoomId(Long roomId) {
            this.roomId = roomId;
        }

        public Long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(Long playerId) {
            this.playerId = playerId;
        }

        public String getTokenId() {
            return tokenId;
        }

        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }

        public int getNewPosition() {
            return newPosition;
        }

        public void setNewPosition(int newPosition) {
            this.newPosition = newPosition;
        }
    }
}
