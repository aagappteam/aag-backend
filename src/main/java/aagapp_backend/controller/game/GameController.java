package aagapp_backend.controller.game;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.*;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.NotificationType;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.games.GameLeagueTournamentService;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.payment.PaymentFeatures;
import aagapp_backend.services.tournamnetservice.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.LimitExceededException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping("/games")
public class GameController {
    private GameService gameService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;
    private PlayerRepository playerRepository;
    private GameRoomRepository gameRoomRepository;
    private NotificationRepository notificationRepository;
    private LeagueService leagueService;
    private TournamentService tournamentService;
    private GameLeagueTournamentService gameleaguetournamentservice;
    @Autowired
    public void setGameLeagueTournamentService(@Lazy GameLeagueTournamentService gameleaguetournamentservice) {
        this.gameleaguetournamentservice = gameleaguetournamentservice;
    }

    @Autowired
    public void setLeagueService(@Lazy LeagueService leagueService) {
        this.leagueService = leagueService;
    }
    @Autowired
    public void setTournamentService(@Lazy TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }


    @Autowired
    public void setNotificationRepository(@Lazy NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Autowired
    public void setGameService(@Lazy GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    public void setPlayerRepository(@Lazy PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Autowired
    public void setGameRoomRepository(@Lazy GameRoomRepository gameRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setPaymentFeatures(@Lazy PaymentFeatures paymentFeatures) {
        this.paymentFeatures = paymentFeatures;
    }

    @GetMapping("/get-all-games")
    public ResponseEntity<?> getAllGames(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "vendorId", required = false) Long vendorId) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            // Assuming your service returns Page<GetGameResponseDTO>
            Page<GetGameResponseDTO> games = gameService.getAllGames(status, vendorId, pageable);

            // Extract only the content (list of games) and return it
            List<GetGameResponseDTO> gameList = games.getContent();
            long totalCount = games.getTotalElements();

            return responseService.generateSuccessResponseWithCount("Games fetched successfully", gameList, totalCount, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-games-by-vendor/{vendorId}")
    public ResponseEntity<?> getGamesByVendorId(@PathVariable Long vendorId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(value = "status", required = false) String status) {
        try {

            if (page < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Size must be between 1 and 100");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Game> gamesPage = gameService.findGamesByVendor(vendorId, status, pageable);
            return responseService.generateSuccessResponse("Games fetched successfully", gamesPage, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);

            return responseService.generateErrorResponse("Error fetching games by vendor : " + e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/get-active-games-by-vendor/{vendorId}")
    public ResponseEntity<?> getActiveGamesByVendorId(@PathVariable Long vendorId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            if (page < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Size must be between 1 and 100");
            }

            Pageable pageable = PageRequest.of(page, size);

            // Call the updated service method
            Page<GetGameResponseDTO> gamesPage = gameService.findGamesScheduledForToday(vendorId, pageable);

            return responseService.generateSuccessResponse("Games fetched successfully", gamesPage, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching games by vendor : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/publishGame/{vendorId}/{existinggameId}")
    public ResponseEntity<?> publishGame(@PathVariable Long vendorId, @PathVariable Long existinggameId, @RequestBody GameRequest gameRequest) {
        try {

            ResponseEntity<?> paymentEntity = paymentFeatures.canPublishGame(vendorId);

            if (paymentEntity.getStatusCode() != HttpStatus.OK) {
                return paymentEntity;
            }

            Game publishedGame = gameService.publishLudoGame(gameRequest, vendorId, existinggameId);


            // Now create a single notification for the vendor
            Notification notification = new Notification();
            notification.setRole("Vendor");

            notification.setVendorId(vendorId);
            if (gameRequest.getScheduledAt() != null) {
/*
                notification.setType(NotificationType.GAME_SCHEDULED);  // Example NotificationType for a successful payment
*/
                notification.setDescription("Scheduled Game"); // Example NotificationType for a successful
                notification.setDetails("Game has been Scheduled"); // Example NotificationType for a successful
            }else{
/*
                notification.setType(NotificationType.GAME_PUBLISHED);  // Example NotificationType for a successful payment
*/
                notification.setDescription("Published Game"); // Example NotificationType for a successful
                notification.setDetails("Game has been Published"); // Example NotificationType for a successful
            }



            notificationRepository.save(notification);

            if (gameRequest.getScheduledAt() != null) {
                return responseService.generateSuccessResponse("Game scheduled successfully", publishedGame, HttpStatus.CREATED);
            } else {
                return responseService.generateSuccessResponse("Game published successfully", publishedGame, HttpStatus.CREATED);
            }
        } catch (LimitExceededException e) {
            return responseService.generateErrorResponse("Exceeded maximum allowed games ", HttpStatus.TOO_MANY_REQUESTS);

        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("Required entity not found " + e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);

            return responseService.generateErrorResponse("Invalid game data " + e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error publishing game" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{vendorId}/{gameId}")
    public ResponseEntity<?> updateStatuses(@PathVariable Long vendorId, @PathVariable Long gameId, @RequestBody GameRequest gameRequest) {
        try {
            ResponseEntity<?> response = gameService.updateGame(vendorId, gameId, gameRequest);

            return ResponseService.generateSuccessResponse("Game updated successfully", gameRequest, HttpStatus.OK);

        } catch (IllegalStateException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating game details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/joinRoom")
    public ResponseEntity<?> joinGameRoom(@RequestBody JoinRoomRequest joinRoomRequest) {
        try {
            return gameService.joinRoom(joinRoomRequest.getPlayerId(), joinRoomRequest.getGameId(), joinRoomRequest.getGametype());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in joining game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/leftGameRoom")
    public ResponseEntity<?> leaveGameRoom(@RequestBody LeaveRoomRequest leaveRoomRequest) {
        try {
            return gameService.leaveRoom(leaveRoomRequest.getPlayerId(), leaveRoomRequest.getGameId());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in leaving game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //    get room by id

    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getRoomById(@PathVariable Long roomId) {
        try {
            return gameService.getRoomById(roomId);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in getting game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/published/{vendorId}")
    public ResponseEntity<?> getAllPublishedGamesByVendorId(@PathVariable Long vendorId) {
        try {
            // Call the service method to get the vendor and published game details
            VendorGameResponse vendorGameResponse = gameService.getVendorPublishedGames(vendorId);

            // Directly pass the vendorGameResponse to generateSuccessResponse
            return responseService.generateSuccessResponse("Games fetched successfully", vendorGameResponse, HttpStatus.OK);

        } catch (RuntimeException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error fetching games by vendor: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to handle game completion and winner calculation
   /* @PostMapping("/game-completed")
    public Map<String, Object> gameCompleted(@RequestBody PostGameRequest postGameRequest) {

        // Fetch game details using gameId (e.g., prize for the game)

        // Calculate the winner based on player scores
        PlayerScore winner = gameService.getWinner(postGameRequest.getPlayers(), postGameRequest);

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        response.put("winner", winner);
        response.put("players", postGameRequest.getPlayers());
        response.put("roomId", postGameRequest.getRoomId());

        return response;
    }*/
    @GetMapping("/get-all-scheduled-events/{vendorId}")
    public ResponseEntity<?> getAllScheduledEvents(
            @PathVariable Long vendorId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "events", required = false) String events,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "scheduleddate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleddate) {

        try {
            // Validate required parameters


            if (events == null || events.trim().isEmpty()) {
                return responseService.generateErrorResponse("The 'events' parameter is required.", HttpStatus.BAD_REQUEST);
            }

            // Initialize pageable object for pagination
            Pageable pageable = PageRequest.of(page, size);
            Map<String, Object> response = new HashMap<>();

            // Handle event filtering based on events type
            switch (events.toLowerCase()) {
                case "game":
                    // Filter for 'game' only
                    Page<GetGameResponseDTO> games = gameleaguetournamentservice.getAllGames(status, vendorId, pageable, startDate, endDate, scheduleddate);
                    response.put("games", games.getContent());
                    break;

                case "league":
                    // Filter for 'league' only
                    Page<LeagueResponseDTO> leagues = gameleaguetournamentservice.getAllLeagues(status,vendorId, pageable, startDate, endDate, scheduleddate);
                    response.put("leagues", leagues.getContent());
                    break;

                case "tournament":
                    // Filter for 'tournament' only
                    Page<TournamentResponseDTO> tournaments = gameleaguetournamentservice.getAllTournaments(status,vendorId, pageable, startDate, endDate, scheduleddate);
                    response.put("tournaments", tournaments.getContent());
                    break;

                case "league_tournament":
                    // Combine filter for 'league' and 'tournament'
                    Map<String, Object> leagueAndTournament = new HashMap<>();
                    Page<LeagueResponseDTO> allLeagues = gameleaguetournamentservice.getAllLeagues(status,vendorId, pageable, startDate, endDate, scheduleddate);
                    Page<TournamentResponseDTO> allTournaments = gameleaguetournamentservice.getAllTournaments(status,vendorId, pageable, startDate, endDate, scheduleddate);

                    leagueAndTournament.put("leagues", allLeagues.getContent());
                    leagueAndTournament.put("tournaments", allTournaments.getContent());
                    response.put("league_tournament", leagueAndTournament);
                    break;

                default:
                    return responseService.generateErrorResponse("Invalid 'events' value. Allowed values are: game, league, tournament, league_tournament.", HttpStatus.BAD_REQUEST);
            }

            return responseService.generateSuccessResponse("Scheduled events fetched successfully", response, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    13.232.105.87:8082


}