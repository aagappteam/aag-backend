package aagapp_backend.services.gameservice;

import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.enums.GameRoomStatus;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.services.ludo.PCGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

@Service
public class LudoGameService {

    // Use ConcurrentHashMap for thread-safe operations
    private ConcurrentHashMap<Long, GameState> gameStateMap = new ConcurrentHashMap<>();

    private static final String INVALID_TOKEN = "Invalid token!";
    private static final String NOT_YOUR_TURN = "It's not your turn!";
    private static final String INVALID_MOVE = "Invalid move. Token cannot move beyond the board.";

    @Autowired
    private PCGService pcgService;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    // Roll the dice
    public int rollDice() {
        return pcgService.nextInt();
    }

    // Fetch GameRoom by ID
    public GameRoom getRoomById(Long id) {
        return gameRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GameRoom not found for id: " + id));
    }

    // Check if the game is complete
    private boolean checkGameCompletion(GameState gameState, GameRoom gameRoom) {
        int winnersCount = gameState.getWinners().size();
        int maxPlayers = gameRoom.getMaxPlayers();
        return winnersCount >= maxPlayers - 1; // Game is complete if all but one player has won
    }

    // Move a token
    public String moveToken(Long roomId, Long gameId, Long playerId, String tokenId, int diceRoll) {
        GameState gameState = gameStateMap.get(roomId);
        if (gameState == null) {
            return "Game not found.";
        }

        GameRoom gameRoom = getRoomById(roomId);
        if (gameRoom == null) {
            return "Invalid room.";
        }

        // Ensure it's the player's turn
        if (!gameState.getCurrentPlayerId().equals(playerId)) {
            return "It's not your turn!";
        }

        // Get and validate the player's token
        Token token = gameState.getPlayerToken(playerId, tokenId);
        if (token == null) {
            return "Invalid token!";
        }

        int currentPosition = token.getPosition();
        int newPosition = currentPosition + diceRoll;

        // Validate move
        if (newPosition > gameState.getBoardSize()) {
            return "Invalid move. Token cannot move beyond the board.";
        }
        if (currentPosition == 0 && diceRoll != 6) {
            return "You need a 6 to move this token out of the starting position.";
        }

        synchronized (gameState){
            // Handle collisions and interactions with other tokens
            Token occupyingToken = gameState.getTokenAtPosition(newPosition);
            if (occupyingToken != null) {
                // Handle own token collision
                if (occupyingToken.getPlayerId().equals(playerId)) {
                    return "You cannot move to a position already occupied by your own token.";
                }

                // Handle opponent token collision (send home if not a safe zone)
                if (!isSafeZone(newPosition)) {
                    gameState.sendTokenHome(occupyingToken);
                }
            }

            // Move the token
            token.setPosition(newPosition);

            // Check if the token reaches the home position
            if (newPosition == gameState.getFinalHomePosition(playerId)) {
                gameState.markTokenHome(token);
                if (gameState.hasPlayerWon(playerId)) {
                    gameState.addWinner(playerId);
                    return "Player " + playerId + " has won the game!";
                }
            }

            // Check if the game is complete (all players have won)
            if (checkGameCompletion(gameState, gameRoom)) {
                gameState.setGameStatus(GameRoomStatus.COMPLETED);
                return "Game Over. Player " + playerId + " has won the game!";
            }

            // Update to the next player's turn
            gameState.setCurrentPlayerId(gameState.getNextPlayerId());
        }

        return "Token moved to position " + newPosition + " successfully.";
    }


    // Safe zones
    private boolean isSafeZone(int position) {
        List<Integer> safeZones = Arrays.asList(8, 13, 21, 26); // Example positions
        return safeZones.contains(position);
    }

}
