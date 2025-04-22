package aagapp_backend.services.leaderboard;

import aagapp_backend.dto.GameLeaderboardResponseDTO;
import aagapp_backend.dto.LeaderboardResponseDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameResultRecord;
import aagapp_backend.entity.game.GameRoomWinner;
import aagapp_backend.entity.players.Player;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.*;
import com.amazonaws.services.kms.model.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.context.Theme;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class LeaderboardGame {



    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private GameRoomWinnerRepository gameRoomWinnerRepository;

    @Autowired
    private GameResultRecordRepository gameResultRecordRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;
    public GameLeaderboardResponseDTO getLeaderboard(Long gameId, Pageable pageable) {
        // 1. Fetch the game details
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }
        Game game = gameOpt.get();

        // 2. Fetch the theme associated with the game
        Optional<ThemeEntity> themeOpt = themeRepository.findById(game.getTheme().getId());
        if (themeOpt.isEmpty()) {
            throw new RuntimeException("Theme not found for game with ID: " + gameId);
        }
        ThemeEntity theme = themeOpt.get();

        // 3. Fetch paginated winner records from GameResultRecord where isWinner = true
        Page<GameResultRecord> winnersPage = gameResultRecordRepository
                .findByGame_IdAndIsWinnerTrue(gameId, pageable);

        // 4. Fetch total players in game rooms (sum of maxPlayers from GameRoom)
        long totalPlayers = gameRoomRepository.sumMaxPlayersByGameId(gameId);

        // 5. Prepare player list
        List<LeaderboardResponseDTO> playerList = new ArrayList<>();
        for (GameResultRecord result : winnersPage.getContent()) {
            Player player = result.getPlayer();

            Optional<CustomCustomer> playerDetails = customCustomerRepository.findById(player.getPlayerId());
            if (playerDetails.isEmpty()) {
                throw new RuntimeException("Player details not found for player ID: " + player.getPlayerId());
            }

            LeaderboardResponseDTO playerDTO = new LeaderboardResponseDTO();
            playerDTO.setPlayerId(player.getPlayerId());
            playerDTO.setPlayerName(playerDetails.get().getName());
            playerDTO.setProfilePicture(playerDetails.get().getProfilePic());
            playerDTO.setScore(result.getScore());
            playerDTO.setWinningammount(
                    result.getWinningammount() != null ? result.getWinningammount().stripTrailingZeros().doubleValue() : 0.0
            );

            playerList.add(playerDTO);
        }

        // 6. Return final wrapped DTO with pagination metadata
        GameLeaderboardResponseDTO response = new GameLeaderboardResponseDTO();
        response.setGameName(game.getName());
        response.setGameFee(game.getFee());
        response.setGameIcon(game.getImageUrl());
        response.setThemeName(theme.getName());
        response.setTotalPlayers((int) totalPlayers);
        response.setPlayers(playerList);
        response.setCurrentPage(winnersPage.getNumber());
        response.setTotalPages(winnersPage.getTotalPages());
        response.setTotalItems(winnersPage.getTotalElements());

        return response;
    }

/*
    public GameLeaderboardResponseDTO getLeaderboard(Long gameId, Pageable pageable) {
        // 1. Fetch the game details
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }
        Game game = gameOpt.get();

        // 2. Fetch the theme associated with the game
        Optional<ThemeEntity> themeOpt = themeRepository.findById(game.getTheme().getId());
        if (themeOpt.isEmpty()) {
            throw new RuntimeException("Theme not found for game with ID: " + gameId);
        }
        ThemeEntity theme = themeOpt.get();

        // 3. Fetch paginated winners (players with scores)
        Page<GameRoomWinner> winnersPage = gameRoomWinnerRepository.findByGame_Id(gameId, pageable);

        // 4. Fetch total players in game rooms (sum of maxPlayers from GameRoom)
        long totalPlayers = gameRoomRepository.sumMaxPlayersByGameId(gameId);

        // 5. Prepare player list
        List<LeaderboardResponseDTO> playerList = new ArrayList<>();
        for (GameRoomWinner winner : winnersPage.getContent()) {
            Player player = winner.getPlayer();

            Optional<CustomCustomer> playerDetails = customCustomerRepository.findById(player.getPlayerId());
            if (playerDetails.isEmpty()) {
                throw new RuntimeException("Player details not found for player ID: " + player.getPlayerId());
            }
            BigDecimal totalCollection = BigDecimal.valueOf(game.getFee()).multiply(BigDecimal.valueOf(winner.getGame().getMaxPlayersPerTeam()));
            BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(0.63));

            Double winningammount = winner.getGame().getFee()*0.63;
            LeaderboardResponseDTO playerDTO = new LeaderboardResponseDTO();
            playerDTO.setPlayerId(player.getPlayerId());
            playerDTO.setPlayerName(playerDetails.get().getName());
            playerDTO.setProfilePicture(playerDetails.get().getProfilePic());
            playerDTO.setScore(winner.getScore());
            playerDTO.setWinningammount(userWin.stripTrailingZeros().doubleValue());

            playerList.add(playerDTO);
        }

        // 6. Return final wrapped DTO with pagination metadata
        GameLeaderboardResponseDTO response = new GameLeaderboardResponseDTO();
        response.setGameName(game.getName());
        response.setGameFee(game.getFee());
        response.setGameIcon(game.getImageUrl());
        response.setThemeName(theme.getName());
        response.setTotalPlayers((int) totalPlayers);
        response.setPlayers(playerList);
        response.setCurrentPage(winnersPage.getNumber());
        response.setTotalPages(winnersPage.getTotalPages());
        response.setTotalItems(winnersPage.getTotalElements());

        return response;
    }
*/


}
