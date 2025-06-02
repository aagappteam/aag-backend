package aagapp_backend.services.pricedistribute;
import aagapp_backend.components.Constant;
import aagapp_backend.dto.GameResult;
import aagapp_backend.dto.LeaderboardDto;
import aagapp_backend.dto.PlayerDto;
import aagapp_backend.dto.PlayerDtoWinner;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameResultRecord;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.wallet.VendorWallet;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.GameRoomStatus;
import aagapp_backend.repository.NotificationRepository;
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
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private NotificationRepository notificationRepository;

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

/*    public void processMatchfirstone(GameResult gameResult) {


        if (gameResult.getGameWinner().equals("NONE")) {
//            no need to procees further
            return;
        }

        GameRoom gameRoom = gameRoomRepository.findById(gameResult.getRoomId())
                .orElseThrow(() -> new RuntimeException("Game room not found with ID: " + gameResult.getRoomId()));

        // Fetch Game
        Game game = gameRepository.findById(gameRoom.getGame().getId())
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameRoom.getGame().getId()));

        // Fetch players from the room
        List<Player> roomPlayers = playerRepository.findByRoomId(gameResult.getRoomId());
        if (roomPlayers.isEmpty()) {
            return;

//            throw new RuntimeException("No players found in room " + gameResult.getRoomId());
        }

        // Fetch players from gameResult and replace playerId 0 with a valid one from the room
        List<PlayerDtoWinner> playersToProcess = gameResult.getPlayers().stream()
                .map(p -> {
                    if (p.getPlayerId() == 0) {
                        // Find the first available player from the room that is not assigned yet
                        Player availablePlayer = roomPlayers.stream()
                                .filter(rp -> gameResult.getPlayers().stream()
                                        .noneMatch(existingPlayer -> existingPlayer.getPlayerId() == rp.getPlayerId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No valid player found in room"));

                        // Assign the playerId from the available room player
                        p.setPlayerId(availablePlayer.getPlayerId());
                    }
                    return p;
                })
                .collect(Collectors.toList());

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

        // Logs to help debug
        System.out.println("[INFO] Winners: " + winners.size());
        if (!winners.isEmpty()) {
            System.out.println("[INFO] Winner PlayerId: " + winners.get(0).getPlayerId());
        }
        if (!losers.isEmpty()) {
            System.out.println("[INFO] Loser PlayerId: " + losers.get(0).getPlayerId());
        }

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
    }*/


    @Transactional
    public void processMatch(GameResult gameResult) {
        if (gameResult.getGameWinner().equals("NONE")) {
            return;
        }

        GameRoom gameRoom = gameRoomRepository.findById(gameResult.getRoomId())
                .orElseThrow(() -> new RuntimeException("Game room not found with ID: " + gameResult.getRoomId()));

        Game game = gameRepository.findById(gameRoom.getGame().getId())
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameRoom.getGame().getId()));

        List<Player> roomPlayers = playerRepository.findByRoomId(gameResult.getRoomId());
        if (roomPlayers.isEmpty()) {
            return;
        }
        int totalPlayers = gameRoom.getMaxPlayers();

        List<PlayerDtoWinner> validPlayers = gameResult.getPlayers().stream()
                .filter(p -> p.getPlayerId() != 0)
                .filter(p -> roomPlayers.stream().anyMatch(rp -> rp.getPlayerId().equals(p.getPlayerId())))
                .collect(Collectors.toList());

        if (validPlayers.isEmpty()) {
            System.out.println("[WARN] No valid players found in input. Skipping match processing.");
            return;
        }

        System.out.println("[INFO] Valid players: " + validPlayers.stream()
                .map(p -> "PlayerId=" + p.getPlayerId() + " Score=" + p.getScore())
                .collect(Collectors.joining(", ")));

        BigDecimal totalCollection = BigDecimal.valueOf(game.getFee())
                .multiply(BigDecimal.valueOf(totalPlayers));
        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(Constant.USER_WIN_PERCENT));
        System.out.println("userWin" + userWin);

        // Determine winners (can be 1 or more)
        List<PlayerDtoWinner> winners = determineWinners(validPlayers);
        List<Long> winnerIds = winners.stream()
                .map(PlayerDtoWinner::getPlayerId)
                .toList();

        BigDecimal entryFee = BigDecimal.valueOf(game.getFee());
