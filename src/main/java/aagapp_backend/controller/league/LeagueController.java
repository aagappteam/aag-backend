package aagapp_backend.controller.league;

import aagapp_backend.dto.GameRequest;
import aagapp_backend.dto.LeagueRequest;
import aagapp_backend.entity.Challenge;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.enums.NotificationType;
import aagapp_backend.repository.ChallangeRepository;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PaymentFeatures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.LimitExceededException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/leagues")
public class LeagueController {

    private LeagueService leagueService;
    private GameService gameService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;
    private VendorRepository vendorRepository;
    private LeagueRepository leagueRepository;
    private NotificationRepository notificationRepository;
    private ChallangeRepository challangeRepository;

    @Autowired
    public void setChallangeRepository(@Lazy ChallangeRepository challangeRepository){
        this.challangeRepository = challangeRepository;
    }

    @Autowired
    public void setGameService(@Lazy GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    public void setNotificationRepository(@Lazy NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Autowired
    public void setLeagueRepository(@Lazy LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
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
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "vendorId", required = false) Long vendorId
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<League> leaguesPage = leagueService.getAllLeagues(pageable, status, vendorId);

            if (leaguesPage.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK,ApiConstants.NO_RECORDS_FOUND, null);

            }

            return responseService.generateSuccessResponse("Leagues fetched successfully", leaguesPage, HttpStatus.OK);

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
            Challenge challenge= leagueService.createChallenge(leagueRequest, vendorId);
            return responseService.generateSuccessResponse("Challenge created successfully. Awaiting opponent's response.",challenge, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error creating challenge: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Reject Challenge API
    @PostMapping("/rejectChallenge/{challengeId}")
    public ResponseEntity<?> rejectChallenge(@PathVariable Long challengeId, @RequestParam Long vendorId) {
        try {
            leagueService.rejectChallenge(challengeId, vendorId);
            return responseService.generateSuccessResponse("Challenge rejected successfully.","reject", HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error rejecting challenge: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/publishLeague/{vendorId}/{challengeId}")
    public ResponseEntity<?> publishGame(@PathVariable Long vendorId, @PathVariable Long challengeId) {
        try {

            Challenge challenge = challangeRepository.findById(challengeId)
                    .orElseThrow(() -> new RuntimeException("This Challange is  not found"));
            if(vendorId != challenge.getOpponentVendorId()) {
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
            }else{
/*
                notification.setType(NotificationType.GAME_PUBLISHED);  // Example NotificationType for a successful payment
*/
                notification.setDescription("Published Game"); // Example NotificationType for a successful
                notification.setDetails("Game has been Published"); // Example NotificationType for a successful
            }

//            System.out.println(notification  + " fdcx");


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


   /*@PutMapping("/update/{vendorId}/{leagueId}")
    public ResponseEntity<?> updateLeague(
            @PathVariable Long vendorId,
            @PathVariable Long leagueId,
            @RequestBody LeagueRequest leagueRequest
    ) {
        try {
            League response = leagueService.updateLeague(vendorId, leagueId, leagueRequest);

            return responseService.generateSuccessResponse("League updated successfully", response, HttpStatus.OK);

        } catch (IllegalStateException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("League not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating league details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{vendorId}/{leagueId}")
    public ResponseEntity<?> deleteLeague(
            @PathVariable Long vendorId,
            @PathVariable Long leagueId
    ) {
        try {
            leagueService.deleteLeague(vendorId, leagueId);
            return responseService.generateSuccessResponse("League deleted successfully", null, HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("League not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error deleting league: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

//    @GetMapping("/get-league/{vendorId}/{leagueId}")
//    public ResponseEntity<?> getLeague(
//            @PathVariable Long vendorId,
//            @PathVariable Long leagueId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        try {
//            Pageable pageable = PageRequest.of(page, size);
//            Page<League> league = leagueService.getLeagueByVendorIdAndLeagueId(vendorId, leagueId, pageable);
//
//            if (league.isEmpty()) {
//                return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
//
//            }
//
//            return responseService.generateSuccessResponse("League fetched successfully", league, HttpStatus.OK);
//        } catch (NoSuchElementException e) {
//            return responseService.generateErrorResponse("League not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
//        } catch (Exception e) {
//            exceptionHandling.handleException(e);
//            return responseService.generateErrorResponse("Error fetching league: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


    /*@PostMapping("/acceptChallenge/{leagueId}/{vendorId}/{leagueName}")
    public ResponseEntity<?> acceptChallenge(@PathVariable Long leagueId, @PathVariable Long vendorId, @PathVariable String leagueName) {
        try {
            // Retrieve the league
            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new RuntimeException("League not found"));

            // Check if the league has been challenged and if the vendor is the one challenged
            if (league.getOpponentVendorId() == null || !league.getOpponentVendorId().equals(vendorId)) {
                return responseService.generateErrorResponse("This vendor was not challenged", HttpStatus.BAD_REQUEST);
            }


            // Ensure the league is still in the "scheduled" state and within the 10-minute challenge window
            if (league.getStatus() != LeagueStatus.SCHEDULED) {
                return responseService.generateErrorResponse("This league is no longer open for challenge", HttpStatus.BAD_REQUEST);
            }

            if (ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(league.getScheduledAt().plusMinutes(10))) {
                return responseService.generateErrorResponse("The challenge period has expired", HttpStatus.BAD_REQUEST);
            }

            // Check if the vendor has already accepted or rejected the challenge
            if (league.getStatus() == LeagueStatus.LIVE) {
                return responseService.generateErrorResponse("This challenge has already been accepted", HttpStatus.BAD_REQUEST);
            }

            if (league.getStatus() == LeagueStatus.REJECTED) {
                return responseService.generateErrorResponse("This challenge has already been rejected", HttpStatus.BAD_REQUEST);
            }

            // Change the status to LIVE
            league.setStatus(LeagueStatus.LIVE);
//            league.setOpponentVendorTeamName(leagueName);
            league.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

            // Save the league status
            leagueRepository.save(league);

            return responseService.generateSuccessResponse("Challenge accepted successfully", league, HttpStatus.OK);

        } catch (Exception e) {
            return responseService.generateErrorResponse("Error accepting challenge: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/rejectChallenge/{leagueId}/{vendorId}")
    public ResponseEntity<?> rejectChallenge(@PathVariable Long leagueId, @PathVariable Long vendorId) {
        try {
            // Retrieve the league
            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new RuntimeException("League not found"));

            // Check if the vendor is part of the challenged vendors
            if (league.getOpponentVendorId() == null || !league.getOpponentVendorId().equals(vendorId)) {
                return responseService.generateErrorResponse("This vendor was not challenged", HttpStatus.BAD_REQUEST);
            }

            // Ensure the league is still in the "scheduled" state
            if (league.getStatus() != LeagueStatus.SCHEDULED) {
                return responseService.generateErrorResponse("This league is no longer open for challenge", HttpStatus.BAD_REQUEST);
            }

            // Check if the vendor has already accepted or rejected the challenge
            if (league.getStatus() == LeagueStatus.LIVE) {
                return responseService.generateErrorResponse("This challenge has already been accepted, it cannot be rejected", HttpStatus.BAD_REQUEST);
            }
            if (league.getStatus() == LeagueStatus.REJECTED) {
                return responseService.generateErrorResponse("This challenge has already been rejected", HttpStatus.BAD_REQUEST);
            }

            // Change the status to REJECTED instantly
            league.setStatus(LeagueStatus.REJECTED);
            league.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

            // Save the league status
            leagueRepository.save(league);

            return responseService.generateSuccessResponse("Challenge rejected successfully", league, HttpStatus.OK);

        } catch (Exception e) {
            return responseService.generateErrorResponse("Error rejecting challenge: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @GetMapping("/get-vendors-with-available-leagues")
    public ResponseEntity<?> getVendorsWithAvailableLeagues() {
        try {
            // Fetch all vendors with league status 'AVAILABLE'
            List<VendorEntity> vendors = vendorRepository.findByLeagueStatus(LeagueStatus.AVAILABLE);

            if (vendors.isEmpty()) {
                return responseService.generateErrorResponse("No vendors with available leagues found", HttpStatus.NOT_FOUND);
            }

            // Return vendors if found
            return responseService.generateSuccessResponse("Vendors with available leagues fetched successfully", vendors, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error fetching vendors with available leagues", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}