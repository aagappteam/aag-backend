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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private ResponseService responseService;

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getVendorSubmissionById(@PathVariable Long id) {
        try {
            VendorSubmissionEntity vendorSubmission = submissionService.getSubmissionById(id);


            if (vendorSubmission == null) {
                return responseService.generateResponse(HttpStatus.OK, "No submission found with the provided ID" ,null);

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

        } catch (VendorSubmissionException ex) {
            // Handling specific VendorSubmissionException (e.g., invalid URL)
            return ResponseService.generateErrorResponse(
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Invalid submission. Please check your details. "                    + e.getMessage()
                    ,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getSubmissionsByStatus(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String planname,
            @RequestParam(defaultValue = "0") int page,    // Default page is 0 (first page)
            @RequestParam(defaultValue = "10") int size     // Default size is 10
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));

            if (email != null && !email.isEmpty()) {
                Page<VendorSubmissionEntity> submissionsPage = submissionService.getSubmissionsByEmail(email, pageable);
                if (submissionsPage.isEmpty()) {
                    return responseService.generateResponse(HttpStatus.OK, "No Data found for the provided email", null);
                }
                return ResponseService.generateSuccessResponseWithCount(
                        "List of submissions based on email: " ,
                        submissionsPage.getContent(),  // Get the content of the page
                        submissionsPage.getTotalElements(),  // Total elements in the database
                        HttpStatus.OK
                );
           }

            Page<VendorSubmissionEntity> submissionsPage = submissionService.getSubmissionsByStatus(status, pageable);

            if (submissionsPage.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "No Data found", null);
            }

            // Return paginated response
            return ResponseService.generateSuccessResponseWithCount(
                    "List of submissions based on status: " + status,
                    submissionsPage.getContent(),  // Get the content of the page
                    submissionsPage.getTotalElements(),  // Total elements in the database
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

            VendorSubmissionEntity updatedEntity = submissionService.updateVendorSubmission(vendorSubmissionEntity, existingSubmission);

            return ResponseService.generateSuccessResponse("Submission updated successfully", updatedEntity, HttpStatus.OK);

        } catch (Exception e) {
            // Log and handle exceptions properly
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Error occurred while updating submission", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteVendorSubmission(@PathVariable Long id) {
        try {
            VendorSubmissionEntity existingSubmission = submissionService.getSubmissionById(id);

            if (existingSubmission == null) {
                return ResponseService.generateErrorResponse(
                        "No submission found with the provided ID",
                        HttpStatus.NOT_FOUND
                );
            }

            submissionService.deleteVendorSubmission(id);

            return ResponseService.generateSuccessResponse(
                    "Vendor submission deleted successfully",
                    null,
                    HttpStatus.OK
            );
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse(
                    "Error occurred while deleting the vendor submission",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }



}