//        int totalPlayers = roomPlayers.size();
//        int totalPlayers = gameRoom.getMaxPlayers();
        int totalWinners = winners.size();

        BigDecimal singleBonus = entryFee.multiply(BigDecimal.valueOf(Constant.BONUS_PERCENT));
        BigDecimal totalBonusCollected = singleBonus.multiply(BigDecimal.valueOf(totalPlayers));



        BigDecimal totalBonusToDistribute = totalBonusCollected.multiply(BigDecimal.valueOf(2));

        BigDecimal bonusPerWinner = totalBonusToDistribute.divide(BigDecimal.valueOf(totalWinners), 2, RoundingMode.HALF_UP);

        BigDecimal individualPrize = userWin.divide(BigDecimal.valueOf(totalWinners), 2, RoundingMode.HALF_UP);

        System.out.println("bonusPerWinner = " + bonusPerWinner);


        BigDecimal finalWinnerAmount = individualPrize.add(bonusPerWinner);
//       BigDecimal individualWinningAmount = userWin.divide(BigDecimal.valueOf(winners.size()), 2, RoundingMode.HALF_UP);

        System.out.println("[INFO] Winners: " + winnerIds + " Split amount per winner = " + individualPrize  + " Final amount = " + finalWinnerAmount);

        // Process winners
        for (PlayerDtoWinner winner : winners) {
            Wallet winnerWallet = walletRepo.findByCustomCustomer_Id(winner.getPlayerId());
            if (winnerWallet == null) {
                throw new RuntimeException("Wallet not found for user ID: " + winner.getPlayerId());
            }

            gameService.leaveRoom(winner.getPlayerId(), game.getId());

            winnerWallet.setWinningAmount(winnerWallet.getWinningAmount().add(individualPrize));

            winnerWallet.setUpdatedAt(LocalDateTime.now());
            walletRepo.save(winnerWallet);

            CustomCustomer winnerCustomer = customCustomerRepository.findById(winner.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + winner.getPlayerId()));

            BigDecimal currentBonus = winnerCustomer.getBonusBalance();
            if (currentBonus == null) {
                currentBonus = BigDecimal.ZERO;
            }
            System.out.println("currentBonus = " + currentBonus);
            winnerCustomer.setBonusBalance(currentBonus.add(bonusPerWinner));


            customCustomerRepository.save(winnerCustomer);

            Player winnerPlayer = playerRepository.findById(winner.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + winner.getPlayerId()));

            GameResultRecord winnerRecord = new GameResultRecord();
            winnerRecord.setRoomId(gameRoom.getId());
            winnerRecord.setGame(game);
            winnerRecord.setPlayer(winnerPlayer);
            winnerRecord.setScore(winner.getScore());
            winnerRecord.setWinningammount(finalWinnerAmount);
            winnerRecord.setIsWinner(true);
            winnerRecord.setPlayedAt(LocalDateTime.now());
            gameResultRecordRepository.save(winnerRecord);

            Notification notification = new Notification();
            notification.setAmount(finalWinnerAmount.doubleValue());
            notification.setDetails("You won Rs. " + finalWinnerAmount + " in Game " + game.getName());
            notification.setDescription("Game Winning Prize");
            notification.setRole("Customer");


            notification.setCustomerId(winnerPlayer.getCustomer().getId());
            notificationRepository.save(notification);

            System.out.println("[INFO] Winner record created for PlayerId=" + winner.getPlayerId());
        }

        // Process losers
        List<PlayerDtoWinner> losers = validPlayers.stream()
                .filter(p -> !winnerIds.contains(p.getPlayerId()))
                .collect(Collectors.toList());

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

/*            Notification notification = new Notification();
            notification.setAmount(0.0);
            notification.setDetails("Better luck next time! You did not win in Game " + game.getName());
            notification.setDescription("Game Result");
            notification.setRole("Customer");


            notification.setCustomerId(loserPlayer.getCustomer().getId());
            notificationRepository.save(notification);

            System.out.println("[INFO] Loser record created for PlayerId=" + loser.getPlayerId());*/
        }

        // Add vendor share and update total balance
//        this.addToVendorWalletAndTotalBalance(game.getVendorEntity().getService_provider_id(), vendorShare);
    }


    private List<PlayerDtoWinner> determineWinners(List<PlayerDtoWinner> players) {
        int maxScore = players.stream()
                .mapToInt(PlayerDtoWinner::getScore)
                .max()
                .orElseThrow(() -> new RuntimeException("No players found"));

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

        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(Constant.USER_WIN_PERCENT));
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

        BigDecimal userWin = totalCollection.multiply(BigDecimal.valueOf(Constant.USER_WIN_PERCENT));
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


    // Fetch player's picture URL based on player ID
    private String fetchPlayerPicture(Long playerId) {
        Optional<CustomCustomer> customCustomer = customCustomerRepository.findById(playerId);
        return customCustomer.map(CustomCustomer::getProfilePic).orElse(null); // Return profile picture URL or null if not found
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
