package aagapp_backend.handler;

import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.gameservice.LudoGameService;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ServerEndpoint("/game/{gameId}")
public class GameWebSocketHandler {

    @Autowired
    private LudoGameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

   @MessageMapping("/game/rollDice")
   public void rollDice(GameMessage gameMessage) {
       try {
           int diceValue = gameService.rollDice();
           String response = "Player " + gameMessage.getPlayerId() + " rolled a " + diceValue;

           // Send the response to the STOMP topic for the game
           messagingTemplate.convertAndSend("/topic/game/" + gameMessage.getRoomId(), response);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

    // Handle a message with a "MOVE_TOKEN" action
    @MessageMapping("/game/moveToken")
    public void moveToken(GameMessage gameMessage) {
        try {
            String response = gameService.moveToken(
                    gameMessage.getRoomId(),
                    gameMessage.getGameId(),
                    gameMessage.getPlayerId(),
                    gameMessage.getTokenId(),
                    gameMessage.getNewPosition()
            );

            // Send the response to the STOMP topic for the game
            messagingTemplate.convertAndSend("/topic/game/" + gameMessage.getRoomId(), response);
        } catch (Exception e) {
            e.printStackTrace();
        }
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