package aagapp_backend.services.gameservice;

import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.game.Token;
import aagapp_backend.entity.players.Player;

import aagapp_backend.enums.GameRoomStatus;
import aagapp_backend.enums.PlayerStatus;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.services.ludo.PCGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LudoGameService {

    // Use ConcurrentHashMap for thread-safe operations
    private ConcurrentHashMap<Long, GameState> gameStateMap = new ConcurrentHashMap<>();
    List<Integer> board = new ArrayList<>(52); // Represents the board

    private static final String INVALID_TOKEN = "Invalid token!";
    private static final String NOT_YOUR_TURN = "It's not your turn!";
    private static final String INVALID_MOVE = "Invalid move. Token cannot move beyond the board.";

    @Autowired
    private PCGService pcgService;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    public LudoResponse rollDice(Long roomId, Long playerId) {
        GameRoom gameRoom = getRoomById(roomId);
        Long nextPlayerId = getNextPlayerId(gameRoom);

        int diceRoll = pcgService.nextInt();
        if (diceRoll == 0) {
            return new LudoResponse(false, "Dice rolled a 0, skipping your turn!", diceRoll, playerId, nextPlayerId);
        }
        return new LudoResponse(true, "Dice rolled successfully!", diceRoll, playerId,nextPlayerId);
    }
    public Long getNextPlayerId(GameRoom gameRoom) {
        List<Player> activePlayers = gameRoom.getCurrentPlayers().stream()
                .filter(p -> !gameRoom.getWinners().contains(p.getPlayerId()))
                .collect(Collectors.toList());

        if (activePlayers.isEmpty()) return null;

        Long currentPlayerId = gameRoom.getCurrentPlayerId();
        int currentIndex = activePlayers.indexOf(currentPlayerId);
        return activePlayers.get((currentIndex + 1) % activePlayers.size()).getPlayerId();
    }

    public boolean markPlayerAsLoser(Long roomId, Long playerId) {
        GameRoom gameRoom = getRoomById(roomId);
        if (gameRoom == null) {
            return false;
        }

        Player player = gameRoom.getPlayerById(playerId);
        if (player != null) {
            player.setPlayerStatus(PlayerStatus.LOST);
            saveGameState(gameRoom);
            return true;
        }

        return false;
    }

    public void saveGameState(GameRoom gameRoom) {
        gameRoom.setActivePlayersCount(gameRoom.getCurrentPlayers().size());

        if (gameRoom.getStatus() == GameRoomStatus.ONGOING) {
            // Check if the game is over and change the status
            if (gameRoom.getActivePlayersCount() <= 1) {
                gameRoom.setStatus(GameRoomStatus.COMPLETED);
                // Handle the winner, score, etc.
            }
        }


        gameRoomRepository.save(gameRoom);

    }


/*    public Map<String, Object> getGameState(Long roomId) {
        GameState gameState = gameStateMap.get(roomId);
        if (gameState == null) {
            return Collections.emptyMap();  // Game not found
        }

        Map<String, Object> data = new HashMap<>();
        data.put("players", gameState.getPlayers());
        data.put("board", gameState.getBoard());
        // Add any other relevant game data you want to share with spectators
        return data;
    }*/


    public boolean isValidPlayer(Long roomId, Long playerId) {
        GameRoom gameRoom = getRoomById(roomId);
        if (gameRoom == null) {
            return false;
        }

        return gameRoom.getCurrentPlayers().stream()
                .anyMatch(player -> player.getPlayerId().equals(playerId));
    }

    public Long getValidNextPlayerId(GameRoom gameRoom) {
        Long nextPlayerId = getNextPlayerId(gameRoom);
        return (nextPlayerId != null && isValidPlayer(gameRoom.getId(), nextPlayerId))
                ? nextPlayerId
                : null;
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

        synchronized (gameState) {
            int currentPosition = token.getPosition();
            int newPosition = currentPosition + diceRoll;

            // Validate move
            if (newPosition > gameState.getBoardSize()) {
                return "Invalid move. Token cannot move beyond the board.";
            }
            if (currentPosition == 0 && diceRoll != 6) {
                return "You need a 6 to move this token out of the starting position.";
            }

            // Check for cutting logic (if not in a safe zone)
            boolean extraTurn = false;
            if (!isSafeZone(newPosition)) {
                Optional<Token> occupyingToken = findTokenAtPosition(gameRoom, newPosition);
                if (occupyingToken.isPresent()) {
                    Token opponentToken = occupyingToken.get();
                    if (!opponentToken.getPlayerId().equals(playerId)) {
                        sendTokenHome(opponentToken);
                        extraTurn = true; // Player gets an extra turn for cutting an opponent
                    }
                }
            }

            // Move the token
            token.setPosition(newPosition);

            // Check if the token reaches home
            if (newPosition == gameState.getFinalHomePosition(playerId)) {
                gameState.markTokenHome(token);
                if (gameState.hasPlayerWon(playerId)) {
                    gameState.addWinner(playerId);
                    return "Player " + playerId + " has won the game!";
                }
            }

            // Check if the game is complete
            if (checkGameCompletion(gameState, gameRoom)) {
                gameState.setGameStatus(GameRoomStatus.COMPLETED);
                return "Game Over. Player " + playerId + " has won the game!";
            }

            // Add score based on the dice roll value
           /* Player currentPlayer = gameState.getCurrentPlayerId();
            if (currentPlayer != null) {
                currentPlayer.setScore(diceRoll); // Add points based on dice roll
            }*/

            // Grant another turn if rolled a 6 or cut an opponent's token
            if (diceRoll == 6 || extraTurn) {
                return "Token moved to position " + token.getPosition() + ". You get another turn!";
            }

            // Move to the next player
            gameState.setCurrentPlayerId(gameState.getNextPlayerId());
        }

        return "Token moved to position " + token.getPosition() + " successfully.";
    }





    private Optional<Token> findTokenAtPosition(GameRoom gameRoom, int position) {
        return gameRoom.getCurrentPlayers().stream()
                .flatMap(player -> player.getTokens().stream())
                .filter(token -> token.getPosition() == position)
                .findFirst();
    }

    private void sendTokenHome(Token token) {
        token.setPosition(0);
        token.setHome(false);
    }

    private void updateNextPlayer(GameRoom gameRoom) {
        List<Player> players = gameRoom.getCurrentPlayers();
        int currentIndex = players.indexOf(gameRoom.getCurrentPlayerId());
        gameRoom.setCurrentPlayerId(players.get((currentIndex + 1) % players.size()).getPlayerId());
    }



    // Safe zones
    private boolean isSafeZone(int position) {
        List<Integer> safeZones = Arrays.asList(8, 13, 21, 26, 34, 39, 47, 52); // Example safe zones

        return safeZones.contains(position);
    }


    public boolean isPlayerInGame(Long roomId, Long playerId) {
        GameRoom gameRoom = this.getRoomById(roomId);

        return gameRoom != null &&
                gameRoom.getCurrentPlayers().stream()
                        .anyMatch(player -> player.getPlayerId().equals(playerId));
    }
    public boolean isGameInProgress(Long roomId) {
        GameRoom gameRoom = this.getRoomById(roomId);
        return gameRoom!= null && gameRoom.getStatus().equals(GameRoomStatus.ONGOING);
    }


    public boolean isGameCompleted(Long roomId) {
        GameRoom gameRoom = this.getRoomById(roomId);
        return gameRoom!= null && gameRoom.getStatus().equals(GameRoomStatus.COMPLETED);
    }

    public boolean isPlayerTurn(Long roomId, Long playerId) {
        GameRoom gameRoom = this.getRoomById(roomId);
        return gameRoom != null && gameRoom.getCurrentPlayerId().equals(playerId);
    }

}
