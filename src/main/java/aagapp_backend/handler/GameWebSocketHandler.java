package aagapp_backend.handler;

import aagapp_backend.services.gameservice.GameService;
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

    private static ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

   /* @OnOpen
    public void onOpen(Session session, @PathParam("gameId") String gameId) {
        sessions.put(session.getId(), session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            GameMessage gameMessage = mapper.readValue(message, GameMessage.class);

            String response;
            if ("ROLL_DICE".equals(gameMessage.getAction())) {
                int diceValue = gameService.rollDice();
                response = "Player " + gameMessage.getPlayerId() + " rolled a " + diceValue;
            } else if ("MOVE_TOKEN".equals(gameMessage.getAction())) {
                response = gameService.moveToken(
                        gameMessage.getGameId(),
                        gameMessage.getPlayerId(),
                        gameMessage.getTokenId(),
                        gameMessage.getNewPosition()
                );
            } else {
                response = "Invalid action!";
            }

            // After performing the action, send real-time updates via STOMP
            messagingTemplate.convertAndSend("/topic/game/" + gameMessage.getGameId(), response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
    }*/
   @MessageMapping("/game/rollDice")
   public void rollDice(GameMessage gameMessage) {
       try {
           int diceValue = gameService.rollDice();
           String response = "Player " + gameMessage.getPlayerId() + " rolled a " + diceValue;

           // Send the response to the STOMP topic for the game
           messagingTemplate.convertAndSend("/topic/game/" + gameMessage.getGameId(), response);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

    // Handle a message with a "MOVE_TOKEN" action
    @MessageMapping("/game/moveToken")
    public void moveToken(GameMessage gameMessage) {
        try {
            String response = gameService.moveToken(
                    gameMessage.getGameId(),
                    gameMessage.getPlayerId(),
                    gameMessage.getTokenId(),
                    gameMessage.getNewPosition()
            );

            // Send the response to the STOMP topic for the game
            messagingTemplate.convertAndSend("/topic/game/" + gameMessage.getGameId(), response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    private void broadcast(String message) {
        sessions.values().forEach(session -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }*/

    public static class GameMessage {
        private String action;
        private Long gameId;
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

        public Long getGameId() {
            return gameId;
        }

        public void setGameId(Long gameId) {
            this.gameId = gameId;
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