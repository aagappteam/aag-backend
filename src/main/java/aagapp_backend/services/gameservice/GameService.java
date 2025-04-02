package aagapp_backend.services.gameservice;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.*;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.*;

import aagapp_backend.entity.league.League;
import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.*;
import aagapp_backend.repository.aagavailblegames.AagAvailbleGamesRepository;
import aagapp_backend.repository.game.*;

import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.payment.PaymentFeatures;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.naming.LimitExceededException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {

    @Autowired
    private MoveRepository moveRepository;

    private GameRepository gameRepository;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;
    private EntityManager em;
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private AagGameRepository aagGameRepository;

    @Autowired
    private AagAvailbleGamesRepository aagAvailbleGamesRepository;

    private final LeagueRepository leagueRepository;

    public GameService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    private PlayerRepository playerRepository;
    private GameRoomRepository gameRoomRepository;


    @Autowired
    public void setExceptionHandling(ExceptionHandlingService exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setPaymentFeatures(@Lazy PaymentFeatures paymentFeatures) {
        this.paymentFeatures = paymentFeatures;
    }

    @Autowired
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setPlayerRepository(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Autowired
    public void setGameRoomRepository(GameRoomRepository gameRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
    }


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

/*    @Scheduled(cron = "0 * * * * *")  // Every minute
    public void checkAndActivateScheduledGames() {
        int page = 0;
        int pageSize = 100;
        List<Long> vendorIds;
        while (!(vendorIds = getActiveVendorIdsInBatch(page, pageSize)).isEmpty()) {
            System.out.println("Scheduled task checkAndActivateScheduledGames started  " + vendorIds);

            vendorIds.parallelStream().forEach(vendorId -> {
                updateGameStatusToActive(vendorId);
                updateLeagueStatusToActive(vendorId);
                updateExpiredGameStatus(vendorId);
                updateExpiredLeagueStatus(vendorId);
            });

            page++;
        }
    }*/

   /* @Scheduled(cron = "0 * * * * *")  // Every minute
    public void checkAndActivateScheduledGames() {
        int page = 0;
        int pageSize = 100;
        List<Long> vendorIds;

        // Reuse the same ExecutorService (create it once, not for every cron execution)
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {
            while (!(vendorIds = getActiveVendorIdsInBatch(page, pageSize)).isEmpty()) {
                System.out.println("Scheduled task checkAndActivateScheduledGames started for vendors: " + vendorIds);

                // Submit each vendorId task to the ExecutorService
                for (Long vendorId : vendorIds) {
                    executorService.submit(() -> {
                        try {
                           *//* updateGameAndLeagueStatuses(vendorId);*//*

                            updateGameStatusToActive(vendorId);
                            updateLeagueStatusToActive(vendorId);
                            updateExpiredGameStatus(vendorId);
                            updateExpiredLeagueStatus(vendorId);
                        } catch (Exception e) {
                            // Handle any task-specific exceptions here
                            System.err.println("Error updating status for vendorId " + vendorId + ": " + e.getMessage());
                        }
                    });
                }

                page++;
            }
        } finally {
            // Ensure the executor shuts down gracefully once all tasks are completed
            shutdownExecutorService(executorService);
        }
    }
*/
/*    private void updateAllStatuses(Long vendorId) {
        // Combine your update methods into a single method for efficiency
        updateGameStatusToActive(vendorId);
        updateLeagueStatusToActive(vendorId);
        updateExpiredGameStatus(vendorId);
        updateExpiredLeagueStatus(vendorId);
    }*/



    private void shutdownExecutorService(ExecutorService executorService) {
        executorService.shutdown();  // Initiates an orderly shutdown of the executor
        try {
            // Block indefinitely until all tasks have completed execution
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // If the current thread is interrupted, cancel ongoing tasks
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    public List<Long> getActiveVendorIdsInBatch(int page, int pageSize) {
        LocalDateTime now = LocalDateTime.now();


        String queryString = "SELECT v.id FROM VendorEntity v " +
                "JOIN PaymentEntity p ON p.vendorEntity.id = v.id " +
                "WHERE p.status = :activeStatus " +
                "AND p.expiryAt IS NOT NULL " +
                "AND p.expiryAt > :now " +
                "AND p.expiryAt = (" +
                "   SELECT MAX(p2.expiryAt) " +
                "   FROM PaymentEntity p2 " +
                "   WHERE p2.vendorEntity.id = v.id " +
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
        try {
            // Create a new Game entity
            Game game = new Game();

            boolean isAvailable = isGameAvailableById(existinggameId);

            if (!isAvailable) {
                throw new RuntimeException("game is not available");
            }
//        check from gamerequestgamename that existing game exists or not for same vendor
            Optional<AagAvailableGames> gameAvailable= aagGameRepository.findById(existinggameId);
            game.setImageUrl(gameAvailable.get().getGameImage());

//           game.setName(gameRequest.getName());


            // Fetch Vendor and Theme Entities
            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (vendorEntity == null) {
                throw new RuntimeException("No records found for vendor");
            }
            ThemeEntity theme = em.find(ThemeEntity.class, gameRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
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

           /* if(gameRequest.getMove()!= null){
                game.setMove(gameRequest.getMove());
            }*/


            // Get current time in Kolkata timezone
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            if (gameRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledInKolkata = gameRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The game must be scheduled at least 4 hours in advance.");
                }
                game.setStatus(GameStatus.SCHEDULED);
                game.setScheduledAt(scheduledInKolkata);
                game.setEndDate(scheduledInKolkata.plusHours(4));
            } else {
/*
                game.setStatus(GameStatus.ACTIVE);
*/
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

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the game: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ResponseEntity<?> joinRoom(Long playerId, Long gameId, String gametype) {
        try {
            Player player = getPlayerById(playerId);
            Game game = getGameById(gameId);

            if (isPlayerInRoom(player)) {
                return responseService.generateErrorResponse("Player already in room with this id: " + player.getPlayerId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            GameRoom gameRoom = findAvailableGameRoom(game);

            // 4. Attempt to add the player to the room
            boolean playerJoined = addPlayerToRoom(gameRoom, player);
            if (!playerJoined) {
                return responseService.generateErrorResponse("Room is Already full with this id: " + game.getId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 5. If the room is full, change status to ONGOING and create a new room
            if (gameRoom.getCurrentPlayers().size() == gameRoom.getMaxPlayers()) {
                transitionRoomToOngoing(gameRoom);
                GameRoom newRoom = createNewEmptyRoom(game);
                gameRoomRepository.save(newRoom); // Save the new room
            }


            // 6. Update player status to PLAYING
            updatePlayerStatusToPlaying(player);

            return responseService.generateSuccessResponse("Player join in the Game Room ", game.getId(), HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player can not joined in the room because " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public VendorGameResponse getVendorPublishedGames(Long vendorId) {
        try {
            // Fetch Vendor entity
            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (vendorEntity == null) {
                throw new RuntimeException("No records found for vendor with ID: " + vendorId);
            }

            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime startTime = nowInKolkata.minusHours(24).withZoneSameInstant(ZoneId.of("UTC"));
            ZonedDateTime endTime = nowInKolkata.withZoneSameInstant(ZoneId.of("UTC"));

// Now query the repository
            List<Game> games = gameRepository.findByVendorEntityAndScheduledAtWithin24Hours(vendorEntity, startTime, endTime);

//            List<Game> games = gameRepository.findAll();
                    // Fetch all available games (no vendor filter)
            List<AagAvailableGames> availableGames = aagAvailbleGamesRepository.findAll(); // Fetch all available games

            // Prepare the response data
            VendorGameResponse response = new VendorGameResponse();
            response.setVendorId(vendorEntity.getService_provider_id());
            response.setDailyLimit(vendorEntity.getDailyLimit());
            int dailyUsage = countGamesByVendorIdAndScheduledDate(vendorId, LocalDate.now());
            vendorEntity.setPublishedLimit(dailyUsage);
            response.setPublishedLimit(dailyUsage);

/*
            response.setPublishedLimit(vendorEntity.getPublishedLimit());
*/

            // Map the published games to return relevant details (image, name)
            List<Map<String, String>> publishedGames = games.stream()
                    .map(game -> {
                        Map<String, String> gameMap = new HashMap<>();
                        gameMap.put("imageUrl", game.getTheme().getImageUrl());
                        gameMap.put("name", game.getTheme().getName());
                        return gameMap;
                    })
                    .collect(Collectors.toList());

            response.setPublishedGames(publishedGames);

            // Map the available games to return relevant details (image, name)
            List<Map<String, String>> availableGamesList = availableGames.stream()
                    .map(game -> {
                        Map<String, String> gameMap = new HashMap<>();
                        gameMap.put("gameId", String.valueOf(game.getId()));  // Include gameId
                        gameMap.put("gameImage", game.getGameImage());         // Include game image URL
                        gameMap.put("gameName", game.getGameName());           // Include game name
                        return gameMap;
                    })
                    .collect(Collectors.toList());

            // Add available games to the response
            response.setGames(availableGamesList); // Set available games in the response

            // Return the response
            return response;

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while fetching games: " + e.getMessage(), e);
        }
    }
    


    @Transactional
    public ResponseEntity<?> leaveRoom(Long playerId, Long gameId) {
        try{
            Player player = getPlayerById(playerId);
            Game game = getGameById(gameId);

            if (!isPlayerInRoom(player)) {
                return responseService.generateErrorResponse("Player is not in room with this id: " + player.getPlayerId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            GameRoom gameRoom = player.getGameRoom();
            leftPlayerFromRoom(gameRoom, player);

            return responseService.generateSuccessResponse("Player left the Game Room ", game.getId(), HttpStatus.OK);
        } catch (Exception e){
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player can not left the room because " + e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    private void leftPlayerFromRoom(GameRoom gameRoom, Player player) {
        player.setGameRoom(null);
        player.setHasWon(false);
        player.setPlayerStatus(PlayerStatus.READY_TO_PLAY);
        playerRepository.save(player);
    }

    // Get Player by ID (Ensures player exists)
    private Player getPlayerById(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId));
    }

    // Get Game by ID (Ensures game exists)
    private Game getGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));
    }

    // Check if the player is already in a room
    public boolean isPlayerInRoom(Player player) {
        return player.getGameRoom() != null;
    }


    public GameRoom findAvailableGameRoom(Game game) {
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
    }


    public boolean addPlayerToRoom(GameRoom gameRoom, Player player) {
        // Check if the game room has space for the player
        if (gameRoom.getCurrentPlayers().size() < gameRoom.getMaxPlayers()) {
            gameRoom.getCurrentPlayers().add(player);

            player.setGameRoom(gameRoom);

            player.setPlayerStatus(PlayerStatus.PLAYING);

            gameRoomRepository.save(gameRoom);
            playerRepository.save(player);

            // Return true as the player was successfully added
            return true;
        } else {
            return false;
        }
    }


    // Transition room status to ONGOING when it's full
    private void transitionRoomToOngoing(GameRoom gameRoom) {
        gameRoom.setStatus(GameRoomStatus.ONGOING);
        gameRoomRepository.save(gameRoom);
    }

    // Create a new empty room for a game
    private GameRoom createNewEmptyRoom(Game game) {
        GameRoom newRoom = new GameRoom();
        newRoom.setMaxPlayers(game.getMaxPlayersPerTeam());
        newRoom.setCurrentPlayers(new ArrayList<>());
        newRoom.setStatus(GameRoomStatus.INITIALIZED);
        newRoom.setCreatedAt(LocalDateTime.now());
        newRoom.setRoomCode(generateRoomCode());
        newRoom.setGame(game);
        return newRoom;
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
        player.setPlayerStatus(PlayerStatus.PLAYING);
        playerRepository.save(player);
    }


    private String generateShareableLink(Long gameId) {
        return "https://example.com/games/" + gameId;
    }

    @Transactional
    public Page<GetGameResponseDTO> getAllGames(String status, Long vendorId, Pageable pageable) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM aag_ludo_game g WHERE 1=1");

            if (status != null && !status.isEmpty()) {
                sql.append(" AND g.status = :status");
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

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Game> games = query.getResultList();

            // Map Game entities to GetGameResponseDTO
            List<GetGameResponseDTO> gameResponseDTOs = games.stream()
                    .map(game -> new GetGameResponseDTO(
                            game.getId(),
                            game.getFee(),
                            game.getMove(),
                            game.getStatus(),
                            game.getShareableLink(),
                            game.getAaggameid(),
                            game.getImageUrl(),
                            game.getTheme() != null ? game.getTheme().getName() : null,
                            game.getTheme() != null ? game.getTheme().getImageUrl() : null,
                            game.getScheduledAt() != null ? game.getScheduledAt().toString() : null,
                            game.getEndDate() != null ? game.getEndDate().toString() : null,
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

        String jpql = "SELECT g FROM Game g WHERE g.id = :gameId AND g.vendorEntity.id = :vendorId";
        TypedQuery<Game> query = em.createQuery(jpql, Game.class);
        query.setParameter("gameId", gameId);
        query.setParameter("vendorId", vendorId);

        Game game = query.getResultList().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Game ID: " + gameId + " does not belong to Vendor ID: " + vendorId));


        ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime scheduledAtInKolkata = game.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        if (game.getStatus() == GameStatus.EXPIRED) {
            throw new IllegalStateException("Game ID: " + game.getId() + " has already expired. No update allowed.");
        } else if (game.getStatus() == GameStatus.ACTIVE) {
            throw new IllegalStateException("Game ID: " + game.getId() + " is already active. No update allowed.");
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
                throw new IllegalStateException("Game ID: " + game.getId() + " cannot be updated on the scheduled date or after.");
            }
        } else {
            throw new IllegalStateException("Game ID: " + game.getId() + " does not have a scheduled time.");
        }

    }

    @Transactional
    public void updateExpiredGameStatus(Long vendorId) {
        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            String sql = "SELECT * FROM aag_ludo_game al " +
                    "WHERE al.vendor_id = :vendorId " +
                    "AND al.status = :status";
            String activeStatus = GameStatus.ACTIVE.name();

            Query query = em.createNativeQuery(sql, Game.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("status", activeStatus);

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
                    leagueRepository.save(league);
                    System.out.println("League ID: " + league.getId() + " status updated to EXPIRED.");
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

//    @Transactional
   /* public Page<GameVendorDTO> findGamesByVendor(Long vendorId, String status, Pageable pageable) {
        try {
            String sql = "SELECT g.id AS game_id,  g.scheduled_at, g.created_date, g.updated_date, " +
                    "g.end_date, g.fee, g.move, g.status, g.shareableLink, " +
                    "v.first_name, v.last_name, v.profile_picture, " +
                    "t.id AS theme_id, t.image_url AS theme_image, " +
                    "g.minPlayersPerTeam, g.maxPlayersPerTeam, " +
                    "(CASE WHEN g.scheduled_at <= NOW() AND g.end_date >= NOW() THEN TRUE ELSE FALSE END) AS is_live " +
                    "FROM aag_ludo_game g " +
                    "JOIN vendor_table v ON g.vendor_id = v.service_provider_id " +
                    "LEFT JOIN themes t ON g.theme_id = t.id " +
                    "WHERE g.vendor_id = :vendorId";



            if (status != null && !status.isEmpty()) {
                sql += " AND g.status = :status";
            }

            Query query = em.createNativeQuery(sql);
            query.setParameter("vendorId", vendorId);
            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Object[]> results = query.getResultList();
            List<GameVendorDTO> games = results.stream()
                    .map(obj -> new GameVendorDTO(
                            ((Number) obj[0]).longValue(), // Game ID
                            (String) obj[1], // Game Name
                            obj[2] != null ? convertToKolkataTime((ZonedDateTime) obj[2]) : null, // Scheduled At
                            obj[3] != null ? convertToKolkataTime((ZonedDateTime) obj[3]) : null, // Created Date
                            obj[4] != null ? convertToKolkataTime((ZonedDateTime) obj[4]) : null, // Updated Date
                            obj[5] != null ? convertToKolkataTime((ZonedDateTime) obj[5]) : null, // End Date
                            obj[6] != null ? ((Number) obj[6]).doubleValue() : 0.0, // Fee
                            obj[7] != null ? ((Number) obj[7]).intValue() : 0, // Move
                            (String) obj[8], // Status
                            (String) obj[9], // Shareable Link
                            (String) obj[10], // Vendor First Name
                            (String) obj[11], // Vendor Last Name
                            (String) obj[12], // Vendor Profile Picture
                            (String) obj[13], // Vendor Mobile Number
                            (String) obj[14], // Vendor Email
                            obj[15] != null && (Boolean) obj[15], // Vendor is Verified
                            obj[16] != null ? ((Number) obj[16]).longValue() : null, // Theme ID
                            (String) obj[17], // Theme Image URL
                            obj[18] != null ? ((Number) obj[18]).intValue() : 1, // Min Players Per Team
                            obj[19] != null ? ((Number) obj[19]).intValue() : 2, // Max Players Per Team
                            obj[20] != null && (Boolean) obj[20] // Is Game Live
                    )).collect(Collectors.toList());

            // Count total records for pagination
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
            throw new RuntimeException("Error retrieving games by vendor ID: " + e.getMessage(), e);
        }
    }*/


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



            games.forEach(game -> {
                game.setCreatedDate(convertToKolkataTime(game.getCreatedDate()));
                game.setUpdatedDate(convertToKolkataTime(game.getUpdatedDate()));
                game.setScheduledAt(convertToKolkataTime(game.getScheduledAt()));

            });

            List<GetGameResponseDTO> gameResponseDTOs = games.stream()
                    .map(game -> new GetGameResponseDTO(
                            game.getId(),
                            game.getFee(),
                            game.getMove(),
                            game.getStatus(),
                            game.getShareableLink(),
                            game.getAaggameid(),
                            game.getImageUrl(),
                            game.getTheme() != null ? game.getTheme().getName() : null,
                            game.getTheme() != null ? game.getTheme().getImageUrl() : null,
                            game.getScheduledAt() != null ? game.getScheduledAt().toString() : null,
                            game.getEndDate() != null ? game.getEndDate().toString() : null,
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
                            game.getImageUrl()
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


    private ZonedDateTime convertToKolkataTime(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
    }


    @Transactional
    @Async
    public void updateGameStatusToActive(Long vendorId) {
        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            String sql = "SELECT * " +
                    "FROM aag_league l " +
                    "WHERE l.vendor_id = :vendorId " +
                    "AND l.status = :scheduledStatus";

            Query query = em.createNativeQuery(sql, League.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("scheduledStatus", Constant.SCHEDULED);
/*
            query.setParameter("activeStatus", Constant.ACTIVE);
*/

            List<League> leagues = query.getResultList();

            for (League league : leagues) {

                league.setScheduledAt(convertToKolkataTime(league.getScheduledAt()));

                if (!league.getScheduledAt().isAfter(nowInKolkata)) {
                    league.setStatus(LeagueStatus.ACTIVE);
                    league.setScheduledAt(nowInKolkata);
                    league.setUpdatedDate(nowInKolkata);
                    leagueRepository.save(league);
                    System.out.println("league ID: " + league.getId() + " status updated to ACTIVE.");
                }
            }

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error updating game statuses: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Async
    private void updateLeagueStatusToActive(Long vendorId) {
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

                game.setScheduledAt(convertToKolkataTime(game.getScheduledAt()));

                if (!game.getScheduledAt().isAfter(nowInKolkata)) {
                    game.setStatus(GameStatus.ACTIVE);
                    game.setScheduledAt(nowInKolkata);
                    game.setUpdatedDate(nowInKolkata);
                    gameRepository.save(game);
                    System.out.println("Game ID: " + game.getId() + " status updated to ACTIVE.");
                }
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


/*    public PlayerScore getWinner(List<PlayerScore> playerScores, PostGameRequest postGameRequest) {
        Game game = getGameById(postGameRequest.getGameId());
        PlayerScore winner = null;

        for (PlayerScore playerScore : playerScores) {
            if (winner == null || playerScore.getScore() > winner.getScore()) {
                winner = playerScore;
            }
        }

        if (winner != null) {
            winner.setPrize(game.getPrice());
        }

        return winner;
    }*/
}

