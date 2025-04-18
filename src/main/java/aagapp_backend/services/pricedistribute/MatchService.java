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
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.*;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.league.LeagueRoomRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.gameservice.GameService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private static final double TAX_PERCENT = 0.28;
    private static final double VENDOR_PERCENT = 0.05;
    private static final double PLATFORM_PERCENT = 0.04;
    private static final double USER_WIN_PERCENT = 0.63;
    private static final double BONUS_PERCENT = 0.20;

    @Autowired
    private GameRoomWinnerRepository gameRoomWinnerRepository;

    @Autowired
    private GameResultRecordRepository gameResultRecordRepository;

    @Autowired
    private LeagueRoomRepository leagueRoomRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

@Autowired
private LeagueRepository leagueRepository;
    @Autowired
    private TournamentRoomRepository tournamentRoomRepository;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private VendorRepository vendorRepo;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private WalletRepository walletRepo;

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



        // Optional: Update AAG Wallet if needed
        // updateAAGWallet(platformShare, tax);

        // Return all players' details (including updated amount for the winner)
        return getAllPlayersDetails(gameResult, gameRoomOpt, game, winner, loser);
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

        return new LeaderboardDto(
                player.getPlayerId(),
                player.getCustomer().getName(),
                player.getCustomer().getProfilePic(),
                record.getScore(),
                record.getIsWinner()
        );
    }



}
