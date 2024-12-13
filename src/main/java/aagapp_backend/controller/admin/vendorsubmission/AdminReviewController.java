package aagapp_backend.controller.admin.vendorsubmission;

import aagapp_backend.services.ResponseService;
import aagapp_backend.services.admin.AdminReviewService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adminreview")
public class AdminReviewController {

    private AdminReviewService reviewService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;

    @Autowired
    public void setReviewService(AdminReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveSubmission(@PathVariable Long id) {
        try {
            AdminReviewService.SubmissionResponse response = (AdminReviewService.SubmissionResponse) reviewService.reviewSubmission(id, true); // true for approval

            if (response.getMessage().contains("already")) {
                return responseService.generateSuccessResponse(response.getMessage(), response.getData(), HttpStatus.OK);
            } else if (response.getMessage().contains("not found")) {
                return responseService.generateErrorResponse(response.getMessage(), HttpStatus.NOT_FOUND);
            }
            return responseService.generateSuccessResponse(response.getMessage(), response.getData(), HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while processing the approval request.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectSubmission(@PathVariable Long id) {
        try {
            AdminReviewService.SubmissionResponse response = (AdminReviewService.SubmissionResponse) reviewService.reviewSubmission(id, false); // false for rejection

            if (response.getMessage().contains("already")) {
                return responseService.generateSuccessResponse(response.getMessage(), response.getData(), HttpStatus.OK);
            } else if (response.getMessage().contains("not found")) {
                return responseService.generateErrorResponse(response.getMessage(), HttpStatus.NOT_FOUND);
            }
            return responseService.generateSuccessResponse(response.getMessage(), response.getData(), HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("An error occurred while processing the rejection request.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
