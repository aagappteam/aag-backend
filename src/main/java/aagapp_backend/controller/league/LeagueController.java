package aagapp_backend.controller.league;

import aagapp_backend.dto.*;
import aagapp_backend.entity.Challenge;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.enums.LeagueRoomStatus;
import aagapp_backend.enums.LeagueStatus;

import aagapp_backend.repository.ChallangeRepository;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.league.LeagueRoomRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PaymentFeatures;
import aagapp_backend.services.pricedistribute.MatchService;
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
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/leagues")
public class LeagueController {

    private LeagueService leagueService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;
    private VendorRepository vendorRepository;
    private NotificationRepository notificationRepository;
    private ChallangeRepository challangeRepository;
    @Autowired
    private LeagueRoomRepository leagueRoomRepository;
    @Autowired
    private MatchService matchService;

    @Autowired
    public void setChallangeRepository(@Lazy ChallangeRepository challangeRepository) {
        this.challangeRepository = challangeRepository;
    }


    @Autowired
    public void setNotificationRepository(@Lazy NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    @Autowired
    public void setVendorRepository(@Lazy VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Autowired
    public void setLeagueService(@Lazy LeagueService leagueService) {
        this.leagueService = leagueService;
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

    @GetMapping("/get-all-leagues")
    public ResponseEntity<?> getAllLeagues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) LeagueStatus status,

            @RequestParam(value = "vendorId", required = false) Long vendorId
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<League> leaguesPage = leagueService.getAllLeagues(pageable, status, vendorId);

            if (leaguesPage.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, ApiConstants.NO_RECORDS_FOUND, null);

            }

            return responseService.generateSuccessResponse("Leagues fetched successfully", leaguesPage.getContent(), HttpStatus.OK);

        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("League not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching leagues", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-active-leagues/{vendorId}")
    public ResponseEntity<?> getAllActiveLeaguesByVendor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(value = "status", required = false) LeagueStatus status,
            @PathVariable(value = "vendorId", required = false) Long vendorId
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<League> leaguesPage = leagueService.getAllActiveLeaguesByVendor(pageable, vendorId);

            if (leaguesPage.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, ApiConstants.NO_RECORDS_FOUND, null);

            }

            return responseService.generateSuccessResponse("Leagues fetched successfully", leaguesPage.getContent(), HttpStatus.OK);

        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("League not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching leagues", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-leagues/{vendorId}")
    public ResponseEntity<?> getAllLeaguesByVendorId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable(value = "vendorId", required = false) Long vendorId
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<League> leaguesPage = leagueService.getAllLeaguesByVendorId(pageable, vendorId);

            if (leaguesPage.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, ApiConstants.NO_RECORDS_FOUND, null);

            }

            return responseService.generateSuccessResponse("Leagues fetched successfully", leaguesPage.getContent(), HttpStatus.OK);

        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("League not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching leagues", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Create Challenge API
    @PostMapping("/createChallenge")
    public ResponseEntity<?> createChallenge(@RequestBody LeagueRequest leagueRequest, @RequestParam Long vendorId) {
        try {
            ResponseEntity<?> paymentEntity = paymentFeatures.canPublishGame(vendorId);
            if (paymentEntity.getStatusCode() != HttpStatus.OK) {
                return paymentEntity;
            }
            Challenge challenge = leagueService.createChallenge(leagueRequest, vendorId);
            return responseService.generateSuccessResponse("Challenge created successfully. Awaiting opponent's response.", challenge, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error creating challenge: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getChallenge/{id}")
    public ResponseEntity<?> getChallengeById(@PathVariable Long id) {
        try {
            Challenge challenge = leagueService.getChallengeById(id);
            return responseService.generateSuccessResponse("Challenge retrieved successfully.", challenge, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error retrieving challenge: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/by-opponents/{opponentVendorId}")
    public ResponseEntity<?> getChallengesByOpponentVendorIds(
            @PathVariable Long opponentVendorId,
            @RequestParam(required = false) Challenge.ChallengeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        try {
            Page<Challenge> challenges = leagueService.getChallengesByOpponentVendorIds(
                    Collections.singletonList(opponentVendorId), status, pageable);

            return responseService.generateSuccessResponseWithCount(
                    "Challenges retrieved successfully.",
                    challenges.getContent(),
                    challenges.getTotalElements(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return responseService.generateErrorResponse(
                    "Error retrieving challenges: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @GetMapping("/by-vendor/{vendorId}")
    public ResponseEntity<?> getChallengesByVendorIds(
            @PathVariable Long vendorId,
            @RequestParam(required = false) Challenge.ChallengeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        try {
            Page<Challenge> challenges = leagueService.getByVendorIdInAndStatus(
                    Collections.singletonList(vendorId), status, pageable);

            return responseService.generateSuccessResponseWithCount(
                    "Challenges retrieved successfully.",
                    challenges.getContent(),
                    challenges.getTotalElements(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return responseService.generateErrorResponse(
                    "Error retrieving challenges: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    // Reject Challenge API
    @PostMapping("/rejectChallenge/{challengeId}")
    public ResponseEntity<?> rejectChallenge(@PathVariable Long challengeId, @RequestParam Long vendorId) {
        try {
            leagueService.rejectChallenge(challengeId, vendorId);
            return responseService.generateSuccessResponse("Challenge rejected successfully.", "reject", HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error rejecting challenge: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/publishLeague/{vendorId}/{challengeId}")
    public ResponseEntity<?> publishGame(@PathVariable Long vendorId, @PathVariable Long challengeId) {
        try {



            Challenge challenge = challangeRepository.findById(challengeId)
                    .orElseThrow(() -> new RuntimeException("This Challange is  not found"));
            if (vendorId != challenge.getOpponentVendorId()) {
                return responseService.generateErrorResponse("You are not the opponent of this challenge.", HttpStatus.UNAUTHORIZED);
            }

            if (challenge.getChallengeStatus() != Challenge.ChallengeStatus.PENDING) {
                throw new IllegalArgumentException("The challenge is no longer pending.");
            }

            ResponseEntity<?> paymentEntity = paymentFeatures.canPublishGame(vendorId);

            if (paymentEntity.getStatusCode() != HttpStatus.OK) {
                return paymentEntity;
            }

            League publishedLeague = leagueService.publishLeague(challenge, vendorId);


            // Now create a single notification for the vendor
            Notification notification = new Notification();
            notification.setRole("Vendor");

            notification.setVendorId(vendorId);
            if (challenge.getScheduledAt() != null) {
/*
                notification.setType(NotificationType.GAME_SCHEDULED);  // Example NotificationType for a successful payment
*/
                notification.setDescription("Scheduled Game"); // Example NotificationType for a successful
                notification.setDetails("Game has been Scheduled"); // Example NotificationType for a successful
            } else {
/*
                notification.setType(NotificationType.GAME_PUBLISHED);  // Example NotificationType for a successful payment
*/
                notification.setDescription("Published Game"); // Example NotificationType for a successful
                notification.setDetails("Game has been Published"); // Example NotificationType for a successful
            }


            notificationRepository.save(notification);

            if (challenge.getScheduledAt() != null) {
                return responseService.generateSuccessResponse("Game scheduled successfully", publishedLeague, HttpStatus.CREATED);
            } else {
                return responseService.generateSuccessResponse("Game published successfully", publishedLeague, HttpStatus.CREATED);
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

    @GetMapping("/get-vendors-with-available-leagues")
    public ResponseEntity<?> getVendorsWithAvailableLeagues() {
        try {
            // Fetch all vendors with league status 'AVAILABLE'
            List<VendorEntity> vendors = vendorRepository.findByLeagueStatus(LeagueStatus.AVAILABLE);

            if (vendors.isEmpty()) {
                return responseService.generateErrorResponse("No vendors with available leagues found", HttpStatus.NOT_FOUND);
            }

            // Map VendorEntity to VendorDTO
            List<VendorDTO> vendorDTOs = vendors.stream()
                    .map(vendor -> new VendorDTO(vendor.getService_provider_id(), vendor.getFirst_name() + " " + vendor.getLast_name()))
                    .collect(Collectors.toList());

            // Return vendors' IDs and names
            return responseService.generateSuccessResponse("Vendors with available leagues fetched successfully", vendorDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error fetching vendors with available leagues", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/joinLeague")
    public ResponseEntity<?> joinGameRoom(@RequestBody JoinLeagueRequest joinLeagueRequest) {
        try {
            return leagueService.joinRoom(joinLeagueRequest.getPlayerId(), joinLeagueRequest.getLeagueId());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in joining game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/leftLeague")
    public ResponseEntity<?> leaveGameRoom(@RequestBody JoinLeagueRequest leaveRoomRequest) {
        try {
            return leagueService.leaveRoom(leaveRoomRequest.getPlayerId(), leaveRoomRequest.getLeagueId());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in leaving game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/active-game-rooms")
    public ResponseEntity<?> getAllActiveGameRooms() {
        // Fetch all GameRooms with status ONGOING
        List<LeagueRoom> ongoingRooms = leagueRoomRepository.findByStatus(LeagueRoomStatus.ONGOING);

        // Map the GameRoom entities to GameRoomResponseDTO
        List<GameRoomResponseDTO> gameRoomResponseDTOS = ongoingRooms.stream().map(gameRoom -> {
            Integer maxParticipants = gameRoom.getMaxPlayers();
            Long gameId = gameRoom.getGame().getId();
            String gamePassword = gameRoom.getGamepassword();
            Integer moves = gameRoom.getGame().getMove();  // Assuming moves is stored in the Game entity

            BigDecimal totalPrize = matchService.getWinningAmountLeague(gameRoom);  // Assuming matchService calculates the total prize

            return new GameRoomResponseDTO(gameId, gameRoom.getId(), gamePassword, moves, totalPrize, maxParticipants);
        }).collect(Collectors.toList());

        // Return the response wrapped in a success response
        return responseService.generateSuccessResponse("Fetching active game rooms from all leagues", gameRoomResponseDTOS, HttpStatus.OK);
    }


}