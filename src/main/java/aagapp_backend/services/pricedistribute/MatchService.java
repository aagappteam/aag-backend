package aagapp_backend.services.pricedistribute;
import aagapp_backend.dto.GameResult;
import aagapp_backend.dto.LeaderboardDto;
import aagapp_backend.dto.PlayerDto;
import aagapp_backend.dto.PlayerDtoWinner;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameResultRecord;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.game.GameRoomWinner;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.wallet.VendorWallet;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.GameRoomStatus;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.*;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.league.LeagueRoomRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.gameservice.GameService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private static final double TAX_PERCENT = 0.28;
    private static final double VENDOR_PERCENT = 0.05;
    private static final double PLATFORM_PERCENT = 0.04;
    private static final double USER_WIN_PERCENT = 0.63;
    private static final double BONUS_PERCENT = 0.20;

    private GameRoomWinnerRepository gameRoomWinnerRepository;
    private GameResultRecordRepository gameResultRecordRepository;
    private LeagueRoomRepository leagueRoomRepository;
    private TournamentRepository tournamentRepository;
    private LeagueRepository leagueRepository;
    private TournamentRoomRepository tournamentRoomRepository;
    private CustomCustomerRepository customCustomerRepository;
    private PlayerRepository playerRepository;
    private GameService gameService;
    private VendorRepository vendorRepo;
    private GameRoomRepository gameRoomRepository;
    private GameRepository gameRepository;
    private WalletRepository walletRepo;

    @Autowired
    public void setGameRoomWinnerRepository(GameRoomWinnerRepository repo) {
        this.gameRoomWinnerRepository = repo;
    }

    @Autowired
    public void setGameResultRecordRepository(GameResultRecordRepository repo) {
        this.gameResultRecordRepository = repo;
    }

    @Autowired
    public void setLeagueRoomRepository(LeagueRoomRepository repo) {
        this.leagueRoomRepository = repo;
    }

    @Autowired
    public void setTournamentRepository(TournamentRepository repo) {
        this.tournamentRepository = repo;
    }

    @Autowired
    public void setLeagueRepository(LeagueRepository repo) {
        this.leagueRepository = repo;
    }

    @Autowired
    public void setTournamentRoomRepository(TournamentRoomRepository repo) {
        this.tournamentRoomRepository = repo;
    }

    @Autowired
    public void setCustomCustomerRepository(CustomCustomerRepository repo) {
        this.customCustomerRepository = repo;
    }

    @Autowired
    public void setPlayerRepository(PlayerRepository repo) {
        this.playerRepository = repo;
    }

    @Autowired
    @Lazy
    public void setGameService(GameService service) {
        this.gameService = service;
    }

    @Autowired
    public void setVendorRepo(VendorRepository repo) {
        this.vendorRepo = repo;
    }

    @Autowired
    public void setGameRoomRepository(GameRoomRepository repo) {
        this.gameRoomRepository = repo;
    }

    @Autowired
    public void setGameRepository(GameRepository repo) {
        this.gameRepository = repo;
    }

    @Autowired
    public void setWalletRepo(WalletRepository repo) {
        this.walletRepo = repo;
    }

