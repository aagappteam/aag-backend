package aagapp_backend.controller.tournament;

import aagapp_backend.dto.GameRequest;
import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.dto.TournamentRequest;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.NotificationType;
import aagapp_backend.enums.TournamentStatus;
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
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/tournament")
public class TournamentController {

    private ResponseService responseService;
    private ExceptionHandlingImplement exceptionHandling;
    private PaymentFeatures paymentFeatures;
    private TournamentService tournamentService;

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


           /* // Now create a single notification for the vendor
            Notification notification = new Notification();
            notification.setRole("Vendor");

            notification.setVendorId(vendorId);
            if (gameRequest.getScheduledAt() != null) {
                notification.setType(NotificationType.GAME_SCHEDULED);  // Example NotificationType for a successful payment


                notification.setDescription("Scheduled Game"); // Example NotificationType for a successful
                notification.setDetails("Game has been Scheduled"); // Example NotificationType for a successful
            }else{
                notification.setType(NotificationType.GAME_PUBLISHED);  // Example NotificationType for a successful payment


                notification.setDescription("Published Game"); // Example NotificationType for a successful
                notification.setDetails("Game has been Published"); // Example NotificationType for a successful
            }*/



//            notificationRepository.save(notification);

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

}
