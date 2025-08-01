package aagapp_backend.services.leaderboard;

import aagapp_backend.dto.game.GameLeaderboardResponseDTOADMIN;
import aagapp_backend.dto.game.LeaderboardResponseDTOAdmin;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameResultRecord;
import aagapp_backend.entity.players.Player;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.*;
import aagapp_backend.services.gameservice.GameService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class LeaderboardGame {


    @Autowired
    private GameService gameService;
    @Autowired
    private GameRoomRepository gameRoomRepository;


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
    public GameLeaderboardResponseDTOADMIN getLeaderboard(Long gameId, String type, Pageable pageable) {
        // 1. Fetch game details
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        // 2. Fetch theme details
        ThemeEntity theme = themeRepository.findById(game.getTheme().getId())
                .orElseThrow(() -> new RuntimeException("Theme not found for game ID: " + gameId));

        // 3. Filter type: winner / loser / all
        Boolean isWinner = null;
        if ("winners".equalsIgnoreCase(type)) isWinner = true;
        else if ("losers".equalsIgnoreCase(type)) isWinner = false;

        // 4. Fetch all GameResultRecords (filtered)
        List<GameResultRecord> results = (isWinner == null)
                ? gameResultRecordRepository.findByGame_Id(gameId)
                : gameResultRecordRepository.findByGame_IdAndIsWinner(gameId, isWinner);

        // 5. Aggregate by playerId
        Map<Long, LeaderboardResponseDTOAdmin> playerMap = new LinkedHashMap<>();

        for (GameResultRecord result : results) {
            Player player = result.getPlayer();
            Long playerId = player.getPlayerId();

            LeaderboardResponseDTOAdmin dto = playerMap.get(playerId);
            if (dto == null) {
                // Fetch customer
                Optional<CustomCustomer> customerOpt = customCustomerRepository.findById(playerId);
                if (customerOpt.isEmpty()) continue;
                CustomCustomer customer = customerOpt.get();

                dto = new LeaderboardResponseDTOAdmin();
                dto.setPlayerId(playerId);
                dto.setPlayerName(customer.getName());
                dto.setMobileNumber(customer.getMobileNumber());
                dto.setState(customer.getState());
                dto.setProfilePicture(customer.getProfilePic());
                dto.setScore(result.getScore() != null ? result.getScore() : 0);
                dto.setTotalPrizePool(result.getWinningammount() != null ? result.getWinningammount().doubleValue() : 0.0);
                dto.setGamesPlayed(1);
                dto.setWinningAmmount(result.getWinningammount() != null ? result.getWinningammount().doubleValue() : 0.0);
                playerMap.put(playerId, dto);
            } else {
                int newScore = result.getScore() != null ? result.getScore().intValue() : 0;
                dto.setScore(dto.getScore() + newScore);
                double winAmt = result.getWinningammount() != null ? result.getWinningammount().doubleValue() : 0.0;
                dto.setGamesPlayed(dto.getGamesPlayed() + 1);
//                dto.setScore(dto.getScore() + newScore.intValue());
                dto.setTotalPrizePool(dto.getTotalPrizePool() + winAmt);
                dto.setWinningAmmount(dto.getWinningAmmount() + winAmt);

            }
        }

        // 6. Convert to list & sort by score descending
        List<LeaderboardResponseDTOAdmin> leaderboard = new ArrayList<>(playerMap.values());
        leaderboard.sort((a, b) -> b.getScore().compareTo(a.getScore()));

        // 7. Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), leaderboard.size());
        List<LeaderboardResponseDTOAdmin> pagedList = leaderboard.subList(start, end);

        // 8. Get total players in all rooms
        long totalPlayers = game.getMaxPlayersPerTeam();

        // 9. Vendor name (only user_name)
        String vendorName = game.getVendorEntity() != null && game.getVendorEntity().getFirst_name() != null
                ? game.getVendorEntity().getFirst_name()
                : "Aagveer";

        // 10. Final response DTO
        GameLeaderboardResponseDTOADMIN response = new GameLeaderboardResponseDTOADMIN();
        response.setGameName(game.getName());
        response.setGameFee(game.getFee());
        response.setGameIcon(game.getImageUrl());
        response.setThemeName(theme.getName());

        response.setTotalPlayers((int) totalPlayers);

        response.setTotalPrizePool(gameService.calculateTotalPrizeNewdouble(game));
        response.setVendorname(vendorName);
        response.setPlayers(pagedList);
        response.setCurrentPage(pageable.getPageNumber());
        response.setTotalPages((int) Math.ceil((double) leaderboard.size() / pageable.getPageSize()));
        response.setTotalItems(leaderboard.size());

        return response;
    }



    public GameLeaderboardResponseDTOADMIN getLeaderboardParticpants(Long gameId,  Pageable pageable) {
        // 1. Fetch game details
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        // 2. Fetch theme details
        ThemeEntity theme = themeRepository.findById(game.getTheme().getId())
                .orElseThrow(() -> new RuntimeException("Theme not found for game ID: " + gameId));



        // 4. Fetch all GameResultRecords (filtered)
        List<GameResultRecord> results = gameResultRecordRepository.findByGame_Id(gameId);

        // 5. Aggregate by playerId
        Map<Long, LeaderboardResponseDTOAdmin> playerMap = new LinkedHashMap<>();

        for (GameResultRecord result : results) {
            Player player = result.getPlayer();
            Long playerId = player.getPlayerId();

            LeaderboardResponseDTOAdmin dto = playerMap.get(playerId);
            if (dto == null) {
                // Fetch customer
                Optional<CustomCustomer> customerOpt = customCustomerRepository.findById(playerId);
                if (customerOpt.isEmpty()) continue;
                CustomCustomer customer = customerOpt.get();

                dto = new LeaderboardResponseDTOAdmin();
                dto.setPlayerId(playerId);
                dto.setPlayerName(customer.getName());
                dto.setMobileNumber(customer.getMobileNumber());
                dto.setState(customer.getState());
                dto.setProfilePicture(customer.getProfilePic());
                dto.setScore(result.getScore() != null ? result.getScore() : 0);
                dto.setTotalPrizePool(result.getWinningammount() != null ? result.getWinningammount().doubleValue() : 0.0);
                dto.setGamesPlayed(1);
                dto.setWinningAmmount(result.getWinningammount() != null ? result.getWinningammount().doubleValue() : 0.0);
                playerMap.put(playerId, dto);
            } else {
                int newScore = result.getScore() != null ? result.getScore().intValue() : 0;
                dto.setScore(dto.getScore() + newScore);
                double winAmt = result.getWinningammount() != null ? result.getWinningammount().doubleValue() : 0.0;
                dto.setGamesPlayed(dto.getGamesPlayed() + 1);
//                dto.setScore(dto.getScore() + newScore.intValue());
                dto.setTotalPrizePool(dto.getTotalPrizePool() + winAmt);
                dto.setWinningAmmount(dto.getWinningAmmount() + winAmt);

            }
        }

        // 6. Convert to list & sort by score descending
        List<LeaderboardResponseDTOAdmin> leaderboard = new ArrayList<>(playerMap.values());
        leaderboard.sort((a, b) -> b.getScore().compareTo(a.getScore()));

        // 7. Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), leaderboard.size());
        List<LeaderboardResponseDTOAdmin> pagedList = leaderboard.subList(start, end);

        // 8. Get total players in all rooms
        long totalPlayers = Optional.ofNullable(
                gameRoomRepository.sumMaxPlayersByGameIdWithCompletedStatusAndPassword(gameId)
        ).orElse(0L);

        // 9. Vendor name (only user_name)
        String vendorName = game.getVendorEntity() != null && game.getVendorEntity().getFirst_name() != null
                ? game.getVendorEntity().getFirst_name()
                : "Aagveer";

        // 10. Final response DTO
        GameLeaderboardResponseDTOADMIN response = new GameLeaderboardResponseDTOADMIN();
        response.setGameName(game.getName());
        response.setGameFee(game.getFee());
        response.setGameIcon(game.getImageUrl());
        response.setThemeName(theme.getName());

        response.setTotalPlayers((int) totalPlayers);

        response.setTotalPrizePool(gameService.calculateTotalPrizeNewdouble(game));
        response.setVendorname(vendorName);
        response.setPlayers(pagedList);
        response.setCurrentPage(pageable.getPageNumber());
        response.setTotalPages((int) Math.ceil((double) leaderboard.size() / pageable.getPageSize()));
        response.setTotalItems(leaderboard.size());

        return response;
    }

/*
    public GameLeaderboardResponseDTOADMIN getLeaderboard(Long gameId, String type, Pageable pageable) {
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
        List<LeaderboardResponseDTOAdmin> playerList = new ArrayList<>();
        for (GameResultRecord result : winnersPage.getContent()) {
            Player player = result.getPlayer();

            Optional<CustomCustomer> playerDetails = customCustomerRepository.findById(player.getPlayerId());
            if (playerDetails.isEmpty()) {
                throw new RuntimeException("Player details not found for player ID: " + player.getPlayerId());
            }

            LeaderboardResponseDTOAdmin playerDTO = new LeaderboardResponseDTOAdmin();
            playerDTO.setPlayerId(player.getPlayerId());
            playerDTO.setPlayerName(playerDetails.get().getName());
            playerDTO.setMobileNumber(playerDetails.get().getMobileNumber());
            playerDTO.setState(playerDetails.get().getState());
            playerDTO.setPlayerName(playerDetails.get().getName());
            playerDTO.setProfilePicture(playerDetails.get().getProfilePic());
            playerDTO.setScore(result.getScore());
            playerDTO.setWinningammount(
                    result.getWinningammount() != null ? result.getWinningammount().stripTrailingZeros().doubleValue() : 0.0
            );

            playerList.add(playerDTO);
        }

        // 6. Return final wrapped DTO with pagination metadata
        GameLeaderboardResponseDTOADMIN response = new GameLeaderboardResponseDTOADMIN();
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
