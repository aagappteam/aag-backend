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
    private SimpMessagingTemplate messagingTemplate;



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
