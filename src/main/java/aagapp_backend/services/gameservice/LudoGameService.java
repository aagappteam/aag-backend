/*
package aagapp_backend.services.gameservice;

import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoom;
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


}
*/
