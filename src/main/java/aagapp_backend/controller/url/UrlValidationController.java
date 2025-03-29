package aagapp_backend.controller.url;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.url.UrlVerificationService;
import aagapp_backend.services.vendor.VenderService;
import aagapp_backend.services.vendor.VenderServiceImpl;
import aagapp_backend.services.vendor.VendorSubmissionService;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private EntityManager entityManager;



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



    @Transactional
    @PostMapping("/validate-and-save/{serviceProviderId}")
    public ResponseEntity<?> validateAndSave(@PathVariable Long serviceProviderId, @RequestBody VendorSubmissionEntity request) {
        try {
            // Validate all social media URLs
            for (Map.Entry<String, String> entry : request.getSocialMediaUrls().entrySet()) {
                boolean isValid = urlVerificationService.isUrlValid(entry.getValue(), entry.getKey());
                if (!isValid) {
                    return ResponseService.generateErrorResponse(
                            "Invalid URL for platform: " + entry.getKey(),
                            HttpStatus.BAD_REQUEST
                    );
                }
            }

            // Check if VendorSubmissionEntity exists for the given serviceProviderId (vendorId)
            VendorSubmissionEntity existingSubmission = submissionService.getVendorSubmissionByServiceProviderId(serviceProviderId);

            if (existingSubmission != null) {
                existingSubmission.setSocialMediaUrls(request.getSocialMediaUrls());
                // Save the updated entity
               entityManager.merge(existingSubmission);

                return ResponseService.generateSuccessResponse("Submission updated successfully!", existingSubmission, HttpStatus.OK);
            } else {
                // If no submission exists, create a new one and save it
                VendorSubmissionEntity newEntity = new VendorSubmissionEntity();
                newEntity.setSocialMediaUrls(request.getSocialMediaUrls());
                 entityManager.persist(newEntity);


                return ResponseService.generateSuccessResponse("Submission saved successfully!", existingSubmission, HttpStatus.OK);
            }

        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Error processing request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


   /* @GetMapping("/status")
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
    }*/
}