/*
    public List<PlayerDto> processMatch(GameResult gameResult) {
        // Fetch the GameRoom based on roomId
        Optional<GameRoom> gameRoomOpt = gameRoomRepository.findById(gameResult.getRoomId());
        if (gameRoomOpt.isEmpty()) {
            throw new RuntimeException("Game room not found with ID: " + gameResult.getRoomId());
        }
        GameRoom gameRoom = gameRoomOpt.get();
        // Fetch the Game associated with the gameId
        Optional<Game> gameOpt = gameRepository.findById(gameRoom.getGame().getId());
        if (gameOpt.isEmpty()) {
            throw new RuntimeException("Game not found with ID: " + gameRoom.getGame().getId());
        }
        Game game = gameOpt.get();

        // Calculate the total collection and shares
        BigDecimal totalCollection = BigDecimal.valueOf(game.getFee()).multiply(BigDecimal.valueOf(gameResult.getPlayers().size()));
        BigDecimal tax = totalCollection.multiply(BigDecimal.valueOf(TAX_PERCENT));
        BigDecimal vendorShare = totalCollection.multiply(BigDecimal.valueOf(VENDOR_PERCENT));
        BigDecimal platformShare = totalCollection.multiply(BigDecimal.valueOf(PLATFORM_PERCENT));
        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(USER_WIN_PERCENT));
//        BigDecimal bonus = BigDecimal.valueOf(game.getFee()).multiply(BigDecimal.valueOf(BONUS_PERCENT));
        BigDecimal finalAmountToUser = userWin;

        // Process the players to find the winner
        PlayerDtoWinner winner = determineWinner(gameResult.getPlayers()); // Get the winner

        // Identify the loser
        PlayerDtoWinner loser = gameResult.getPlayers().stream()
                .filter(p -> !p.getPlayerId().equals(winner.getPlayerId()))
                .findFirst().orElseThrow(() -> new RuntimeException("No loser found"));

        // Update winner's wallet with the prize amount
        Wallet wallet = walletRepo.findByCustomCustomer_Id(winner.getPlayerId());
        if (wallet == null) {
            throw new RuntimeException("Wallet not found for user ID: " + winner.getPlayerId());
        }

        gameService.leaveRoom(winner.getPlayerId(), game.getId());
        gameService.leaveRoom(loser.getPlayerId(), game.getId());


        // Add the prize to the winner's wallet
        BigDecimal updatedWinning = wallet.getWinningAmount().add(finalAmountToUser);
        wallet.setWinningAmount(updatedWinning);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepo.save(wallet);

        // Add vendor share and update total balance
        this.addToVendorWalletAndTotalBalance(game.getVendorEntity().getService_provider_id(), vendorShare);

        // Fetch Player entity using playerId
        Optional<Player> winnerPlayerOpt = playerRepository.findById(winner.getPlayerId());
        if (winnerPlayerOpt.isEmpty()) {
            throw new RuntimeException("Player not found with ID: " + winner.getPlayerId());
        }
        Player winnerPlayer = winnerPlayerOpt.get();

        GameRoomWinner gameRoomWinner = new GameRoomWinner();
        gameRoomWinner.setGame(game);  // Set the game
        gameRoomWinner.setPlayer(winnerPlayer);  // Set the player (winner)
        gameRoomWinner.setScore(winner.getScore());  // Set the score
        gameRoomWinner.setWinTimestamp(LocalDateTime.now());  // Set the win timestamp

        // Save the winner record into the database
        gameRoomWinnerRepository.save(gameRoomWinner);

        GameResultRecord winnerRecord = new GameResultRecord();
        winnerRecord.setRoomId(gameRoom.getId());
        winnerRecord.setGame(game);
        winnerRecord.setPlayer(winnerPlayer); // already fetched
        winnerRecord.setScore(winner.getScore());
        winnerRecord.setIsWinner(true);
        winnerRecord.setPlayedAt(LocalDateTime.now());

        Player loserPlayer = playerRepository.findById(loser.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        GameResultRecord loserRecord = new GameResultRecord();
        loserRecord.setRoomId(gameRoom.getId());
        loserRecord.setGame(game);
        loserRecord.setPlayer(loserPlayer);
        loserRecord.setScore(loser.getScore());
        loserRecord.setIsWinner(false);
        loserRecord.setPlayedAt(LocalDateTime.now());

        gameResultRecordRepository.saveAll(List.of(winnerRecord, loserRecord));


*/
/*        gameRoom.setStatus(GameRoomStatus.COMPLETED);
        gameRoomRepository.save(gameRoom);*//*


        // Optional: Update AAG Wallet if needed
        // updateAAGWallet(platformShare, tax);

        // Return all players' details (including updated amount for the winner)
        return getAllPlayersDetails(gameResult, gameRoomOpt, game, winner, loser);
    }
*/

    /*public void processMatch(GameResult gameResult) {
        // Fetch GameRoom
        Optional<GameRoom> gameRoomOpt = gameRoomRepository.findById(gameResult.getRoomId());
        if (gameRoomOpt.isEmpty()) {
            throw new RuntimeException("Game room not found with ID: " + gameResult.getRoomId());
        }
        GameRoom gameRoom = gameRoomOpt.get();

        // Fetch Game
        Optional<Game> gameOpt = gameRepository.findById(gameRoom.getGame().getId());
        if (gameOpt.isEmpty()) {
            throw new RuntimeException("Game not found with ID: " + gameRoom.getGame().getId());
        }
        Game game = gameOpt.get();

        // Calculate total collection and shares
        BigDecimal totalCollection = BigDecimal.valueOf(game.getFee())
                .multiply(BigDecimal.valueOf(gameResult.getPlayers().size()));
        BigDecimal tax = totalCollection.multiply(BigDecimal.valueOf(TAX_PERCENT));
        BigDecimal vendorShare = totalCollection.multiply(BigDecimal.valueOf(VENDOR_PERCENT));
        BigDecimal platformShare = totalCollection.multiply(BigDecimal.valueOf(PLATFORM_PERCENT));
        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(USER_WIN_PERCENT));

        // Determine winners and losers
        List<PlayerDtoWinner> winners = determineWinners(gameResult.getPlayers());
        List<PlayerDtoWinner> losers = gameResult.getPlayers().stream()
                .filter(p -> winners.stream().noneMatch(w -> w.getPlayerId().equals(p.getPlayerId())))
                .collect(Collectors.toList());


        // If there is only one winner, assign the entire `userWin` to that player
        BigDecimal individualWinningAmount;
        if (winners.size() == 1) {
            individualWinningAmount = userWin; // No division needed, the winner gets the full share
        } else {
            // If there are multiple winners, divide `userWin` by the number of winners

            individualWinningAmount = userWin.divide(BigDecimal.valueOf(winners.size()), RoundingMode.HALF_UP);
        }

        // Process each winner
        for (PlayerDtoWinner winner : winners) {
            Wallet wallet = walletRepo.findByCustomCustomer_Id(winner.getPlayerId());
            if (wallet == null) {
                throw new RuntimeException("Wallet not found for user ID: " + winner.getPlayerId());
            }
            gameService.leaveRoom(winner.getPlayerId(), game.getId());

//            BigDecimal updatedWinning = wallet.getWinningAmount().add(individualWinningAmount);
            wallet.setWinningAmount(individualWinningAmount);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepo.save(wallet);

            Player winnerPlayer = playerRepository.findById(winner.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + winner.getPlayerId()));

            GameResultRecord winnerRecord = new GameResultRecord();
            winnerRecord.setRoomId(gameRoom.getId());
            winnerRecord.setGame(game);
            winnerRecord.setPlayer(winnerPlayer);
            winnerRecord.setScore(winner.getScore());
            winnerRecord.setWinningammount(individualWinningAmount);
            winnerRecord.setIsWinner(true);
            winnerRecord.setPlayedAt(LocalDateTime.now());
            gameResultRecordRepository.save(winnerRecord);
        }

        // Process each loser
        for (PlayerDtoWinner loser : losers) {
            gameService.leaveRoom(loser.getPlayerId(), game.getId());

            Player loserPlayer = playerRepository.findById(loser.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + loser.getPlayerId()));

            GameResultRecord loserRecord = new GameResultRecord();
            loserRecord.setRoomId(gameRoom.getId());
            loserRecord.setGame(game);
            loserRecord.setPlayer(loserPlayer);
            loserRecord.setScore(loser.getScore());
            loserRecord.setIsWinner(false);
            loserRecord.setWinningammount(BigDecimal.valueOf(0));

            loserRecord.setPlayedAt(LocalDateTime.now());
            gameResultRecordRepository.save(loserRecord);
        }

        // Add vendor share and update total balance
        this.addToVendorWalletAndTotalBalance(game.getVendorEntity().getService_provider_id(), vendorShare);

        // Optional: update AAG wallet if required
        // updateAAGWallet(platformShare, tax);

        // Return updated player details
//        return getAllPlayersDetails(gameResult, gameRoomOpt, game, winners, losers);
    }*/

    public void processMatch(GameResult gameResult) {
        // Fetch GameRoom
        GameRoom gameRoom = gameRoomRepository.findById(gameResult.getRoomId())
                .orElseThrow(() -> new RuntimeException("Game room not found with ID: " + gameResult.getRoomId()));

        // Fetch Game
        Game game = gameRepository.findById(gameRoom.getGame().getId())
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameRoom.getGame().getId()));

        // === Check if any playerId == 0 and replace players ===
        boolean hasInvalidPlayer = gameResult.getPlayers().stream()
                .anyMatch(p -> p.getPlayerId() == 0);

        List<PlayerDtoWinner> playersToProcess;

        if (hasInvalidPlayer) {
            System.out.println("[INFO] Invalid playerId detected. Replacing players list using room players for Room ID: " + gameResult.getRoomId());

            List<Player> roomPlayers = playerRepository.findByRoomId(gameResult.getRoomId());
            if (roomPlayers.isEmpty()) {
                throw new BusinessException("No players found in room " + gameResult.getRoomId(), HttpStatus.BAD_REQUEST);
            }

            // Build map of submitted playerId -> score (excluding invalid playerId=0)
            Map<Long, Integer> inputPlayerScores = gameResult.getPlayers().stream()
                    .filter(p -> p.getPlayerId() != 0)
                    .collect(Collectors.toMap(PlayerDtoWinner::getPlayerId, PlayerDtoWinner::getScore));

            // Build playersToProcess with preserved scores (if any)
            playersToProcess = roomPlayers.stream()
                    .map(p -> {
                        int score = inputPlayerScores.getOrDefault(p.getPlayerId(), 0);
                        return new PlayerDtoWinner(p.getPlayerId(), score);
                    })
                    .collect(Collectors.toList());

            System.out.println("[INFO] Replaced players list from room (Room ID " + gameResult.getRoomId() + "): " + playersToProcess);
        } else {
            playersToProcess = gameResult.getPlayers();
        }

        // Calculate total collection and shares
        BigDecimal totalCollection = BigDecimal.valueOf(game.getFee())
                .multiply(BigDecimal.valueOf(playersToProcess.size()));
        BigDecimal tax = totalCollection.multiply(BigDecimal.valueOf(TAX_PERCENT));
        BigDecimal vendorShare = totalCollection.multiply(BigDecimal.valueOf(VENDOR_PERCENT));
        BigDecimal platformShare = totalCollection.multiply(BigDecimal.valueOf(PLATFORM_PERCENT));
        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(USER_WIN_PERCENT));

        // Determine winners and losers
        List<PlayerDtoWinner> winners = determineWinners(playersToProcess);
        List<PlayerDtoWinner> losers = playersToProcess.stream()
                .filter(p -> winners.stream().noneMatch(w -> w.getPlayerId().equals(p.getPlayerId())))
                .collect(Collectors.toList());

        // Calculate individual winning amount
        BigDecimal individualWinningAmount;
        if (winners.size() == 1) {
            individualWinningAmount = userWin;
        } else {
            individualWinningAmount = userWin.divide(BigDecimal.valueOf(winners.size()), RoundingMode.HALF_UP);
        }

        // Process winners
        for (PlayerDtoWinner winner : winners) {
            Wallet wallet = walletRepo.findByCustomCustomer_Id(winner.getPlayerId());
            if (wallet == null) {
                throw new RuntimeException("Wallet not found for user ID: " + winner.getPlayerId());
            }

            gameService.leaveRoom(winner.getPlayerId(), game.getId());
            wallet.setWinningAmount(individualWinningAmount);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepo.save(wallet);

            Player winnerPlayer = playerRepository.findById(winner.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + winner.getPlayerId()));

            GameResultRecord winnerRecord = new GameResultRecord();
            winnerRecord.setRoomId(gameRoom.getId());
            winnerRecord.setGame(game);
            winnerRecord.setPlayer(winnerPlayer);
            winnerRecord.setScore(winner.getScore());
            winnerRecord.setWinningammount(individualWinningAmount);
            winnerRecord.setIsWinner(true);
            winnerRecord.setPlayedAt(LocalDateTime.now());
            gameResultRecordRepository.save(winnerRecord);
        }

        // Process losers
        for (PlayerDtoWinner loser : losers) {
            gameService.leaveRoom(loser.getPlayerId(), game.getId());

            Player loserPlayer = playerRepository.findById(loser.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + loser.getPlayerId()));

            GameResultRecord loserRecord = new GameResultRecord();
            loserRecord.setRoomId(gameRoom.getId());
            loserRecord.setGame(game);
            loserRecord.setPlayer(loserPlayer);
            loserRecord.setScore(loser.getScore());
            loserRecord.setIsWinner(false);
            loserRecord.setWinningammount(BigDecimal.ZERO);
            loserRecord.setPlayedAt(LocalDateTime.now());
            gameResultRecordRepository.save(loserRecord);
        }

        // Add vendor share and update total balance
        this.addToVendorWalletAndTotalBalance(game.getVendorEntity().getService_provider_id(), vendorShare);

        // Optional: update AAG wallet if required
        // updateAAGWallet(platformShare, tax);
    }

