package aagapp_backend.handler;

import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.gameservice.LudoGameService;
import aagapp_backend.services.gameservice.LudoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameWebSocketHandler {

    @Autowired
    private GameService gameService;

    @Autowired
    private LudoGameService ludogameservice;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/join")
    public void joinGame(Map<String, Object> data) {
        Long playerId = ((Number) data.get("playerId")).longValue();
        String username = (String) data.get("username");
        Long roomId = ((Number) data.get("roomId")).longValue();
        String gameType = (String) data.get("gameType");

        ResponseEntity<?> gameRoom = gameService.joinRoom(playerId, roomId, gameType);

        String joinMessage = username + " has joined the game.";

        messagingTemplate.convertAndSend("/topic/room/" + roomId, joinMessage);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, gameRoom);
    }
    

    @MessageMapping("/move")
    @SendTo("/topic/game")
    public Map<String, Object> processGameMove(Map<String, Object> moveData) {
        Long playerId = ((Number) moveData.get("playerId")).longValue();
        String gameType = (String) moveData.get("gameType");
        Object move = moveData.get("move"); // This could be a dice roll, move action, etc.

        // Example game state (this would typically come from a service or game state repository)
        Map<String, Object> gameState = new HashMap<>();

        // Handle game move based on game type
        switch (gameType) {
            case "LUDO":
                // Ludo game logic
                gameState.put("message", "Player " + playerId + " made a move in LUDO.");
                // Ludo-specific move handling
                processLudoMove(playerId, move, gameState);
                break;
            case "SNAKE_LADDER":
                // Snake and Ladder game logic
                gameState.put("message", "Player " + playerId + " made a move in SNAKE_LADDER.");
                // Snake and Ladder-specific move handling
                processSnakeLadderMove(playerId, move, gameState);
                break;
            default:
                gameState.put("message", "Unknown game type.");
                break;
        }

        return gameState;
    }

    private void processLudoMove(Long playerId, Object move, Map<String, Object> gameState) {
        // Implement the Ludo-specific move logic here
        // For example, you could update the player's position on the board
        // gameState.put("playerPosition", updatedPosition);
        // Add additional Ludo game logic as needed
    }

    private void processSnakeLadderMove(Long playerId, Object move, Map<String, Object> gameState) {
        // Implement the Snake & Ladder-specific move logic here
        // For example, you could update the player's position on the board
        // gameState.put("playerPosition", updatedPosition);
        // Add additional Snake & Ladder game logic as needed
    }




    @MessageMapping("/game/rollDice")
    public void rollDice(GameMessage gameMessage) {
        try {
            Long roomId = gameMessage.getRoomId();
            Long playerId = gameMessage.getPlayerId();

            if (!ludogameservice.isPlayerTurn(roomId, playerId)) {
                sendJsonResponse(roomId, "error", "It's not your turn!", null);
                return;
            }

            LudoResponse response = ludogameservice.rollDice(roomId, playerId);
            Long nextPlayerId = ludogameservice.getValidNextPlayerId(ludogameservice.getRoomById(roomId));

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

            if (!ludogameservice.isGameInProgress(roomId)) {
                sendJsonResponse(roomId, "error", "Game is not in progress or invalid room.", null);
                return;
            }

            if (ludogameservice.isGameCompleted(roomId)) {
                sendJsonResponse(roomId, "error", "Game is already completed.", null);
                return;
            }

            if (!ludogameservice.isPlayerInGame(roomId, playerId)) {
                sendJsonResponse(roomId, "error", "Player is not part of this game.", null);
                return;
            }

            String moveResponse = ludogameservice.moveToken(
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

            if (!ludogameservice.isGameInProgress(roomId)) {
                sendJsonResponse(roomId, "error", "Game is not in progress.", null);
                return;
            }

            boolean isPlayerRemoved = ludogameservice.markPlayerAsLoser(roomId, playerId);
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

//        String destination = isSpectator ? "/topic/spectators/" + roomId : "/topic/game/" + roomId;

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
