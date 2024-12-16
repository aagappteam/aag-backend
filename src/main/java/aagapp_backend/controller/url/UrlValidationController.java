package aagapp_backend.controller.url;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.url.UrlVerificationService;
import aagapp_backend.services.vendor.VenderServiceImpl;
import aagapp_backend.services.vendor.VendorSubmissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/url")
public class UrlValidationController {

    @Autowired
    private UrlVerificationService urlVerificationService;

    @Autowired
    private VendorSubmissionService submissionService;

    @Autowired
    private VenderServiceImpl venderService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @GetMapping("/validate")
    public ResponseEntity<?> validateUrl(@RequestParam String url, @RequestParam String platform) {
        try {
            boolean isValid = urlVerificationService.isUrlValid(url, platform);

            if (isValid) {
                return ResponseService.generateSuccessResponse(
                        "The URL is valid for the given platform.",
                        new HashMap<String, String>() {{
                            put("url", url);
                            put("platform", platform);
                        }},                        HttpStatus.OK
                );
            } else {
                return ResponseService.generateErrorResponse(
                        "Invalid URL for the specified platform.",
                        HttpStatus.BAD_REQUEST
                );
            }

        } catch (IllegalArgumentException e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Invalid submission. Please check your details.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }


    @PostMapping("/submit/{id}")
    public ResponseEntity<?> submitVendorDetails(@RequestBody @Valid VendorSubmissionEntity vendorSubmissionEntity,@PathVariable Long id) {
        try {

            VendorEntity vendorEntity = venderService.getServiceProviderById(id);

            if(vendorEntity==null){
                return ResponseService.generateErrorResponse("No Data not found for this vendor ", HttpStatus.NOT_FOUND);
            }

            VendorSubmissionEntity submittedEntity = submissionService.submitDetails(vendorSubmissionEntity,vendorEntity);

            if (submittedEntity != null) {
                return ResponseService.generateSuccessResponse(
                        "Submission successful. Awaiting admin verification.",
                        submittedEntity,
                        HttpStatus.OK
                );
            }


            return ResponseService.generateErrorResponse(
                    "Invalid submission. Please check your details.",
                    HttpStatus.BAD_REQUEST
            );

        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Invalid submission. Please check your details.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getSubmissionsByStatus(@RequestParam(required = false, defaultValue = "all") String status) {
        try {
            List<VendorSubmissionEntity> submissions = submissionService.getSubmissionsByStatus(status);

            if (submissions.isEmpty()) {
                return ResponseService.generateErrorResponse(
                        "No Data found",
                        HttpStatus.NOT_FOUND
                );
            }

            return ResponseService.generateSuccessResponse(
                    "List of submissions based on status: " + status,
                    submissions,
                    HttpStatus.OK
            );
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(
                    "An error occurred while retrieving submissions.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
