package aagapp_backend.services.gameservice;

import aagapp_backend.components.Constant;
import aagapp_backend.components.pricelogic.PriceConstant;
import aagapp_backend.dto.*;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.*;

import aagapp_backend.entity.league.League;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.*;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.aagavailblegames.AagAvailbleGamesRepository;
import aagapp_backend.repository.game.*;

import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.CommonService;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.payment.PaymentFeatures;
import aagapp_backend.services.pricedistribute.MatchService;
import aagapp_backend.services.vendor.VenderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.RandomStringUtils;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.naming.LimitExceededException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.http.*;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.client.RestTemplate;
@Service
public class GameService {

    @Autowired
    private CommonService commonservice;

    @Autowired
    private CustomCustomerService   customCustomerService;
    @Autowired
    private NotificationRepository notificationRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final MoveRepository moveRepository;
    private MatchService matchService;
    private final VenderService venderService;
    private final GameRepository gameRepository;
    private final ResponseService responseService;
    private final PaymentFeatures paymentFeatures;
    private final EntityManager em;
    private final ExceptionHandlingService exceptionHandling;
    private final AagGameRepository aagGameRepository;
    private final TournamentRepository tournamentRepository;
    private final AagAvailbleGamesRepository aagAvailbleGamesRepository;
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final GameRoomRepository gameRoomRepository;
    private final VendorRepository vendorRepository;

    @Autowired
    public GameService(
            VendorRepository vendorRepository,
            MoveRepository moveRepository,
            @Lazy VenderService venderService,
            GameRepository gameRepository,
            ResponseService responseService,
            @Lazy PaymentFeatures paymentFeatures,
            EntityManager em,
            ExceptionHandlingService exceptionHandling,
            AagGameRepository aagGameRepository,
            TournamentRepository tournamentRepository,
            AagAvailbleGamesRepository aagAvailbleGamesRepository,
            LeagueRepository leagueRepository,
            PlayerRepository playerRepository,
            GameRoomRepository gameRoomRepository
    ) {
        this.vendorRepository = vendorRepository;
        this.moveRepository = moveRepository;
        this.venderService = venderService;
        this.gameRepository = gameRepository;
        this.responseService = responseService;
        this.paymentFeatures = paymentFeatures;
        this.em = em;
        this.exceptionHandling = exceptionHandling;
        this.aagGameRepository = aagGameRepository;
        this.tournamentRepository = tournamentRepository;
        this.aagAvailbleGamesRepository = aagAvailbleGamesRepository;
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.gameRoomRepository = gameRoomRepository;
    }

    @Autowired
    @Lazy
    public void setMatchService(MatchService matchService) {
        this.matchService = matchService;
    }

    @Autowired
    private LeagueService leagueService;

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(GameService.class);


    @Scheduled(cron = "0 * * * * *")  // Every minute
    public void checkAndActivateScheduledGames() {
        int page = 0;
        int pageSize = 100;
        List<Long> vendorIds;
        while (!(vendorIds = getActiveVendorIdsInBatch(page, pageSize)).isEmpty()) {


            for (Long vendorId : vendorIds) {

                updateGameStatusToActive(vendorId);
                updateLeagueStatusToActive(vendorId);
                updateExpiredGameStatus(vendorId);
                updateExpiredLeagueStatus(vendorId);

            }
            page++;
        }
    }

/// at 12 am cron should run daily
@Scheduled(cron = "0 0 0 * * *")  // Every day at midnight
@Transactional
public void updateDailylimit() {
    int page = 0;
    int pageSize = 100;
    List<Long> vendorIds;

    while (!(vendorIds = getActiveVendorIdsInBatch(page, pageSize)).isEmpty()) {
        try {
            logger.info("Updating daily limit for vendor IDs: {}", vendorIds);
            vendorRepository.updateDailyLimitForVendors(vendorIds);
        } catch (Exception e) {
            logger.error("Failed to update daily limits for batch page {} with vendorIds: {}", page, vendorIds, e);
        }
        page++;
    }
}


    public List<Long> getActiveVendorIdsInBatch(int page, int pageSize) {
        LocalDateTime now = LocalDateTime.now();

        // Query adjusted to capture the most recent active payment plans
        String queryString = "SELECT v.id FROM VendorEntity v " +
                "JOIN PaymentEntity p ON p.vendorEntity.id = v.id " +
                "WHERE p.status = :activeStatus " +
                "AND p.expiryAt IS NOT NULL " +
                "AND p.expiryAt > :now " +
                "AND p.id = (" +
                "   SELECT MAX(p2.id) " +
                "   FROM PaymentEntity p2 " +
                "   WHERE p2.vendorEntity.id = v.id " +
                "   AND p2.status = :activeStatus " +
                "   AND p2.expiryAt IS NOT NULL " +
                "   AND p2.expiryAt > :now" +
                ")";

        Query query = em.createQuery(queryString);
        query.setParameter("activeStatus", PaymentStatus.ACTIVE);
        query.setParameter("now", now);

        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }


