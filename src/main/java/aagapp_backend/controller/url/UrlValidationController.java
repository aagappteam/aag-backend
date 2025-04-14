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
                        "Profile has been saved successfully!",
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
                try {
                    boolean isValid = urlVerificationService.isUrlValid(entry.getValue(), entry.getKey());
                    if (!isValid) {
                        return ResponseService.generateErrorResponse(
                                "Invalid URL format for: " + entry.getKey(),
                                HttpStatus.BAD_REQUEST
                        );
                    }
                } catch (IllegalArgumentException e) {
                    // Unsupported platform
                    return ResponseService.generateErrorResponse(
                            "Unsupported platform: " + entry.getKey(),
                            HttpStatus.BAD_REQUEST
                    );
                } catch (Exception e) {
                    // Any unexpected error during validation
                    exceptionHandlingImplement.handleException(e);
                    return ResponseService.generateErrorResponse(
                            "Error validating URL for: " + entry.getKey(),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                }
            }

            // Check if VendorSubmissionEntity exists for the given serviceProviderId (vendorId)
            VendorSubmissionEntity existingSubmission = submissionService.getVendorSubmissionByServiceProviderId(serviceProviderId);

            if (existingSubmission != null) {
                existingSubmission.setSocialMediaUrls(request.getSocialMediaUrls());
                entityManager.merge(existingSubmission);
                return ResponseService.generateSuccessResponse("Submission updated successfully!", existingSubmission, HttpStatus.OK);
            } else {
                VendorSubmissionEntity newEntity = new VendorSubmissionEntity();
                newEntity.setSocialMediaUrls(request.getSocialMediaUrls());
                entityManager.persist(newEntity);
                return ResponseService.generateSuccessResponse("Submission saved successfully!", newEntity, HttpStatus.OK);
            }

        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Error processing request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
