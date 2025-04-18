package aagapp_backend.services.leaderboard;

import aagapp_backend.dto.GameLeaderboardResponseDTO;
import aagapp_backend.dto.LeaderboardResponseDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoomWinner;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueRoomWinner;
import aagapp_backend.entity.players.Player;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.ThemeRepository;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.league.LeagueRoomRepository;
import aagapp_backend.repository.league.LeagueRoomWinnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class LeaderBoardLeague {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private LeagueRoomRepository leagueRoomRepository;

    @Autowired
    private LeagueRoomWinnerRepository leagueRoomWinnerRepository;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    public GameLeaderboardResponseDTO getLeaderboard(Long leagueId, Pageable pageable) {
        // 1. Fetch the game details
        Optional<League> gameOpt = leagueRepository.findById(leagueId);
        if (gameOpt.isEmpty()) {
            throw new RuntimeException("Game not found with ID: " + leagueId);
        }
        League game = gameOpt.get();

        // 2. Fetch the theme associated with the game
        Optional<ThemeEntity> themeOpt = themeRepository.findById(game.getTheme().getId());
        if (themeOpt.isEmpty()) {
            throw new RuntimeException("Theme not found for game with ID: " + leagueId);
        }
        ThemeEntity theme = themeOpt.get();

        // 3. Fetch all winners (players with scores)
        Page<LeagueRoomWinner> winnersPage = leagueRoomWinnerRepository.findByLeagueRoomId(leagueId, pageable);
        List<LeagueRoomWinner> winners = winnersPage.getContent();

        // 4. Fetch total players in game rooms (sum of maxPlayers from GameRoom)
        long totalPlayers = leagueRoomRepository.sumMaxPlayersByLeagueId(leagueId);

        // 5. Prepare player list
        List<LeaderboardResponseDTO> playerList = new ArrayList<>();
        for (LeagueRoomWinner winner : winners) {
            Player player = winner.getPlayer();

            Optional<CustomCustomer> playerDetails = customCustomerRepository.findById(player.getPlayerId());
            if (playerDetails.isEmpty()) {
                throw new RuntimeException("Player details not found for player ID: " + player.getPlayerId());
            }

            LeaderboardResponseDTO playerDTO = new LeaderboardResponseDTO();
            playerDTO.setPlayerId(player.getPlayerId());
            playerDTO.setPlayerName(playerDetails.get().getName());
            playerDTO.setProfilePicture(playerDetails.get().getProfilePic());
            playerDTO.setScore(winner.getScore());

            playerList.add(playerDTO);
        }

        // 6. Return final wrapped DTO
        GameLeaderboardResponseDTO response = new GameLeaderboardResponseDTO();
        response.setGameName(game.getName());
        response.setGameFee(game.getFee());
        response.setGameIcon(game.getLeagueUrl());
        response.setThemeName(theme.getName());
        response.setTotalPlayers((int) totalPlayers);
        response.setPlayers(playerList);
        response.setTotalPages(winnersPage.getTotalPages());
        response.setTotalItems(winnersPage.getNumberOfElements());
        response.setCurrentPage(winnersPage.getNumber());

        return response;
    }
}
