package aagapp_backend.controller.league;

import aagapp_backend.dto.LeagueRequest;
import aagapp_backend.entity.league.League;
import aagapp_backend.services.ApiConstants;
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
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/leagues")
public class LeagueController {

    private LeagueService leagueService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;

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
                return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
            }

            return responseService.generateSuccessResponse("Leagues fetched successfully", leaguesPage, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("League not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching leagues", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/publishLeague/{vendorId}")
    public ResponseEntity<?> publishLeague(@PathVariable Long vendorId, @RequestBody LeagueRequest leagueRequest) {
        try {
            ResponseEntity<?> paymentEntity = paymentFeatures.canPublishGame(vendorId);

            if (paymentEntity.getStatusCode() != HttpStatus.OK) {
                return paymentEntity;
            }

            League publishedLeague = leagueService.publishLeague(leagueRequest, vendorId);

            if (leagueRequest.getScheduledAt() != null) {
                return responseService.generateSuccessResponse(
                        "League scheduled successfully", publishedLeague, HttpStatus.CREATED
                );
            } else {
                return responseService.generateSuccessResponse(
                        "League published successfully", publishedLeague, HttpStatus.CREATED
                );
            }
        } catch (LimitExceededException e) {
            return responseService.generateErrorResponse("Exceeded maximum allowed leagues", HttpStatus.TOO_MANY_REQUESTS);
        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("Required entity not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Invalid league data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error publishing league: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{vendorId}/{leagueId}")
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
    }

    @GetMapping("/get-league/{vendorId}/{leagueId}")
    public ResponseEntity<?> getLeague(
            @PathVariable Long vendorId,
            @PathVariable Long leagueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<League> league = leagueService.getLeagueByVendorIdAndLeagueId(vendorId, leagueId, pageable);

            if (league.isEmpty()) {
                return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);

            }

            return responseService.generateSuccessResponse("League fetched successfully", league, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("League not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching league: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
