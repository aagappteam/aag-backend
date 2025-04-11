package aagapp_backend.controller.leaderboard;

import aagapp_backend.dto.GameWinnersResponseDTO;
import aagapp_backend.dto.PlayerResponseDTO;
import aagapp_backend.dto.RoomWinnersDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.players.Player;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.repository.game.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/leaderboard")
public class GameLeaderboard {

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping("/winners/{gameId}")
    public ResponseEntity<GameWinnersResponseDTO> getAllWinners(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "0") int page,  // Default page is 0
            @RequestParam(defaultValue = "10") int size) {  // Default size is 10
        // Fetch the Game based on gameId
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Game game = gameOpt.get();

        // Fetch paginated GameRooms associated with the specific Game
        Pageable pageable = PageRequest.of(page, size);
        Page<GameRoom> gameRoomsPage = gameRoomRepository.findByGame_Id(gameId, pageable);

        // List to store winners' information
        List<RoomWinnersDTO> allWinners = new ArrayList<>();

        // Iterate through each GameRoom to collect winners
        for (GameRoom gameRoom : gameRoomsPage.getContent()) {
            Set<Long> winnerIds = gameRoom.getWinners();
            List<PlayerResponseDTO> winners = new ArrayList<>();

            // Collect each winner's details
            for (Long winnerId : winnerIds) {
                CustomCustomer customCustomer = customCustomerRepository.findById(winnerId).orElse(null);

                Optional<Player> playerOpt = playerRepository.findById(winnerId);
                playerOpt.ifPresent(player -> {
                    // Assuming Player has a 'score' field
                    PlayerResponseDTO playerResponse = new PlayerResponseDTO(
                            player.getPlayerId(),
                            player.getName(),
                            player.getColor(), // Assuming this is the URL to the player's profile image
                            player.getScore()  // Fetching the score for each player
                    );
                    winners.add(playerResponse);
                });
            }

            // Add to the winners list
            RoomWinnersDTO roomWinnersDTO = new RoomWinnersDTO(
                    gameRoom.getRoomCode(),
                    gameRoom.getId(),
                    winners
            );
            allWinners.add(roomWinnersDTO);
        }

        // Prepare the response DTO
        GameWinnersResponseDTO responseDTO = new GameWinnersResponseDTO(
                game.getName(),
                game.getImageUrl(), // Assuming you have a logo URL in your Game entity
                allWinners,
                gameRoomsPage.getNumber(), // Current page number
                gameRoomsPage.getSize(),   // Page size
                gameRoomsPage.getTotalPages(), // Total number of pages
                gameRoomsPage.getTotalElements() // Total number of elements (rooms)
        );

        return ResponseEntity.ok(responseDTO);
    }
}