    public boolean isGameAvailable(String gameName) {
        // Use the repository to find a game by its name
        Optional<AagAvailableGames> game = aagGameRepository.findByGameName(gameName);
        return game.isPresent(); // Return true if the game is found, false otherwise
    }
    public boolean isGameAvailableById(Long gameId) {
        // Use the repository to find a game by its name
        Optional<AagAvailableGames> game = aagGameRepository.findById(gameId);
        return game.isPresent(); // Return true if the game is found, false otherwise
    }

    @Transactional
    public Game publishLudoGame(GameRequest gameRequest, Long vendorId,Long existinggameId) throws LimitExceededException {

            // Create a new Game entity
            Game game = new Game();

            boolean isAvailable = isGameAvailableById(existinggameId);

            if (!isAvailable) {
                throw new BusinessException("game is not available" , HttpStatus.BAD_REQUEST);
            }
//        check from gamerequestgamename that existing game exists or not for same vendor
            Optional<AagAvailableGames> gameAvailable= aagGameRepository.findById(existinggameId);

//            game.setImageUrl(gameAvailable.get().getGameImage());
        AagAvailableGames gameEntity = gameAvailable.orElseThrow(() ->
                new BusinessException("Game not found with ID: " + existinggameId, HttpStatus.NOT_FOUND)
        );

        game.setImageUrl(commonservice.resolveGameImageUrl(gameEntity,gameRequest.getThemeId()));


        game.setName(gameAvailable.get().getGameName());


            // Fetch Vendor and Theme Entities
            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (vendorEntity == null) {
                throw new BusinessException("No records found for vendor" , HttpStatus.BAD_REQUEST);
            }
            ThemeEntity theme = em.find(ThemeEntity.class, gameRequest.getThemeId());
            if (theme == null) {
                throw new BusinessException("No theme found with the provided ID " + gameRequest.getThemeId() , HttpStatus.BAD_REQUEST);
            }

            // Set Vendor and Theme to the Game
            game.setVendorEntity(vendorEntity);
            game.setTheme(theme);
            game.setAaggameid(existinggameId);
            // Calculate moves based on the selected fee
            game.setFee(gameRequest.getFee());
            if(gameRequest.getFee()>10){
                game.setMove(Constant.TENMOVES);
            } else{
                game.setMove(Constant.SIXTEENMOVES);

            }

            // Get current time in Kolkata timezone
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            if (gameRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledInKolkata = gameRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new BusinessException("The game must be scheduled at least 4 hours in advance." , HttpStatus.BAD_REQUEST);
                }
                game.setStatus(GameStatus.SCHEDULED);
                game.setScheduledAt(scheduledInKolkata);
                game.setEndDate(scheduledInKolkata.plusHours(4));
            } else {
                game.setStatus(GameStatus.SCHEDULED);

                game.setScheduledAt(nowInKolkata.plusMinutes(15));
                game.setEndDate(nowInKolkata.plusHours(4));

            }

            // Set the minimum and maximum players
            if (gameRequest.getMinPlayersPerTeam() != null) {
                game.setMinPlayersPerTeam(gameRequest.getMinPlayersPerTeam());
            }
            if (gameRequest.getMaxPlayersPerTeam() != null) {
                game.setMaxPlayersPerTeam(gameRequest.getMaxPlayersPerTeam());
            }
            vendorEntity.setPublishedLimit((vendorEntity.getPublishedLimit() == null ? 0 : vendorEntity.getPublishedLimit()) + 1);


            // Set created and updated timestamps
            game.setCreatedDate(nowInKolkata);
            game.setUpdatedDate(nowInKolkata);

            // Save the game to get the game ID
            Game savedGame = gameRepository.save(game);

            // Create the first GameRoom (initialized, 2 players max)
            GameRoom gameRoom = createNewEmptyRoom(savedGame);

            // Save the game room
            gameRoomRepository.save(gameRoom);

