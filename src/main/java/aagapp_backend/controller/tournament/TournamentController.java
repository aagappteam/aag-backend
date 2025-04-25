package aagapp_backend.controller.tournament;

import aagapp_backend.dto.GameRoomResponseDTO;
import aagapp_backend.dto.TournamentRequest;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.tournament.TournamentPlayerRegistration;
import aagapp_backend.entity.tournament.TournamentRoom;
import aagapp_backend.entity.tournament.TournamentRoundWinner;
import aagapp_backend.enums.LeagueRoomStatus;
import aagapp_backend.enums.TournamentStatus;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PaymentFeatures;
import aagapp_backend.services.tournamnetservice.TournamentService;
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
    private TournamentRoomRepository tournamentRoomRepository;

    @Autowired
    PlayerRepository playerRepository;



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
            @RequestParam(value = "status", required = false) TournamentStatus status,
            @RequestParam(value = "vendorId", required = false) Long vendorId) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            // Assuming your service returns Page<GetGameResponseDTO>
            Page<Tournament> games = tournamentService.getAllTournaments(pageable, status, vendorId);

            // Extract only the content (list of games) and return it
            List<Tournament> gameList = games.getContent();
            long totalCount = games.getTotalElements();

            return responseService.generateSuccessResponseWithCount("Tournament fetched successfully", gameList, totalCount, HttpStatus.OK);
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
//                notification.setType(NotificationType.GAME_SCHEDULED);  // Example NotificationType for a successful payment


                notification.setDescription("Scheduled Tournament"); // Example NotificationType for a successful
                notification.setDetails("Tournament has been Scheduled"); // Example NotificationType for a successful
            }else{
//                notification.setType(NotificationType.GAME_PUBLISHED);  // Example NotificationType for a successful payment


                notification.setDescription("Published Tournament"); // Example NotificationType for a successful
                notification.setDetails("Tournament has been Published"); // Example NotificationType for a successful
            }


            notificationRepository.save(notification);

            if (tournamentRequest.getScheduledAt() != null) {
                return responseService.generateSuccessResponse("Tournament scheduled successfully", publishedGame, HttpStatus.CREATED);
            } else {
                return responseService.generateSuccessResponse("Tournament published successfully", publishedGame, HttpStatus.CREATED);
            }
        } catch (LimitExceededException e) {
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
    public ResponseEntity<?> getRegisteredPlayers(@PathVariable Long tournamentId) {
        try {
            //list of all players
            List<Player> players = tournamentService.getRegisteredPlayers(tournamentId);
            return responseService.generateSuccessResponse("Players fetched successfully", players, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse("Error fetching players: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/startTournament/{tournamentId}")
    public ResponseEntity<?> startTournament(@PathVariable Long tournamentId) {
        try {
            tournamentService.startTournament(tournamentId);
            return ResponseEntity.ok("🏆 Tournament Started Successfully!");
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
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching rooms: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Join a Room
    @PostMapping("/join-room/{tournamentId}")
    public ResponseEntity<?> joinRoom(
            @PathVariable Long tournamentId,
            @RequestParam Long playerId) {

        try {

            TournamentRoom roomDetails = tournamentService.assignPlayerToRoom(playerId, tournamentId);
            return responseService.generateSuccessResponse("Player joined room successfully", roomDetails, HttpStatus.OK);
        }catch (RuntimeException e) {
            return responseService.generateErrorResponse("Error in joining rooms : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error in joining rooms : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/leave-room/{playerId}/{tournamentId}")
    public ResponseEntity<?> leaveTournamentRoom(@PathVariable Long playerId, @PathVariable Long tournamentId) {
        // Delegate the request to the existing TournamentController
        return tournamentService.leaveRoom(playerId, tournamentId);
    }


    // Endpoint to add a player to a round
    @PostMapping("/add-player-next-round")
    public ResponseEntity<?> addPlayerToRound(
            @RequestParam Long tournamentId,
            @RequestParam Long playerId,
            @RequestParam Integer roundNumber) {
        try {
            TournamentRoundWinner tournamentRoundWinner = tournamentService.addPlayerToRound(tournamentId, playerId, roundNumber);
            return responseService.generateSuccessResponse("Player added successfully to the round", tournamentRoundWinner, HttpStatus.OK);
        } catch (RuntimeException e) {
            return responseService.generateErrorResponse("Error adding player to the round: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error occurred while adding player to the round: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to retrieve players by tournamentId and roundNumber
    @GetMapping("/players-list")
    public ResponseEntity<?> getPlayersInTournamentRound(
            @RequestParam Long tournamentId,
            @RequestParam Integer roundNumber) {
        try {
            List<TournamentRoundWinner> players = tournamentService.getPlayersByTournamentAndRound(tournamentId, roundNumber);
            return responseService.generateSuccessResponse("Players retrieved successfully", players, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error occurred while retrieving players: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to create rooms for players (called when creating rooms for tournament and round)
    @PostMapping("/create-rooms")
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
    }

    /*@GetMapping("/active-game-rooms")
    public ResponseEntity<?> getAllActiveGameRooms() {
        // Fetch all GameRooms with status ONGOING
        List<TournamentRoom> ongoingRooms = tournamentRoomRepository.findByStatus("ACTIVE");

        // Map the GameRoom entities to GameRoomResponseDTO
        List<GameRoomResponseDTO> gameRoomResponseDTOS = ongoingRooms.stream().map(gameRoom -> {
            Integer maxParticipants = gameRoom.getMaxParticipants();
            Long gameId = gameRoom.ge().getId();
            String gamePassword = gameRoom.getGamepassword();
            Integer moves = gameRoom.get().getMove();  // Assuming moves is stored in the Game entity

            BigDecimal totalPrize = matchService.getWinningAmountLeague(gameRoom);  // Assuming matchService calculates the total prize

            return new GameRoomResponseDTO(gameId, gameRoom.getId(), gamePassword, moves, totalPrize, maxParticipants);
        }).collect(Collectors.toList());

        // Return the response wrapped in a success response
        return responseService.generateSuccessResponse("Fetching active game rooms from all leagues", gameRoomResponseDTOS, HttpStatus.OK);
    }*/

}
