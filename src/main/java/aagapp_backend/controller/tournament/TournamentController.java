package aagapp_backend.controller.tournament;

import aagapp_backend.dto.*;
import aagapp_backend.dto.tournament.MatchResultRequest;
import aagapp_backend.dto.tournament.TournamentJoinRequest;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.*;
import aagapp_backend.enums.LeagueRoomStatus;
import aagapp_backend.enums.TournamentStatus;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.tournament.TournamentPlayerRegistrationRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.tournament.TournamentResultRecordRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PaymentFeatures;
import aagapp_backend.services.tournamnetservice.TournamentService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.LimitExceededException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tournament")
public class TournamentController {

    private ResponseService responseService;
    private ExceptionHandlingImplement exceptionHandling;
    private PaymentFeatures paymentFeatures;
    private TournamentService tournamentService;
    private NotificationRepository notificationRepository;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TournamentResultRecordRepository tournamentResultRecordRepository;
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentRoomRepository tournamentRoomRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    private TournamentPlayerRegistrationRepository tournamentPlayerRegistration;


    @Autowired
    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    @Autowired
    public void setTournamentService(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }


    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }
    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }
    @Autowired
    public void setPaymentFeatures(@Lazy PaymentFeatures paymentFeatures) {
        this.paymentFeatures = paymentFeatures;
    }

    @GetMapping("/get-all-Tournaments")
    public ResponseEntity<?> getAllGames(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) List<TournamentStatus> status,
            @RequestParam(value = "vendorId", required = false) Long vendorId) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Tournament> games = tournamentService.getAllTournaments(pageable, status, vendorId);

            List<Tournament> gameList = games.getContent();
            long totalCount = games.getTotalElements();

            return responseService.generateSuccessResponseWithCount("Tournaments fetched successfully", gameList, totalCount, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-registered-players-by-tournament/{tournamentId}/{playerId}")
    public ResponseEntity<?> getRegisteredPlayersByTournamentId(@PathVariable Long tournamentId, @PathVariable Long playerId) {
        try {
            Boolean registeredPlayers = tournamentService.findTournamentResultRecordByTournamentIdAndPlayerId(tournamentId, playerId);
            return responseService.generateSuccessResponse("Players fetched successfully", registeredPlayers, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-tournament-by-vendor/{vendorId}")
    public ResponseEntity<?> getGamesByVendorId(@PathVariable Long vendorId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(value = "status", required = false) String status) {
        try {

            if (page < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Size must be between 1 and 100");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Tournament> gamesPage = tournamentService.findTournamentsByVendor(pageable, vendorId);
            return responseService.generateSuccessResponse("Tournament fetched successfully", gamesPage.getContent(), HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);

            return responseService.generateErrorResponse("Error fetching Tournament by vendor : " + e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/get-active-tournament-by-vendor/{vendorId}")
    public ResponseEntity<?> getActiveTournamentsByVendorId(@PathVariable Long vendorId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            if (page < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Size must be between 1 and 100");
            }

            Pageable pageable = PageRequest.of(page, size);

            // Call the updated service method
            Page<Tournament> gamesPage = tournamentService.getAllActiveTournamentsByVendor(pageable,vendorId);

            return responseService.generateSuccessResponse("Tournaments fetched successfully", gamesPage.getContent(), HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching Tournaments by vendor : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/publishTournament/{vendorId}")
    public ResponseEntity<?> publishGame(@PathVariable Long vendorId, @RequestBody TournamentRequest tournamentRequest) {
        try {

            ResponseEntity<?> paymentEntity = paymentFeatures.canPublishGame(vendorId);

            if (paymentEntity.getStatusCode() != HttpStatus.OK) {
                return paymentEntity;
            }

           Tournament publishedGame = tournamentService.publishTournament(tournamentRequest, vendorId);

            // Now create a single notification for the vendor
            Notification notification = new Notification();
            notification.setRole("Vendor");

            notification.setVendorId(vendorId);
            if (tournamentRequest.getScheduledAt() != null) {
//                notification.setType(NotificationType.GAME_SCHEDULED);


                notification.setDescription("Scheduled Tournament");
                notification.setDetails("Tournament has been Scheduled");
            }else{
//                notification.setType(NotificationType.GAME_PUBLISHED);

                notification.setDescription("Published Tournament");
                notification.setDetails("Tournament has been Published");
            }


            notificationRepository.save(notification);

            if (tournamentRequest.getScheduledAt() != null) {
                return responseService.generateSuccessResponse("Tournament scheduled successfully", publishedGame, HttpStatus.CREATED);
            } else {
                return responseService.generateSuccessResponse("Tournament published successfully", publishedGame, HttpStatus.CREATED);
            }
        }catch (BusinessException e){
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (LimitExceededException e) {
            return responseService.generateErrorResponse("Exceeded maximum allowed Tournaments ", HttpStatus.TOO_MANY_REQUESTS);

        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("Required entity not found " + e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);

            return responseService.generateErrorResponse("Invalid game data " + e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error publishing Tournaments" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register/{tournamentId}/{playerId}")
    public ResponseEntity<?> registerPlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long playerId) {
        try {

            TournamentPlayerRegistration registration = tournamentService.registerPlayer(tournamentId, playerId);
            return responseService.generateSuccessResponse("Player registered successfully", registration, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse("Error registering player: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/players/{tournamentId}")
    public ResponseEntity<?> getRegisteredPlayers(
            @PathVariable Long tournamentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Page<Player> playersPage = tournamentService.getRegisteredPlayers(tournamentId, page, size);

            long totalCount = playersPage.getTotalElements();
            List<Player> gameList = playersPage.getContent();

//            return ResponseService.generateSuccessResponseWithCount("List of vendors", vendorDetailList, totalCount, HttpStatus.OK);
            return responseService.generateSuccessResponseWithCount("Players fetched successfully", gameList,totalCount, HttpStatus.OK);

//            return responseService.generateSuccessResponse("Players fetched successfully", gameList, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse("Error fetching players: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


        @GetMapping("/tournament/{tournamentId}")
        public ResponseEntity<?> getTournamentProgress(@PathVariable Long tournamentId) {
            List<Map<String, Object>> rounds = tournamentService.generateTournamentRounds(tournamentId);

            Map<String, Object> response = new HashMap<>();
            response.put("status_code", 200);
            response.put("status", "success");
            response.put("message", "Tournament Progress");
            response.put("rounds", rounds);

            return ResponseEntity.ok(response);
        }


    @PostMapping("/startTournament/{tournamentId}")
    public ResponseEntity<?> startTournament(@PathVariable Long tournamentId) {
        try {
          Tournament tournament=  tournamentService.startTournament(tournamentId);
            return responseService.generateSuccessResponse("🏆 Tournament Started Successfully!",tournament,HttpStatus.OK);

        }catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting tournament: " + e.getMessage());
        }
    }

    @GetMapping("/get-rooms-by-tournament/{tournamentId}")
    public ResponseEntity<?> getRoomsByTournamentId(@PathVariable Long tournamentId) {
        try {
            List<TournamentRoom> rooms = tournamentService.getAllRoomsByTournamentId(tournamentId);
            return responseService.generateSuccessResponse("Rooms fetched successfully", rooms, HttpStatus.OK);
        }catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching rooms: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/join-room")
    public ResponseEntity<?> joinTournament(@RequestBody TournamentJoinRequest tournamentJoinRequest ) {

        try {
            Long tournamentId = tournamentJoinRequest.getGameId();
            Long playerId = tournamentJoinRequest.getPlayerId();

            if (tournamentId == null || playerId == null) {
                return ResponseEntity.badRequest().body("Tournament ID and Player ID are required.");
            }

            TournamentPlayerRegistration registration = tournamentPlayerRegistration
                    .findByTournamentIdAndPlayer_PlayerId(tournamentId, playerId)
                    .orElseThrow(() -> new RuntimeException("Player not registered for this tournament"));

            if (registration.getStatus() == TournamentPlayerRegistration.RegistrationStatus.CANCELLED) {
                return ResponseEntity.badRequest().body("Player cancelled registration earlier.");
            }

            if (registration.getStatus() == TournamentPlayerRegistration.RegistrationStatus.ACTIVE) {
                return ResponseEntity.badRequest().body("Player is already active in the tournament.");
            }

            registration.setStatus(TournamentPlayerRegistration.RegistrationStatus.ACTIVE);
            entityManager.merge(registration);
            return responseService.generateSuccessResponse("Player successfully joined the tournament room.",registration,HttpStatus.OK);
        }
        catch (BusinessException ex) {
            exceptionHandling.handleException(ex);

            return responseService.generateErrorResponse("Error joining tournament room: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error joining tournament room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



/*    @PostMapping("/join-room")
    public ResponseEntity<?> joinTournament(@RequestBody TournamentJoinRequest tournamentJoinRequest ) {

        Long tournamentId = tournamentJoinRequest.getGameId();
        Long playerId = tournamentJoinRequest.getPlayerId();

        if (tournamentId == null || playerId == null) {
            return ResponseEntity.badRequest().body("Tournament ID and Player ID are required.");
        }


        TournamentPlayerRegistration registration = tournamentPlayerRegistration
                .findByTournamentIdAndPlayer_PlayerId(tournamentId, playerId)
                .orElseThrow(() -> new RuntimeException("Player not registered for this tournament"));

        if (registration.getStatus() == TournamentPlayerRegistration.RegistrationStatus.CANCELLED) {
            return ResponseEntity.badRequest().body("Player cancelled registration earlier.");
        }

        registration.setStatus(TournamentPlayerRegistration.RegistrationStatus.ACTIVE);
        entityManager.persist(registration);
        return ResponseEntity.ok("Player successfully joined the tournament room.");
    }*/



    @PostMapping("/leave-room")
    public ResponseEntity<?> leaveTournamentRoom(@RequestBody TournamentJoinRequest tournamentJoinRequest) {


        Long tournamentId = tournamentJoinRequest.getGameId();
        Long playerId = tournamentJoinRequest.getPlayerId();

        if (tournamentId == null || playerId == null) {
            return ResponseEntity.badRequest().body("Tournament ID and Player ID are required.");
        }

        // Delegate the request to the existing TournamentController
        return tournamentService.leaveRoom(playerId, tournamentId);
    }

    @GetMapping("/my-opponent/{playerId}/{tournamentId}")
    public ResponseEntity<?> getMyRoom(@PathVariable Long playerId, @PathVariable Long tournamentId) {
        try {
            TournamentRoom room = tournamentService.getMyRoomDetails(playerId, tournamentId);
            return responseService.generateSuccessResponse("Tournament Room fetched successfully", room, HttpStatus.OK);
        }catch (BusinessException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error fetching tournament room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to add a player to a round
    @PostMapping("/process-next-round")
    public ResponseEntity<?> processNextRound(
            @RequestParam Long tournamentId,
            @RequestParam Integer roundNumber) {
        try {
            tournamentService.processNextRoundMatches(tournamentId, roundNumber);

            return responseService.generateSuccessResponse("Next round processing completed.", null, HttpStatus.OK);
        } catch (RuntimeException e) {
            return responseService.generateErrorResponse("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /*@GetMapping("/waiting-room-status")
    public ResponseEntity<?> getWaitingRoomStatus(
            @RequestParam Long tournamentId,
            @RequestParam Integer roundNumber) {
        try {
            // 1. Get all READY_TO_PLAY players in this round
            List<TournamentResultRecord> readyPlayers = tournamentService
                    .getReadyPlayersByTournamentAndRound(tournamentId, roundNumber);
            int waitingCount = readyPlayers.size();

            // 2. Fetch all rooms from previous round
            List<TournamentRoom> previousRoundRooms = tournamentRoomRepository
                    .findByTournamentIdAndRound(tournamentId, roundNumber - 1);

            boolean allRoomsCompleted = previousRoundRooms.stream()
                    .allMatch(room -> room.getStatus().equalsIgnoreCase("COMPLETED"));

            // 4. Calculate expected players:
            // - One winner per completed room
            // - Add free pass count (assumed to be stored as FREE_PASS in TournamentResultRecord)
            long completedRoomCount = previousRoundRooms.stream()
                    .filter(room -> room.getStatus().equalsIgnoreCase("COMPLETED"))
                    .count();

            long freePassCount = tournamentResultRecordRepository
                    .countFreePassWinners(tournamentId, roundNumber); // You must implement this method

            long expectedPlayers = completedRoomCount + freePassCount;

            // 5. Decide if next round can be started
            boolean canStartNextRound = allRoomsCompleted && waitingCount == expectedPlayers && expectedPlayers > 0;

            Map<String, Object> response = new HashMap<>();
            response.put("waitingCount", waitingCount);
            response.put("expectedPlayers", expectedPlayers);
            response.put("freePassCount", freePassCount);
            response.put("players", readyPlayers);
            response.put("canStartNextRound", canStartNextRound);

            return responseService.generateSuccessResponse("Waiting room status fetched", response, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error occurred while retrieving waiting status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

   /* @GetMapping("/waiting-room-status")
    public ResponseEntity<?> getWaitingRoomStatus(
            @RequestParam Long tournamentId,
            @RequestParam Integer roundNumber) {
        try {
            // 1. Get all READY_TO_PLAY players in this round
            List<TournamentResultRecord> readyPlayers = tournamentService
                    .getPlayersForNextRound(tournamentId, roundNumber-1);
            int waitingCount = readyPlayers.size();

            // 2. Fetch all rooms from the current and previous rounds
            List<TournamentRoom> previousRoundRooms = tournamentRoomRepository
                    .findByTournamentIdAndRound(tournamentId, roundNumber - 1);

            List<TournamentRoom> currentRoundRooms = tournamentRoomRepository
                    .findByTournamentIdAndRound(tournamentId, roundNumber);

            boolean allPreviousRoundRoomsCompleted = previousRoundRooms.stream()
                    .allMatch(room -> room.getStatus().equalsIgnoreCase("COMPLETED"));

            boolean allRoundsCompleted = currentRoundRooms.stream()
                    .allMatch(room -> room.getStatus().equalsIgnoreCase("COMPLETED"));

            // 4. Calculate expected players:
            // - One winner per completed room
            // - Add free pass count (assumed to be stored as FREE_PASS in TournamentResultRecord)
            long completedRoomCount = previousRoundRooms.stream()
                    .filter(room -> room.getStatus().equalsIgnoreCase("COMPLETED"))
                    .count();

            long freePassCount = tournamentResultRecordRepository
                    .countFreePassWinners(tournamentId, roundNumber); // You must implement this method

            long expectedPlayers = completedRoomCount + freePassCount;

            boolean canStartNextRound = allPreviousRoundRoomsCompleted && waitingCount == expectedPlayers && expectedPlayers > 0;

            boolean playAgainEnabled = allRoundsCompleted || allPreviousRoundRoomsCompleted;

            Map<String, Object> response = new HashMap<>();
            response.put("waitingCount", waitingCount);
            response.put("expectedPlayers", expectedPlayers);
            response.put("freePassCount", freePassCount);
            response.put("players", readyPlayers);
            response.put("canStartNextRound", canStartNextRound);
            response.put("playAgainEnabled", playAgainEnabled);  // Add this flag to the response

            return responseService.generateSuccessResponse("Waiting room status fetched", response, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error occurred while retrieving waiting status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
   @GetMapping("/waiting-room-status")
   public ResponseEntity<?> getWaitingRoomStatus(
           @RequestParam Long tournamentId,
           @RequestParam Integer roundNumber) {
       try {
//           fetch tournmanet exists or not
           Tournament tournament = entityManager.find(Tournament.class,tournamentId);
           if (tournament==null){
               return responseService.generateErrorResponse("Tournament not found.", HttpStatus.BAD_REQUEST);

           }
           // 1. Get all players who are winners or have a free pass from the previous round
           List<TournamentResultRecord> readyPlayers = tournamentService
                   .getPlayersForNextRound(tournamentId, roundNumber - 1);  // Fetch players for the previous round
           int waitingCount = readyPlayers.size();

           // 2. Fetch all rooms from the current and previous rounds
           List<TournamentRoom> previousRoundRooms = tournamentRoomRepository
                   .findByTournamentIdAndRound(tournamentId, roundNumber - 1);

           List<TournamentRoom> currentRoundRooms = tournamentRoomRepository
                   .findByTournamentIdAndRound(tournamentId, roundNumber);

           // Handle cases where no previous round rooms are created
           if (previousRoundRooms.isEmpty()) {
               // If no previous round rooms exist, it means no tournament progression has happened
               return responseService.generateErrorResponse("No previous round rooms found. Tournament may not have started.", HttpStatus.BAD_REQUEST);
           }

           // Handle the case where only one player exists (already a winner)
           if (waitingCount == 1) {
               // If only one player is left and they've won, consider the tournament as completed
               return responseService.generateSuccessResponse("Tournament completed with 1 winner.", null, HttpStatus.OK);
           }

           boolean allPreviousRoundRoomsCompleted = previousRoundRooms.stream()
                   .allMatch(room -> room.getStatus().equalsIgnoreCase("COMPLETED"));

           boolean allRoundsCompleted = currentRoundRooms.stream()
                   .allMatch(room -> room.getStatus().equalsIgnoreCase("COMPLETED"));

           long completedRoomCount = previousRoundRooms.stream()
                   .filter(room -> room.getStatus().equalsIgnoreCase("COMPLETED"))
                   .count();

           long freePassCount = tournamentResultRecordRepository
                   .countFreePassWinners(tournamentId, roundNumber); // You must implement this method

           long expectedPlayers = completedRoomCount + freePassCount;

           // 5. Decide if next round can be started
           boolean canStartNextRound = allPreviousRoundRoomsCompleted && waitingCount == expectedPlayers && expectedPlayers > 0;

           // 6. Enable Play Again button if all rounds are completed
           boolean playAgainEnabled = allRoundsCompleted || allPreviousRoundRoomsCompleted;

           Map<String, Object> response = new HashMap<>();
           response.put("tournament_status",tournament.getStatus());
           response.put("waitingCount", waitingCount);
           response.put("expectedPlayers", expectedPlayers);
           response.put("freePassCount", freePassCount);
           response.put("players", readyPlayers);
           response.put("canStartNextRound", canStartNextRound);
           response.put("playAgainEnabled", playAgainEnabled);

           return responseService.generateSuccessResponse("Waiting room status fetched", response, HttpStatus.OK);

       } catch (Exception e) {
           exceptionHandling.handleException(e);
           return responseService.generateErrorResponse("Error occurred while retrieving waiting status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
   }



    // Endpoint to retrieve players by tournamentId and roundNumber
    @GetMapping("/players-list")
    public ResponseEntity<?> getPlayersInTournamentRound(
            @RequestParam Long tournamentId,
            @RequestParam(value = "iswinner", required = false) Boolean iswinner,
            @RequestParam Integer roundNumber) {

            List<TournamentResultRecord> players = tournamentService.getPlayersByTournamentAndRound(tournamentId, roundNumber,iswinner);
            return responseService.generateSuccessResponse("Players retrieved successfully", players, HttpStatus.OK);

    }

    @PostMapping("/add-player-next-round")
    public ResponseEntity<?> playAgain(
            @RequestParam Long tournamentId,
            @RequestParam Integer roundNumber,
            @RequestParam Long playerId
    ) {
        try {

            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Tournament not found"));

            if (tournament.getRound() > roundNumber) {
                return responseService.generateErrorResponse(
                        "Too late to join this round. You are disqualified.", HttpStatus.FORBIDDEN);
            }

            Optional<TournamentResultRecord> record = tournamentResultRecordRepository.findWinnerRecord(
                    tournamentId, playerId, roundNumber-1);

            if (record.isEmpty()) {
                return responseService.generateErrorResponse(
                        "You are not a winner or eligible for next round", HttpStatus.FORBIDDEN);
            }

            TournamentResultRecord nextRoundRecord = tournamentService.addPlayerToNextRound(
                    tournamentId,
                    roundNumber,
                    record.get());

            return responseService.generateSuccessResponse(
                    "Player added to next round.", nextRoundRecord, HttpStatus.OK);
        }catch (BusinessException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error occurred while retrieving waiting status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    // Endpoint to create rooms for players (called when creating rooms for tournament and round)
/*    @PostMapping("/create-rooms")
    public ResponseEntity<?> createRoomsForTournament(
            @RequestParam Long tournamentId,    // Tournament ID (which tournament to create rooms for)
            @RequestParam Integer roundNumber   // Round number (which round to create rooms for)
    ) {
        try {
            // Call the service method to create rooms for players in this tournament round
            List<TournamentRoom> createdRooms = tournamentService.createRoomsForPlayersNextRoundAndAssign(tournamentId, roundNumber);
            // Return a success message with status 200 OK
            return responseService.generateSuccessResponse("Rooms created successfully for Tournament " + tournamentId + " and Round " + roundNumber, createdRooms, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error occurred while creating rooms: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @PostMapping("/update-match")
    public ResponseEntity<?> updateMatchResult(@RequestBody  GameResult gameResult) {
        try{
            tournamentService.processMatchResults(gameResult);

            return ResponseEntity.ok("Match result updated");
        }
        catch (BusinessException e) {
            // Handle BusinessException here
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