            // Generate a shareable link for the game
            String shareableLink = generateShareableLink(savedGame.getId());
            savedGame.setShareableLink(shareableLink);
            // Return the saved game with the shareable link
            return gameRepository.save(savedGame);


    }



    public String createNewGame(String baseUrl, Long gameId, Long roomId, Integer players, Integer move, BigDecimal prize) {
        try {
            // Construct the URL for the POST request, including query parameters
//            String url = baseUrl + "/CreateNewGame?gameid=" + gameId + "&roomid=" + roomId + "&players=" + players + "&prize=" + prize + "&moves=" + move + "&gametype=GAME";
            String url = baseUrl + "/CreateNewGame?gametype=GAME"
                    + "&gameid=" + gameId
                    + "&roomid=" + roomId
                    + "&players=" + players
                    + "&prize=" + prize
                    + "&moves=" + move;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse the response body (assuming it's a JSON response)
            String responseBody = response.getBody();

            // Assuming the response is JSON and contains "GamePassword"
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            String gamePassword = jsonResponse.get("GamePassword").asText();

            return gamePassword;

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);

            throw new RuntimeException("Error occurred while creating the game on the server: " + e.getMessage(), e);
        }
    }
    @Transactional
    public ResponseEntity<?> joinRoom(Long playerId, Long gameId, String gametype) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new BusinessException("Game not found with ID: " + gameId, HttpStatus.BAD_REQUEST));

            BigDecimal entryFee = BigDecimal.valueOf(game.getFee());

            BigDecimal vendorShareAmount = (PriceConstant.VENDOR_REVENUE_PERCENT);


            if (isPlayerInRoom(player)) {
                leaveRoom(playerId, player.getGameRoom().getGame().getId());
            }

            GameRoom gameRoom = findAvailableGameRoom(game);


            commonservice.deductFromWallet(playerId, game.getFee(),"Rs. " + game.getFee() + " deducted for playing " + game.getName() + " game");

           commonservice.addVendorEarningForPayment(game.getVendorEntity().getService_provider_id(), BigDecimal.valueOf(game.getFee()), vendorShareAmount);

            boolean playerJoined = addPlayerToRoom(gameRoom, player);

            if (!playerJoined) {
                return responseService.generateErrorResponse("Room is already full with game ID: " + game.getId(), HttpStatus.BAD_REQUEST);
            }

            if (gameRoom.getCurrentPlayers().size() == gameRoom.getMaxPlayers()) {
                transitionRoomToOngoing(gameRoom);
                GameRoom newRoom = createNewEmptyRoom(game);
                gameRoomRepository.save(newRoom);
            }

            updatePlayerStatusToPlaying(player);

/*            Wallet wallet = player.getCustomer().getWallet();
            Double currentBalance = wallet.getUnplayedBalance();
            Double entryFee = game.getFee();
            if (currentBalance < entryFee) {
                return responseService.generateErrorResponse("Insufficient wallet balance", HttpStatus.BAD_REQUEST);
            }
            wallet.setUnplayedBalance(currentBalance - entryFee);

            // Now create a single notification for the vendor
            Notification notification = new Notification();
            CustomCustomer customer = customCustomerService.getCustomerById(playerId);
            notification.setCustomerId(customer.getId());
            notification.setDescription("Wallet balance deducted"); // Example NotificationType for a successful
            notification.setAmount(entryFee);
            notification.setDetails("Rs. " + entryFee + " deducted for playing " + game.getName()); // Example NotificationType for a successful

            notificationRepository.save(notification);*/


            return responseService.generateSuccessResponse("Player joined the Game Room", gameRoom, HttpStatus.OK);
        } catch (BusinessException e) {
            throw e;  // propagate cleanly
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Unexpected error in joinRoom: " + e.getMessage(), e);
        }
    }