/*    private List<PlayerDtoWinner> determineWinners(List<PlayerDtoWinner> players) {
        int maxScore = players.stream()
                .mapToInt(PlayerDtoWinner::getScore)
                .max()
                .orElseThrow(() -> new RuntimeException("No players found"));

        return players.stream()
                .filter(p -> p.getScore() == maxScore)
                .collect(Collectors.toList());
    }*/

    private List<PlayerDtoWinner> determineWinners(List<PlayerDtoWinner> players) {
        if (players.isEmpty()) {
            throw new RuntimeException("No players found");
        }

        int maxScore = players.stream()
                .mapToInt(PlayerDtoWinner::getScore)
                .max()
                .orElseThrow(() -> new RuntimeException("No players found"));

        // If maxScore == 0 → no winners
        if (maxScore == 0) {
            return Collections.emptyList();
        }

        return players.stream()
                .filter(p -> p.getScore() == maxScore)
                .collect(Collectors.toList());
    }



    //method to get wiining ammount from game room to user
    public BigDecimal getWinningAmount(GameRoom gameRoom) {
        Optional<GameRoom> gameRoomOpt = gameRoomRepository.findById(gameRoom.getId());
        if (gameRoomOpt.isEmpty()) {
            throw new RuntimeException("Game room not found with ID: " + gameRoom.getId());
        }

        // Fetch the Game associated with the gameId
        Optional<Game> gameOpt = gameRepository.findById(gameRoom.getGame().getId());
        if (gameOpt.isEmpty()) {
            throw new RuntimeException("Game not found with ID: " + gameRoom.getGame().getId());
        }
        Game game = gameOpt.get();

        // Calculate the total collection and shares
        BigDecimal totalCollection = BigDecimal.valueOf(game.getFee()).multiply(BigDecimal.valueOf(gameOpt.get().getMaxPlayersPerTeam()));

        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(USER_WIN_PERCENT));
        BigDecimal finalAmountToUser = userWin;
        return finalAmountToUser;
    }




    public BigDecimal getWinningAmountLeague(LeagueRoom leagueRoom) {
        Optional<LeagueRoom> leagueRoomOptional = leagueRoomRepository.findById(leagueRoom.getId());
        if (leagueRoomOptional.isEmpty()) {
            throw new RuntimeException("Game room not found with ID: " + leagueRoom.getId());
        }

        // Fetch the Game associated with the gameId
        Optional<League> leagueOpt = leagueRepository.findById(leagueRoom.getLeague().getId());
        if (leagueOpt.isEmpty()) {
            throw new RuntimeException("league not found with ID: " + leagueRoomOptional.get().getId());
        }
        League league = leagueOpt.get();

        // Calculate the total collection and shares
        BigDecimal totalCollection = BigDecimal.valueOf(league.getFee()).multiply(BigDecimal.valueOf(leagueOpt.get().getMaxPlayersPerTeam()));

        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(USER_WIN_PERCENT));
        BigDecimal finalAmountToUser = userWin;

        return finalAmountToUser;
    }




    private Player getPlayerById(Long playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isEmpty()) {
            throw new RuntimeException("Player not found with ID: " + playerId);
        }
        return playerOpt.get();
    }

    // Method to determine the winner based on score
    private PlayerDtoWinner determineWinner(List<PlayerDtoWinner> players) {
        return players.stream()
                .max(Comparator.comparingInt(PlayerDtoWinner::getScore))  // Find the player with the highest score
                .orElseThrow(() -> new RuntimeException("No players found"));
    }

    // Get all players' details (including amount for the winner)
    public List<PlayerDto> getAllPlayersDetails(GameResult gameResult, Optional<GameRoom> gameRoomOpt, Game game, PlayerDtoWinner winner, PlayerDtoWinner loser) {
        List<PlayerDto> playersDetails = new ArrayList<>();

        // Calculate the total collection first
        BigDecimal totalCollection = BigDecimal.valueOf(game.getFee())
                .multiply(BigDecimal.valueOf(gameResult.getPlayers().size()));

        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(USER_WIN_PERCENT));
