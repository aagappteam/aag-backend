package aagapp_backend.controller.customer.socialmedia;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.social.SocialUser;
import aagapp_backend.enums.SocialStatus;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.social.SocialUserRepository;
import aagapp_backend.services.CommonService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.url.UrlVerificationService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("user-social")
public class SocialUserController {

    private final SocialUserRepository socialUserRepository;
    private final CustomCustomerRepository customerRepository;
    private final UrlVerificationService urlVerificationService;
    private final ExceptionHandlingImplement exceptionHandlingImplement;
    private final EntityManager entityManager;

    @Autowired
    private CommonService commonService;

    public SocialUserController(
            SocialUserRepository socialUserRepository,
            CustomCustomerRepository customerRepository,
            UrlVerificationService urlVerificationService,
            ExceptionHandlingImplement exceptionHandlingImplement,
            EntityManager entityManager) {
        this.socialUserRepository = socialUserRepository;
        this.customerRepository = customerRepository;
        this.urlVerificationService = urlVerificationService;
        this.exceptionHandlingImplement = exceptionHandlingImplement;
        this.entityManager = entityManager;
    }

    @Transactional
    @PostMapping("/submit/{customerId}")
    public ResponseEntity<?> submitSocialLinks(@PathVariable Long customerId,
                                               @RequestBody Map<String, String> socialMediaUrls) {
        try {
            // Validate all social media URLs
            for (Map.Entry<String, String> entry : socialMediaUrls.entrySet()) {
                try {
                    boolean isValid = urlVerificationService.isUrlValid(entry.getValue(), entry.getKey());
                    if (!isValid) {
                        return ResponseService.generateErrorResponse(
                                "Invalid URL format for: " + entry.getKey(),
                                HttpStatus.BAD_REQUEST
                        );
                    }
                } catch (IllegalArgumentException e) {
                    return ResponseService.generateErrorResponse(
                            "Unsupported platform: " + entry.getKey(),
                            HttpStatus.BAD_REQUEST
                    );
                } catch (Exception e) {
                    exceptionHandlingImplement.handleException(e);
                    return ResponseService.generateErrorResponse(
                            "Error validating URL for: " + entry.getKey(),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                }
            }

            // Find the customer
            Optional<CustomCustomer> optionalCustomer = customerRepository.findById(customerId);
            if (optionalCustomer.isEmpty()) {
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }

            CustomCustomer customer = optionalCustomer.get();

            // Check if a SocialUser already exists
            SocialUser existing = socialUserRepository.findByCustomer(customer);
            if (existing != null &&
                    (existing.getStatus() == SocialStatus.APPROVED || existing.getStatus() == SocialStatus.REJECTED)) {

                return ResponseService.generateSuccessResponse(
                        "Social links already processed, you cannot re-submit",
                        existing,
                        HttpStatus.OK
                );
            }

            if (existing != null) {
                existing.setSocialMediaUrls(socialMediaUrls);
                existing.setStatus(SocialStatus.PENDING); // Move back to pending on re-submit
                existing.setUpdatedAt(new Date());
                entityManager.merge(existing);
                return ResponseService.generateSuccessResponse("Social links updated", existing, HttpStatus.OK);
            }

            // Else, create new
            SocialUser socialUser = new SocialUser();
            socialUser.setCustomer(customer);
            socialUser.setSocialMediaUrls(socialMediaUrls);
            socialUser.setStatus(SocialStatus.PENDING);
            socialUserRepository.save(socialUser);

            String notificationTitle = "New Social Link Submission";
            String submittedByName = customer.getName() != null ? customer.getName() : "Unknown User";
            Long userOrVendorId = customer.getId();
            String notificationMessage = "User " + submittedByName + " has submitted their social media links for verification.";
            Long senderId = customer.getId();
            String senderRole = "CUSTOMER";

            commonService.notifyAdminsByRole(
                    Constant.ADMIN_ROLE,
                    "Social Link Submission",
                    submittedByName,
                    userOrVendorId,
                    notificationMessage,
                    senderId,
                    senderRole
            );


            return ResponseService.generateSuccessResponse("Social links submitted", socialUser, HttpStatus.CREATED);

        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return ResponseService.generateErrorResponse("Server error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