//    deduct from wallet ammount for game and add notification
/*    public void deductFromWallet(Long playerId, Double amount) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));
        Wallet wallet = player.getCustomer().getWallet();
*//*        Double currentBalance = wallet.getUnplayedBalance();
        if (currentBalance < amount.doubleValue()) {
            throw new BusinessException("Insufficient wallet balance", HttpStatus.BAD_REQUEST);
        }
        wallet.setUnplayedBalance(currentBalance - amount.doubleValue());*//*

        Double unplayed = wallet.getUnplayedBalance();  // Add cash balance record
        BigDecimal winning = wallet.getWinningAmount(); // Winning ammount
        BigDecimal gameAmount = BigDecimal.valueOf(amount);
        BigDecimal unplayedBD = BigDecimal.valueOf(unplayed);


        System.out.println("unplayedBD: " + unplayedBD);
        System.out.println("gameAmount: " + gameAmount);
        System.out.println("winning: " + winning);

        BigDecimal totalBalance = unplayedBD.add(winning);
        System.out.println("totalBalance: " + totalBalance);

        if (totalBalance.compareTo(gameAmount) < 0) {
            throw new BusinessException("Insufficient wallet balance", HttpStatus.BAD_REQUEST);
        }

        // Case 2: Enough in unplayed balance
        if (unplayedBD.compareTo(gameAmount) >= 0) {
            wallet.setUnplayedBalance(unplayedBD.subtract(gameAmount).doubleValue());
            System.out.println("Deducted full from unplayed.");
        } else {
            BigDecimal remaining = gameAmount.subtract(unplayedBD);
            wallet.setUnplayedBalance(0.0); // Unplayed exhausted
            wallet.setWinningAmount(winning.subtract(remaining));
            System.out.println("Deducted " + unplayedBD + " from unplayed and " + remaining + " from winning.");
        }


        // Now create a single notification for the vendor
        Notification notification = new Notification();
        CustomCustomer customer = customCustomerService.getCustomerById(playerId);
        notification.setCustomerId(customer.getId());
        notification.setDescription("Wallet balance deducted"); // Example NotificationType for a successful
        notification.setAmount(amount.doubleValue());
        notification.setDetails("Rs. " + amount + " deducted for playing a game " + player.getGameRoom().getGame().getName() ); // Example NotificationType for a successful

        notificationRepository.save(notification);

    }*/


    @Transactional
    public VendorGameResponse getVendorPublishedGames(Long vendorId) {
        try{

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (vendorEntity == null) {
                throw new BusinessException("No records found for vendor with ID: " + vendorId , HttpStatus.BAD_REQUEST);
            }

            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime startOfDayInKolkata = nowInKolkata.toLocalDate().atStartOfDay(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime endOfDayInKolkata = startOfDayInKolkata.plusDays(1).minusSeconds(1);

            ZonedDateTime startTimeUTC = startOfDayInKolkata.withZoneSameInstant(ZoneId.of("UTC"));
            ZonedDateTime endTimeUTC = endOfDayInKolkata.withZoneSameInstant(ZoneId.of("UTC"));

            List<Game> games = gameRepository.findByVendorEntityAndScheduledAtBetween(vendorEntity, startTimeUTC, endTimeUTC);
            List<League> leagues = leagueRepository.findByVendorEntityAndScheduledAtBetween(vendorEntity, startTimeUTC, endTimeUTC);
            List<Tournament> tournaments = tournamentRepository.findByVendorEntityAndScheduledAtBetween(vendorId, startTimeUTC, endTimeUTC);
            List<AagAvailableGames> availableGames = aagAvailbleGamesRepository.findAll();

            VendorGameResponse response = new VendorGameResponse();
            response.setVendorId(vendorEntity.getService_provider_id());
            response.setDailyLimit(vendorEntity.getDailyLimit());
            response.setPublishedLimit(vendorEntity.getPublishedLimit());

            // Merge all published games, leagues, and tournaments into one list
            List<Map<String, String>> publishedContent = new ArrayList<>();

            publishedContent.addAll(games.stream()
                    .map(game -> {
                        Map<String, String> gameMap = new HashMap<>();
                        gameMap.put("imageUrl",(game.getTheme() != null && game.getTheme().getGameimageUrl() != null) ? game.getTheme().getGameimageUrl() : game.getImageUrl()
                                );
                        gameMap.put("name", game.getName() != null ? game.getName() : "n/a");
                        gameMap.put("themename", game.getTheme().getName());
                        return gameMap;
                    })
                    .collect(Collectors.toList()));

            publishedContent.addAll(leagues.stream()
                    .map(league -> {
                        Map<String, String> gameMap = new HashMap<>();
                        gameMap.put("imageUrl",             (league.getTheme() != null && league.getTheme().getGameimageUrl() != null) ? league.getTheme().getGameimageUrl() : league.getTheme().getImageUrl()
                        );
                        gameMap.put("name", league.getName() != null ? league.getName() : "n/a");
                        gameMap.put("themename", league.getTheme().getName());
                        return gameMap;
                    })
                    .collect(Collectors.toList()));


                    publishedContent.addAll(tournaments.stream()
                    .map(tournament -> {
                        Map<String, String> gameMap = new HashMap<>();
                        gameMap.put("imageUrl",(tournament.getTheme() != null && tournament.getTheme().getGameimageUrl() != null) ? tournament.getTheme().getGameimageUrl() : tournament.getTheme().getImageUrl());
                        gameMap.put("name", tournament.getName() != null ? tournament.getName() : "n/a");
                        gameMap.put("themename", tournament.getTheme().getName());
                        return gameMap;
                    })
                    .collect(Collectors.toList()));

            // Set merged list as publishedGames
            response.setPublishedGames(publishedContent);

            // available games untouched
            List<Map<String, String>> availableGamesList = availableGames.stream()
                    .map(game -> {
                        Map<String, String> gameMap = new HashMap<>();
                        gameMap.put("gameId", String.valueOf(game.getId()));
                        gameMap.put("gameImage", game.getGameImage());
                        gameMap.put("gameName", game.getGameName());
                        return gameMap;
                    })
                    .collect(Collectors.toList());

            response.setGames(availableGamesList);

            return response;
        }catch (BusinessException e) {
            throw e;
        }catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Unexpected error in getVendorPublishedGames: " + e.getMessage(), e);
        }


    }


    @Transactional
    public ResponseEntity<?> leaveRoom(Long playerId, Long gameId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new BusinessException("Game not found with ID: " + gameId, HttpStatus.BAD_REQUEST));

            if (!isPlayerInRoom(player)) {
                return responseService.generateErrorResponse("Player is not in room with this id: " + player.getPlayerId(), HttpStatus.BAD_REQUEST);
            }

            GameRoom gameRoom = player.getGameRoom();
            player.setGameRoom(null);

            List<Player> remainingPlayers = playerRepository.findAllByGameRoom(gameRoom);
            if (remainingPlayers.isEmpty()) {
                gameRoom.setStatus(GameRoomStatus.COMPLETED);
                gameRoomRepository.save(gameRoom);
            }

            return responseService.generateSuccessResponse("Player left the Game Room", gameRoom, HttpStatus.OK);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Unexpected error in leaveRoom: " + e.getMessage(), e);
        }
    }



    // Check if the player is already in a room
    public boolean isPlayerInRoom(Player player) {
        return player.getGameRoom() != null;
    }


    public GameRoom findAvailableGameRoom(Game game) {
            try{
                // Find a game room that has available space and matches the specified game
                List<GameRoom> availableRooms = gameRoomRepository.findByGameAndStatus(game, GameRoomStatus.INITIALIZED);
                // Loop through the available rooms and return the first one with space
                for (GameRoom room : availableRooms) {
                    if (room.getCurrentPlayers().size() < room.getMaxPlayers()) {
                        return room;
                    }
                }

                // If no available room is found, return null or create a new room
                GameRoom newRoom = createNewEmptyRoom(game);
                gameRoomRepository.save(newRoom); // Save the new room
                return newRoom;
            }catch (Exception e){
                exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                return null;
            }
    }


    public boolean addPlayerToRoom(GameRoom gameRoom, Player player) {
        // Check if the game room has space for the player
        if (gameRoom.getCurrentPlayers().size() < gameRoom.getMaxPlayers()) {
            gameRoom.getCurrentPlayers().add(player);

            player.setGameRoom(gameRoom);
            gameRoomRepository.save(gameRoom);
            playerRepository.save(player);



            // Return true as the player was successfully added
            return true;
        } else {
            return false;
        }
    }


    // Transition room status to ONGOING when it's full
    private void transitionRoomToOngoing(GameRoom newRoom) {

        try {

            Game game = newRoom.getGame();

            String gamePassword = null;

            // Calculate the total collection and shares
            BigDecimal totalCollection = BigDecimal.valueOf(game.getFee()).multiply(BigDecimal.valueOf(game.getMaxPlayersPerTeam()));
            BigDecimal totalPrize = totalCollection.multiply(Constant.USER_PERCENTAGE);

            BigDecimal entryFee = BigDecimal.valueOf(game.getFee());

            BigDecimal singleBonus = entryFee.multiply(BigDecimal.valueOf(Constant.BONUS_PERCENT));
            BigDecimal totalBonusCollected = singleBonus.multiply(BigDecimal.valueOf(game.getMaxPlayersPerTeam()));


            BigDecimal finalWinnerAmount = totalPrize.add(totalBonusCollected.multiply(BigDecimal.valueOf(2)));

            BigDecimal totalPrizenew = finalWinnerAmount;

            System.out.println("totalPrize: " + totalPrize);
            System.out.println("totalPrizenew: " + totalPrizenew);


            String gameName = game.getName().toLowerCase();

            if (gameName.equals("ludo")) {
                gamePassword = this.createNewGame(Constant.ludobaseurl, game.getId(), newRoom.getId(), game.getMaxPlayersPerTeam(), game.getMove(), totalPrizenew);
            } else if (gameName.equals("snake & ladder")) {
                gamePassword = this.createNewGame(Constant.snakebaseUrl, game.getId(), newRoom.getId(), game.getMaxPlayersPerTeam(), game.getMove(), totalPrizenew);
            } else {
                throw new BusinessException("Unsupported game: " + gameName, HttpStatus.BAD_REQUEST);
            }
            newRoom.setGamepassword(gamePassword);

            newRoom.setStatus(GameRoomStatus.ONGOING);
            gameRoomRepository.save(newRoom);
        }catch (Exception e){
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Unexpected error in transitionRoomToOngoing: " + e.getMessage(), e);
        }
    }


    private GameRoom createNewEmptyRoom(Game game) {
        try{
            GameRoom newRoom = new GameRoom();
            newRoom.setMaxPlayers(game.getMaxPlayersPerTeam());
            newRoom.setCurrentPlayers(new ArrayList<>());
            newRoom.setStatus(GameRoomStatus.INITIALIZED);
            newRoom.setCreatedAt(LocalDateTime.now());
            newRoom.setRoomCode(generateRoomCode());
            newRoom.setGame(game);

            // Save the room first so it gets an ID
            gameRoomRepository.save(newRoom); // Save and assign the generated ID


            return newRoom;
        }catch (Exception e){
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return null;
        }
    }



    // Generate a unique room code
    private String generateRoomCode() {
        String roomCode;
        do {
            roomCode = RandomStringUtils.randomAlphanumeric(6);
        } while (gameRoomRepository.findByRoomCode(roomCode) != null);
        return roomCode;
    }

    // Update the player's status to PLAYING
    private void updatePlayerStatusToPlaying(Player player) {
//        player.setPlayerStatus(PlayerStatus.PLAYING);
        playerRepository.save(player);
    }


    private String generateShareableLink(Long gameId) {
        return "https://example.com/games/" + gameId;
    }

    @Transactional
    public Page<GetGameResponseDTO> getAllGames(String status, Long vendorId, Pageable pageable,String gamename) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM aag_ludo_game g WHERE 1=1");

            if (status != null && !status.isEmpty()) {
                sql.append(" AND g.status = :status");
            }
            if(gamename!= null &&!gamename.isEmpty()) {
                sql.append(" AND g.name LIKE :gamename");
            }
            if (vendorId != null) {
                sql.append(" AND g.vendor_id = :vendorId");
            }

            Query query = em.createNativeQuery(sql.toString(), Game.class);

            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }
            if (vendorId != null) {
                query.setParameter("vendorId", vendorId);
            }
            if(gamename!= null &&!gamename.isEmpty()) {
                query.setParameter("gamename", "%" + gamename + "%");
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Game> games = query.getResultList();


            // Map Game entities to GetGameResponseDTO
            List<GetGameResponseDTO> gameResponseDTOs = games.stream()
                    .map(game -> new GetGameResponseDTO(
                            game.getId(),
                            game.getName(),

                            game.getFee(),
                            game.getMove(),
                            game.getStatus(),
                            game.getShareableLink(),
                            game.getAaggameid(),
                            (game.getTheme() != null && game.getTheme().getGameimageUrl() != null) ? game.getTheme().getGameimageUrl() : game.getImageUrl(),
                            game.getTheme() != null ? game.getTheme().getName() : null,
                            game.getTheme() != null ? game.getTheme().getImageUrl() : null,
                            game.getCreatedDate() != null ? game.getCreatedDate() : null,

                            game.getScheduledAt() != null ? game.getScheduledAt() : null,
                            game.getEndDate() != null ? game.getEndDate() : null,

                            game.getMinPlayersPerTeam(),
                            game.getMaxPlayersPerTeam(),
                            game.getVendorEntity() != null ? game.getVendorEntity().getFirst_name() : null,
                            game.getVendorEntity() != null ? game.getVendorEntity().getProfilePic() : null
                    ))
                    .collect(Collectors.toList());

            String countSql = "SELECT COUNT(*) FROM aag_ludo_game g WHERE 1=1";
            if (status != null && !status.isEmpty()) {
                countSql += " AND g.status = :status";
            }
            if (vendorId != null) {
                countSql += " AND g.vendor_id = :vendorId";
            }

            Query countQuery = em.createNativeQuery(countSql);
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }
            if (vendorId != null) {
                countQuery.setParameter("vendorId", vendorId);
            }

            Long count = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(gameResponseDTOs, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving games", e);
        }
    }


    public ResponseEntity<?> getGameDetails(Long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (game == null) {
            return ResponseService.generateErrorResponse("Game not found", HttpStatus.NOT_FOUND);
        }

        game.ifPresent(g -> {

            g.setCreatedDate(convertToKolkataTime(g.getCreatedDate()));
            g.setUpdatedDate(convertToKolkataTime(g.getUpdatedDate()));
            g.setScheduledAt(convertToKolkataTime(g.getScheduledAt()));

        });

        return responseService.generateSuccessResponse("Game details Found", game, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> updateGame(Long vendorId, Long gameId, GameRequest gameRequest) {

      try {
          String jpql = "SELECT g FROM Game g WHERE g.id = :gameId AND g.vendorEntity.id = :vendorId";
          TypedQuery<Game> query = em.createQuery(jpql, Game.class);
          query.setParameter("gameId", gameId);
          query.setParameter("vendorId", vendorId);

          Game game = query.getResultList().stream()
                  .findFirst()
                  .orElseThrow(() -> new BusinessException("Game ID: " + gameId + " does not belong to Vendor ID: " + vendorId, HttpStatus.BAD_REQUEST));


          ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
          ZonedDateTime scheduledAtInKolkata = game.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

          if (game.getStatus() == GameStatus.EXPIRED) {
              throw new BusinessException("Game ID: " + game.getId() + " has already expired. No update allowed.", HttpStatus.BAD_REQUEST);
          } else if (game.getStatus() == GameStatus.ACTIVE) {
              throw new BusinessException("Game ID: " + game.getId() + " is already active. No update allowed.", HttpStatus.BAD_REQUEST);
          }

          if (scheduledAtInKolkata != null) {
              ZonedDateTime oneDayBeforeScheduled = scheduledAtInKolkata.minusDays(1);

              if (nowInKolkata.isBefore(oneDayBeforeScheduled)) {


               /* if (gameRequest.getName() != null && !gameRequest.getName().isEmpty()) {
                    game.setName(gameRequest.getName());
                }*/

                  // Calculate moves based on the selected fee

                  game.setFee(gameRequest.getFee());
                  if(gameRequest.getFee()>10){
                      game.setMove(Constant.TENMOVES);
                  } else{
                      game.setMove(Constant.SIXTEENMOVES);
                  }
                  ZonedDateTime scheduledInKolkata = gameRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

                  game.setScheduledAt(scheduledInKolkata);
                  game.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
                  game.setEndDate(scheduledInKolkata.plusHours(4));

                  em.merge(game);
                  return ResponseEntity.ok("Game updated successfully");
              } else {
                  throw new BusinessException("Game ID: " + game.getId() + " cannot be updated on the scheduled date or after.", HttpStatus.BAD_REQUEST);
              }
          } else {
              throw new BusinessException("Game ID: " + game.getId() + " does not have a scheduled time.", HttpStatus.BAD_REQUEST);
          }

      }catch (BusinessException e) {
          throw e;
      }

      catch (IllegalStateException e) {
          return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
      }catch (Exception e) {
          exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
          return ResponseService.generateErrorResponse("Error updating game details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }

    @Transactional
    public void updateExpiredGameStatus(Long vendorId) {
        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            String sql = "SELECT * FROM aag_ludo_game al " +
                    "WHERE al.vendor_id = :vendorId " +
                    "AND al.status = :status " +
                    "AND al.end_date <= :nowInKolkata"; // Add condition to check end_date
            String activeStatus = GameStatus.ACTIVE.name();

            Query query = em.createNativeQuery(sql, Game.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("status", activeStatus);
            query.setParameter("nowInKolkata", nowInKolkata);

            List<Game> games = query.getResultList();

            for (Game game : games) {
                game.setEndDate(convertToKolkataTime(game.getEndDate()));

                // Ensure we have an end date for the game
                if (game.getEndDate() != null && game.getEndDate().isBefore(nowInKolkata)) {
                    game.setStatus(GameStatus.EXPIRED); // Change the status to EXPIRED
                    game.setUpdatedDate(nowInKolkata);
                    gameRepository.save(game);
                }
            }

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error updating expired game statuses: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Async
    public void updateExpiredLeagueStatus(Long vendorId) {
        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            String sql = "SELECT * " +
                    "FROM aag_league l WHERE l.vendor_id = :vendorId " +
                    "AND l.scheduled_at <= :nowInKolkata AND l.status = :status";
            String activeStatus = GameStatus.ACTIVE.name();

            Query query = em.createNativeQuery(sql, League.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("nowInKolkata", nowInKolkata);
            query.setParameter("status", activeStatus); // Only check leagues that are active

            List<League> leagues = query.getResultList();

            for (League league : leagues) {
                // Ensure we have an end date for the league (e.g., league could have an "endDate" property)
                if (league.getEndDate() != null && league.getEndDate().isBefore(nowInKolkata)) {
                    league.setStatus(LeagueStatus.EXPIRED); // Change the status to EXPIRED
                    league.setUpdatedDate(nowInKolkata);
                    leagueService.distributePrizePoolSilently(league.getId());
                    leagueRepository.save(league);
                }
            }

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error updating expired league statuses: " + e.getMessage(), e);
        }
    }

    public int countGamesByVendorIdAndScheduledDate(Long vendorId, LocalDate date) {
        String queryString = "SELECT COUNT(g) FROM Game g WHERE g.vendorEntity.id = :vendorId AND FUNCTION('DATE', g.createdDate) = :date";
        Query query = em.createQuery(queryString);
        query.setParameter("vendorId", vendorId);
        query.setParameter("date", date);

        return ((Long) query.getSingleResult()).intValue();
    }


    @Transactional
    public Page<Game> findGamesByVendor(Long vendorId, String status, Pageable pageable) {
        try {
            String sql = "SELECT * FROM aag_ludo_game g WHERE g.vendor_id = :vendorId";

            if (status != null && !status.isEmpty()) {
                sql += " AND g.status = :status";
            }

            Query query = em.createNativeQuery(sql, Game.class);
            query.setParameter("vendorId", vendorId);

            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Game> games = query.getResultList();

            games.forEach(game -> {
                if (game.getCreatedDate() != null) {
                    game.setCreatedDate(convertToKolkataTime(game.getCreatedDate()));

                }
                if (game.getUpdatedDate() != null) {
                    game.setUpdatedDate(convertToKolkataTime(game.getUpdatedDate()));
                }
                if (game.getScheduledAt() != null) {
                    game.setScheduledAt(convertToKolkataTime(game.getScheduledAt()));
                }
                if(game.getEndDate() != null) {
                    game.setEndDate(convertToKolkataTime(game.getEndDate()));
                }
            });

            String countSql = "SELECT COUNT(*) FROM aag_ludo_game g WHERE g.vendor_id = :vendorId";
            if (status != null && !status.isEmpty()) {
                countSql += " AND g.status = :status";
            }

            Query countQuery = em.createNativeQuery(countSql);
            countQuery.setParameter("vendorId", vendorId);
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }

            Long count = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(games, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving games by vendor ID: " + e, e);
        }
    }

    @Transactional
    public Page<GetGameResponseDTO> findGamesScheduledForToday(Long vendorId, Pageable pageable) {

        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime startOfDay = nowInKolkata.toLocalDate().atStartOfDay(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

            String sql = "SELECT * FROM aag_ludo_game g WHERE g.vendor_id = :vendorId "
                    + "AND g.scheduled_at >= :startOfDay AND g.scheduled_at <= :endOfDay "
                    + "AND g.status = :status";
            String activeStatus = GameStatus.ACTIVE.name();

            Query query = em.createNativeQuery(sql, Game.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("startOfDay", startOfDay);
            query.setParameter("endOfDay", endOfDay);
            query.setParameter("status", activeStatus); // Pass the string representation of the enum


            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Game> games = query.getResultList();


            List<GetGameResponseDTO> gameResponseDTOs = games.stream()
                    .map(game -> new GetGameResponseDTO(
                            game.getId(),
                            game.getName(),
                            game.getFee(),
                            game.getMove(),
                            game.getStatus(),
                            game.getShareableLink(),
                            game.getAaggameid(),
                            (game.getTheme() != null && game.getTheme().getGameimageUrl() != null) ? game.getTheme().getGameimageUrl() : game.getImageUrl(),
                            game.getTheme() != null ? game.getTheme().getName() : null,
                            game.getTheme() != null ? game.getTheme().getImageUrl() : null,
                            game.getCreatedDate() != null ? game.getCreatedDate() : null,
                            game.getScheduledAt() != null ? game.getScheduledAt() : null,
                            game.getEndDate() != null ? game.getEndDate() : null,
                            game.getMinPlayersPerTeam(),
                            game.getMaxPlayersPerTeam(),
                            game.getVendorEntity() != null ? game.getVendorEntity().getFirst_name() : null,
                            game.getVendorEntity() != null ? game.getVendorEntity().getProfilePic() : null
                    ))
                    .collect(Collectors.toList());

            long count = games.size();
            return new PageImpl<>(gameResponseDTOs, pageable, count);
//            return new PageImpl<>(games, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving active games scheduled for today by vendor ID: " + vendorId, e);
        }
    }

    @Transactional
    public List<GetGameResponseDashboardDTO> findActivegamesByVendorId(Long vendorId, int offset, int limit) {
        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime startOfDay = nowInKolkata.toLocalDate().atStartOfDay(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

            String sql = "SELECT * FROM aag_ludo_game g WHERE g.vendor_id = :vendorId "
                    + "AND g.scheduled_at >= :startOfDay AND g.scheduled_at <= :endOfDay "
                    + "AND g.status = :status";
            String activeStatus = GameStatus.ACTIVE.name();

            Query query = em.createNativeQuery(sql, Game.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("startOfDay", startOfDay);
            query.setParameter("endOfDay", endOfDay);
            query.setParameter("status", activeStatus); // Pass the string representation of the enum

            // Set the limit and offset for pagination
            query.setFirstResult(offset);
            query.setMaxResults(limit);

            List<Game> games = query.getResultList();

            // Adjust times for the returned games
            games.forEach(game -> {
                game.setCreatedDate(convertToKolkataTime(game.getCreatedDate()));
                game.setUpdatedDate(convertToKolkataTime(game.getUpdatedDate()));
                game.setScheduledAt(convertToKolkataTime(game.getScheduledAt()));
            });

            // Convert games to response DTOs
            List<GetGameResponseDashboardDTO> gameResponseDTOs = games.stream()
                    .map(game -> new GetGameResponseDashboardDTO(
                            game.getId(),
                            game.getName(),
                            (game.getTheme() != null && game.getTheme().getGameimageUrl() != null) ? game.getTheme().getGameimageUrl() : game.getImageUrl()
/*
                            game.getTheme() != null ? game.getTheme().getImageUrl() : null
*/

                    ))
                    .collect(Collectors.toList());

            return gameResponseDTOs;

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving active games scheduled for today by vendor ID: " + vendorId, e);
        }
    }





    private ZonedDateTime convertToKolkataTime(ZonedDateTime utcDateTime) {
        if (utcDateTime != null) {
            return utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
        }
        return null;  // Handle null cases
    }
    @Transactional
    private void updateGameStatusToActive(Long vendorId) {
        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            String sql = "SELECT * " +
                    "FROM aag_ludo_game g WHERE g.vendor_id = :vendorId " +
                    "AND g.scheduled_at <= :nowInKolkata AND g.status = :status";

            String activeStatus = GameStatus.SCHEDULED.name();

            Query query = em.createNativeQuery(sql, Game.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("nowInKolkata", nowInKolkata);
            query.setParameter("status", activeStatus);

            List<Game> games = query.getResultList();

            for (Game game : games) {

                game.setStatus(GameStatus.ACTIVE);
                game.setScheduledAt(nowInKolkata);
                game.setUpdatedDate(nowInKolkata);
                gameRepository.save(game);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error updating game statuses: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Async
    public void updateLeagueStatusToActive(Long vendorId) {
        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            String sql = "SELECT * " +
                    "FROM aag_league l " +
                    "WHERE l.scheduled_at <= :nowInKolkata " +
                    "AND l.vendor_id = :vendorId " +
                    "AND l.status = :scheduledStatus";


            Query query = em.createNativeQuery(sql, League.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("scheduledStatus", Constant.SCHEDULED);
            query.setParameter("nowInKolkata", nowInKolkata);


            List<League> leagues = query.getResultList();

            for (League league : leagues) {


                    league.setStatus(LeagueStatus.ACTIVE);
                    league.setScheduledAt(nowInKolkata);
                    league.setUpdatedDate(nowInKolkata);
                    leagueRepository.save(league);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error updating game statuses: " + e.getMessage(), e);
        }
    }



    public ResponseEntity<?> getRoomById(Long roomId) {
        try {
            GameRoom gameRoom = gameRoomRepository.findById(roomId).orElse(null);
            if (gameRoom == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(gameRoom);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error retrieving game room by ID: " + roomId, e);
        }
    }

}

