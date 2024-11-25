package aagapp_backend.controller.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.exception.VendorSubmissionException;
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
@RequestMapping("/vendor-submission")
public class VendorSubmission {

    @Autowired
    private UrlVerificationService urlVerificationService;

    @Autowired
    private VendorSubmissionService submissionService;

    @Autowired
    private VenderServiceImpl venderService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getVendorSubmissionById(@PathVariable Long id) {
        try {
            VendorSubmissionEntity vendorSubmission = submissionService.getSubmissionById(id);

            if (vendorSubmission == null) {
                return ResponseService.generateErrorResponse("No submission found with the provided ID", HttpStatus.NOT_FOUND);
            }

            return ResponseService.generateSuccessResponse("Vendor submission fetched successfully", vendorSubmission, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Error occurred while retrieving the vendor submission", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/submit/{id}")
    public ResponseEntity<?> submitVendorDetails(@RequestBody @Valid VendorSubmissionEntity vendorSubmissionEntity,@PathVariable Long id) {
        try {

            VendorEntity vendorEntity = venderService.getServiceProviderById(id);

            if(vendorEntity==null){
                return ResponseService.generateErrorResponse("No Data not found for this vendor ", HttpStatus.NOT_FOUND);
            }
            VendorSubmissionEntity submittedEntity = submissionService.submitDetails(vendorSubmissionEntity, vendorEntity);

            return ResponseService.generateSuccessResponse(
                    "Submission successful. Awaiting admin verification.",
                    submittedEntity,
                    HttpStatus.OK);

        }catch (VendorSubmissionException ex) {
            return ResponseService.generateErrorResponse(
                    ex.getMessage(),
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


    @PutMapping("/update/{serviceProviderId}")
    public ResponseEntity<?> updateVendorDetails(@RequestBody VendorSubmissionEntity vendorSubmissionEntity, @PathVariable Long serviceProviderId) {
        try {
            VendorSubmissionEntity existingSubmission = submissionService.getVendorSubmissionByServiceProviderId(serviceProviderId);

            if (existingSubmission == null) {
                return ResponseService.generateErrorResponse("No existing submission found for this vendor", HttpStatus.NOT_FOUND);
            }

            // Update the vendor submission with the new data
            VendorSubmissionEntity updatedEntity = submissionService.updateVendorSubmission(vendorSubmissionEntity, existingSubmission);

            // Return success response
            return ResponseService.generateSuccessResponse("Submission updated successfully", updatedEntity, HttpStatus.OK);

        } catch (Exception e) {
            // Log and handle exceptions properly
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Error occurred while updating submission", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
