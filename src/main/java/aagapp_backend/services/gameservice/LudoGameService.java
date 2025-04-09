package aagapp_backend.services.gameservice;

import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.game.Token;
import aagapp_backend.entity.players.Player;

import aagapp_backend.enums.GameRoomStatus;
import aagapp_backend.enums.PlayerStatus;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.repository.game.PlayerRepository;
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
    private PlayerRepository  playerRepository;

    @Autowired
    private GameRoomRepository gameRoomRepository;



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


    private void updatePlayerScore(Long playerId, int points) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setScore(player.getScore() + points);
            playerRepository.save(player);
        }
    }

    public Map<Long, Integer> getPlayerScores() {
        List<Player> players = playerRepository.findAll();
        Map<Long, Integer> scores = new HashMap<>();
        for (Player player : players) {
            scores.put(player.getPlayerId(), player.getScore());
        }
        return scores;
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



}