//        BigDecimal bonus = BigDecimal.valueOf(game.getFee()).multiply(BigDecimal.valueOf(BONUS_PERCENT));

        // Iterate over all players and set their details
        for (PlayerDtoWinner player : gameResult.getPlayers()) {
            PlayerDto playerDto = new PlayerDto();
            // Set player details based on whether they are the winner or loser
            if (player.getPlayerId().equals(winner.getPlayerId())) {
                // Winner gets the prize
                playerDto.setPlayerId(player.getPlayerId());
                playerDto.setScore(player.getScore());
                playerDto.setAmount(userWin);  // Set prize for winner
                playerDto.setPictureUrl(fetchPlayerPicture(player.getPlayerId()));
            } else {
                // Loser gets no prize (amount = 0)
                playerDto.setPlayerId(player.getPlayerId());
                playerDto.setScore(player.getScore());
                playerDto.setAmount(BigDecimal.ZERO);  // No prize for loser
                playerDto.setPictureUrl(fetchPlayerPicture(player.getPlayerId()));
            }

            playersDetails.add(playerDto);
        }

        return playersDetails;
    }

    // Fetch player's picture URL based on player ID
    private String fetchPlayerPicture(Long playerId) {
        Optional<CustomCustomer> customCustomer = customCustomerRepository.findById(playerId);
        return customCustomer.map(CustomCustomer::getProfilePic).orElse(null); // Return profile picture URL or null if not found
    }

    // Method to add the vendor share to the vendor's wallet
    @Transactional
    public void addToVendorWalletAndTotalBalance(Long vendorId, BigDecimal vendorShare) {
        // 1. Fetch Vendor
        VendorEntity vendor = vendorRepo.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        // 2. Get or create wallet for vendor
        VendorWallet wallet = vendor.getWallet();
        if (wallet == null) {
            wallet = new VendorWallet();
            wallet.setVendorEntity(vendor);
            wallet.setWinningAmount(BigDecimal.ZERO);  // Initializing as BigDecimal
            wallet.setIsTest(false);
            vendor.setWallet(wallet); // Set wallet in vendor
        }

        // 3. Update the vendor's wallet with the new share
        BigDecimal currentWinningAmount = wallet.getWinningAmount();
        BigDecimal newWinningAmount = currentWinningAmount.add(vendorShare);
        wallet.setWinningAmount(newWinningAmount);
        wallet.setUpdatedAt(LocalDateTime.now());

        // 4. Update the total wallet balance
        BigDecimal updatedBalance = vendor.getTotalWalletBalance().add(vendorShare);
        vendor.setTotalWalletBalance(updatedBalance);

        // 5. Save the vendor (wallet will be saved due to cascading)
        vendorRepo.save(vendor);
    }
    public List<LeaderboardDto> getLeaderboard(Long gameId, Long roomId, Boolean winnerFlag) {
        List<GameResultRecord> results;

        if (roomId != null) {
            // Filter by both game and room
            results = gameResultRecordRepository.findByGame_IdAndRoomId(gameId, roomId);
        } else {
            // Filter only by game
            results = gameResultRecordRepository.findByGame_Id(gameId);
        }
        if (results.isEmpty()) {
            throw new RuntimeException("No game results found for this room and game.");
        }

        // Filter based on winner param
        if (winnerFlag != null) {
            results = results.stream()
                    .filter(record -> record.getIsWinner().equals(winnerFlag))
                    .collect(Collectors.toList());
        }

        // Sort by score in descending order
        results.sort(Comparator.comparing(GameResultRecord::getScore).reversed());

        return results.stream()
                .map(this::mapToLeaderboardDto)
                .collect(Collectors.toList());
    }


    private LeaderboardDto mapToLeaderboardDto(GameResultRecord record) {
        Player player = record.getPlayer();
        BigDecimal totalCollection = BigDecimal.valueOf(record.getGame().getFee()).multiply(BigDecimal.valueOf(record.getGame().getMaxPlayersPerTeam()));

        BigDecimal amount = record.getIsWinner() ?
                (record.getWinningammount() != null ? record.getWinningammount() : BigDecimal.ZERO)
                : BigDecimal.ZERO;

        return new LeaderboardDto(
                player.getPlayerId(),
                player.getCustomer().getName(),
                player.getCustomer().getProfilePic(),
                record.getScore(),
                record.getIsWinner(),
                amount
        );
    }



}
