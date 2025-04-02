package aagapp_backend.services.payment;

import aagapp_backend.dto.PaymentDashboardDTO;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorReferral;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.payment.PlanEntity;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.enums.VendorLevelPlan;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.payment.PaymentRepository;
import aagapp_backend.repository.payment.PlanRepository;
import aagapp_backend.repository.vendor.VendorReferralRepository;
import aagapp_backend.services.NotificationService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private VendorReferralRepository vendorReferralRepository;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JavaMailSender mailSender;


    public List<PaymentEntity> findActivePlansByVendorId(Long vendorId) {
        String query = "SELECT p FROM PaymentEntity p WHERE p.vendorEntity.id = :vendorId AND p.status = 'ACTIVE'";

        TypedQuery<PaymentEntity> typedQuery = entityManager.createQuery(query, PaymentEntity.class);
        typedQuery.setParameter("vendorId", vendorId);

        List<PaymentEntity> result = typedQuery.getResultList();
        logger.info("Found {} active payments for vendorId: {}", result.size(), vendorId);
        return result;
    }


//    @todo:_ Need to rework on this according to level and plan details
    @Transactional
    public PaymentEntity createPayment(PaymentEntity paymentRequest, Long vendorId) {
        VendorEntity existingVendor = entityManager.find(VendorEntity.class, vendorId);

        Long planId = paymentRequest.getPlanId(); // Get planId from the payment request
        PlanEntity planEntity = entityManager.find(PlanEntity.class, planId);
        if (planEntity == null) {
            throw new RuntimeException("Invalid plan ID: " + planId);
        }

        if (existingVendor == null) {
            throw new RuntimeException("Vendor not found with ID: " + vendorId);
        }
       /* List<String> planFeatures = planEntity.getFeatures();
        Integer dailyGameLimit = extractDailyGameLimitByPlan(planFeatures); // Implement logic for extracting game limit*/

        // Associate the vendor with the payment
        paymentRequest.setVendorEntity(existingVendor);
        paymentRequest.setPlanId(planEntity.getId());
        paymentRequest.setAmount(planEntity.getPrice()); // Set the amount based on the plan price
        if(vendorId != null){
            VendorLevelPlan level = existingVendor.getVendorLevelPlan();
            Integer dailyGameLimit = extractDailyGameLimit(planEntity.getFeatures());
            existingVendor.setDailyLimit(dailyGameLimit);

/*
            existingVendor.setDailyLimit(level.getDailyGameLimit());
*/
            paymentRequest.setDailyLimit(dailyGameLimit);
        }

//        @todo need to check theme limit and daily limit
       /* VendorLevelPlan currentLevel = existingVendor.getVendorLevelPlan();

        // Get the new plan level (for example, upgrading to PRO_C)
        VendorLevelPlan newLevel = getVendorLevelFromPlan(planEntity); // Implement this logic based on the selected plan

        if (newLevel != currentLevel) {
            // Vendor is changing levels, so update daily game limit and theme limit
            updateVendorLevel(existingVendor, newLevel, planEntity);
        }*/
        paymentRequest.setPlanDuration(planEntity.getPlanVariant());

        // Generate a unique transaction ID
        paymentRequest.setTransactionId(UUID.randomUUID().toString());
        paymentRequest.setFromUser(existingVendor.getFirst_name());
        paymentRequest.setToUser("Aag App");
        String invoiceUrl = generateInvoiceUrl(paymentRequest.getTransactionId());
        paymentRequest.setDownloadInvoice(invoiceUrl);


        // Set status to ACTIVE by default
        paymentRequest.setStatus(PaymentStatus.ACTIVE);
        existingVendor.setLeagueStatus(LeagueStatus.AVAILABLE);

        // Set expiry date based on the plan duration
        setPlanExpiry(paymentRequest);

        // Set the payment type
        paymentRequest.setPaymentType(paymentRequest.getPaymentType());

        // Expire any existing active payments for the vendor
        expireExistingActivePayments(vendorId);

        // Check if it's the vendor's first payment and handle referral rewards
        if (isFirstPayment(existingVendor)) {
            Optional<VendorReferral> vendorReferralOpt = Optional.ofNullable(vendorReferralRepository.findByReferredId(existingVendor));
            if (vendorReferralOpt.isPresent()) {
                VendorEntity referrer = vendorReferralOpt.get().getReferrerId();
                double commission = calculateReferralCommission(referrer, paymentRequest.getAmount());
                updateReferrerWallet(referrer, commission);
            }
        }


        // Save and return the newly created payment
        existingVendor.setIsPaid(true);
        entityManager.persist(existingVendor);


        // Now create a single notification for the vendor
        Notification notification = new Notification();
        notification.setVendorId(existingVendor.getService_provider_id());  // Set the vendor ID
        notification.setRole("Vendor");  // The role is "Vendor"
/*
        notification.setType(NotificationType.PAYMENT_SUCCESS);  // Example NotificationType for a successful payment
*/
        notification.setDescription("Plan purchased successfully"); // Example NotificationType for a successful
        notification.setAmount(paymentRequest.getAmount());
        notification.setDetails("Your payment of " + paymentRequest.getAmount() + " has been processed. Plan: " + planEntity.getPlanName());

        notificationRepository.save(notification);

        return paymentRepository.save(paymentRequest);
    }

    // Helper method to update the vendor's level and associated features
    private void updateVendorLevel(VendorEntity existingVendor, VendorLevelPlan newLevel, PlanEntity planEntity) {
        // Set the vendor's level to the new level
        existingVendor.setVendorLevelPlan(newLevel);

        // Update the daily game limit and themes based on the new level
        existingVendor.setDailyLimit(newLevel.getDailyGameLimit()); // Set new daily limit
//        existingVendor.setThemeCount(newLevel.getThemeCount()); // Set new theme count

        // Additionally, you can update the feature slots, user counter, etc., if needed
//        existingVendor.setFeatureSlots(newLevel.getFeatureSlots());

        // Update the payment request's daily limit as well (if you want to associate this to the payment)
//        paymentRequest.setDailyLimit(newLevel.getDailyGameLimit());

        // Apply any other changes based on the level, if necessary
    }

    private VendorLevelPlan getVendorLevelFromPlan(PlanEntity planEntity) {
        // Map plan to vendor level, assuming you can determine this from the plan features or name
        String planName = planEntity.getPlanName();
        switch (planName) {
            case "STANDARD_A":
                return VendorLevelPlan.STANDARD_A;
            case "STANDARD_B":
                return VendorLevelPlan.STANDARD_B;
            case "STANDARD_C":
                return VendorLevelPlan.STANDARD_C;
            case "STANDARD_D":
                return VendorLevelPlan.STANDARD_D;
            case "STANDARD_E":
                return VendorLevelPlan.STANDARD_E;
            case "PRO_A":
                return VendorLevelPlan.PRO_A;
            case "PRO_B":
                return VendorLevelPlan.PRO_B;
            case "PRO_C":
                return VendorLevelPlan.PRO_C;
            case "PRO_D":
                return VendorLevelPlan.PRO_D;
            case "PRO_E":
                return VendorLevelPlan.PRO_E;
            case "ELITE_A":
                return VendorLevelPlan.ELITE_A;
            case "ELITE_B":
                return VendorLevelPlan.ELITE_B;
            case "ELITE_C":
                return VendorLevelPlan.ELITE_C;
            case "ELITE_D":
                return VendorLevelPlan.ELITE_D;
            case "ELITE_E":
                return VendorLevelPlan.ELITE_E;
            default:
                throw new RuntimeException("Unknown plan type: " + planName);
        }
    }

    private Integer extractDailyGameLimitByPlan(List<String> features) {
        Pattern pattern = Pattern.compile("Upto (\\d+) Games per Day");

        for (String feature : features) {
            Matcher matcher = pattern.matcher(feature);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        // Default case
        return 0;  // No limit set
    }

    private String generateInvoiceUrl(String transactionId) {
        // You can replace this with the actual base URL for your invoice storage
        String baseUrl = "https://example.com/invoices/";

        // Generate the invoice URL using the transactionId or paymentRequest ID
        return baseUrl + "invoice_" + transactionId + ".pdf"; // The filename pattern could be adjusted as needed
    }
    private Integer extractDailyGameLimit(List<String> features) {
        // Default limit if no matching feature is found
        Integer defaultLimit = 5;

        for (String feature : features) {
            // Match for "daily game publish limit" followed by a number (case insensitive)
            Pattern pattern = Pattern.compile("([0-9]+)\\s*daily game publish limit", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(feature);

            if (matcher.find()) {
                String number = matcher.group(1); // Extract the number
                System.out.println("Feature found: " + feature);
                System.out.println("Extracted number: " + number);
                return Integer.parseInt(number);  // Return the extracted number
            }
        }

        System.out.println("Feature not found, returning default limit of " + defaultLimit);
        return defaultLimit;
    }

/*    private Integer extractDailyGameLimit(List<String> features) {
        for (String feature : features) {
            if (feature.toLowerCase().contains("daily game publish limit")) {
                System.out.println(feature + " feature");
                // Extract the number from the feature string (e.g., "Updated Daily Game Publish Limit: 10")
                return extractNumberFromString(feature);
            }
        }
        return 5;
    }

    private Integer extractNumberFromString(String text) {
        // Use regular expression to find the first occurrence of digits in the string
//        String number = text.replaceAll("\\D", ""); // Remove non-digits characters
        String number = text.replaceAll("[^\\d]", ""); // Replace all non-digit characters except numbers

        System.out.println(number + " extractDailyGameLimit");

        return number.isEmpty() ? 5 : Integer.parseInt(number); // Return 0 if no number is found
    }*/



    // Update referrer wallet balance
    private void updateReferrerWallet(VendorEntity referrer, double amount) {
        if (referrer != null) {
            referrer.setWalletBalance(referrer.getWalletBalance() + amount);
            entityManager.merge(referrer);
        }
    }


    private void setPlanExpiry(PaymentEntity paymentRequest) {
        String planDuration = paymentRequest.getPlanDuration();
        if (planDuration != null) {
            switch (planDuration.toLowerCase()) {
                case "monthly":
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusMonths(1));
                    break;
                case "yearly":
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusYears(1));
                    break;
                default:
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusDays(1));
                    break;
            }
        } else {
            // Default to 1-day expiry if no plan duration is provided
            paymentRequest.setExpiryAt(LocalDateTime.now().plusDays(1));
        }
    }

    private void expireExistingActivePayments(Long vendorId) {
        List<PaymentEntity> existingActivePayments = this.findActivePlansByVendorId(vendorId);
        if (existingActivePayments != null && !existingActivePayments.isEmpty()) {
            for (PaymentEntity existingPayment : existingActivePayments) {
                existingPayment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(existingPayment);  // Mark them as expired
            }
        }
    }

    // Updated to include transactionReference as an additional parameter
    public List<PaymentEntity> getTransactionsByVendorId(Long vendorId, int page, int size, String transactionReference) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findAllTransactionsByVendorId(vendorId, transactionReference, pageable);
    }

    // Updated to include transactionReference as an additional parameter
    public Optional<PaymentDashboardDTO> getActiveTransactionsByVendorId(Long vendorId, Integer dailyPercentage,Integer PublishedLimit) {
        PaymentStatus status = PaymentStatus.ACTIVE;

        // Retrieve the active payment plan for the vendor
        Optional<PaymentEntity> activePlanOptional = paymentRepository.findActivePlanByVendorId(vendorId, LocalDateTime.now(), status);

        // If an active payment plan is found, proceed to map it to the DTO
        if (activePlanOptional.isPresent()) {
            PaymentEntity paymentEntity = activePlanOptional.get();

            // Retrieve the PlanEntity using the planId from the PaymentEntity
            Optional<PlanEntity> planEntityOptional = planRepository.findById(paymentEntity.getPlanId());
            String dailyLimitString = PublishedLimit  + "/" + paymentEntity.getDailyLimit();

            // Map PaymentEntity and PlanEntity to PaymentDashboardDTO
            return Optional.of(planEntityOptional.map(planEntity -> {
                return new PaymentDashboardDTO(
                        planEntity.getPlanName(),                      // Map PlanEntity's planName
                        planEntity.getPlanVariant(),                   // Map PlanEntity's planVariant
                        dailyPercentage != null ? dailyPercentage + "%" : 0 + "%", // Use the passed dailyPercentage or default to 0
                        dailyLimitString,  // Map PaymentEntity's dailyLimit
                        paymentEntity.getId() != null ? paymentEntity.getId() : 0L,  // Map PaymentEntity's ID
                        planEntity.getPrice() != null ? planEntity.getPrice() : 0L   // Map PlanEntity's price
                );
            }).orElseGet(() -> {  // If PlanEntity is not found, return a default PaymentDashboardDTO
                return new PaymentDashboardDTO(
                        "NA",  // Default value for planName
                        "NA",  // Default value for planVariant
                        0 + "%",     // Default value for dailyPercentage
                        PublishedLimit + "/" + 0,     // Default value for dailyLimit
                        0L,    // Default value for id
                        0D     // Default value for price
                );
            }));
        } else {
            // If no active plan is found, return a PaymentDashboardDTO with default values
            PaymentDashboardDTO defaultDTO = new PaymentDashboardDTO(
                    "NA",  // Default value for planName
                    "NA",  // Default value for planVariant
                    0+ "%",     // Default value for dailyPercentage
                    PublishedLimit + "/" + 0,     // Default value for dailyLimit
                    0L,    // Default value for id
                    0D     // Default value for price
            );

            return Optional.of(defaultDTO);  // Wrap default DTO in Optional
        }
    }


    // Updated to include transactionReference as an additional parameter
    public List<PaymentEntity> getAllTransactionsByVendorName(String vendorName, int page, int size, String transactionReference) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findAllTransactionsByVendorName(vendorName, transactionReference, pageable);
    }

    // Method to check if it's the vendor's first payment
    private boolean isFirstPayment(VendorEntity vendor) {
        // Query the number of payments made by this vendor, if it's the first payment (count = 0)
        long paymentCount = paymentRepository.countByVendorEntity(vendor);
        return paymentCount == 0;
    }

    // Dynamic commission calculation method
    private double calculateReferralCommission(VendorEntity referrer, double paymentAmount) {

        // Check if referrer or referrer's VendorLevelPlan is null, and assign STANDARD_A as default
        if (referrer == null || referrer.getVendorLevelPlan() == null) {
            // Assign the default value if null
            referrer.setVendorLevelPlan(VendorLevelPlan.STANDARD_A);
        }
        // Get the VendorLevelPlan of the referrer
        VendorLevelPlan referrerLevel = referrer.getVendorLevelPlan();

        // Define commission rates based on the vendor level and the case
        double commissionPercentage = 0.0;

        switch (referrerLevel) {
            case STANDARD_A:
                commissionPercentage = 0.05;
                break;
            case STANDARD_B:
                commissionPercentage = 0.06;
                break;
            case STANDARD_C:
                commissionPercentage = 0.07;
                break;
            case STANDARD_D:
                commissionPercentage = 0.08;
                break;
            case STANDARD_E:
                commissionPercentage = 0.09;
                break;
            case PRO_A:
                commissionPercentage = 0.05;
                break;
            case PRO_B:
                commissionPercentage = 0.06;
                break;
            case PRO_C:
                commissionPercentage = 0.07;
                break;
            case PRO_D:
                commissionPercentage = 0.08;
                break;
            case PRO_E:
                commissionPercentage = 0.09;
                break;
            case ELITE_A:
                commissionPercentage = 0.05;
                break;
            case ELITE_B:
                commissionPercentage = 0.06;
                break;
            case ELITE_C:
                commissionPercentage = 0.07;
                break;
            case ELITE_D:
                commissionPercentage = 0.08;
                break;
            case ELITE_E:
                commissionPercentage = 0.09;
                break;
            default:
                commissionPercentage = 0.00;
                break;
        }

        // Calculate the commission based on the payment amount
        return paymentAmount * commissionPercentage;
    }


    public void sendEmail(PaymentEntity paymentEntity, String invoiceUrl) throws MessagingException {
        // Create the MimeMessage for the email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");

        try {
            // Set the "From" address (your company or no-reply email)
            helper.setFrom("aagappteam@gmail.com", "AAG App Team");

            // Set the recipient email address
            helper.setTo(paymentEntity.getVendorEntity().getPrimary_email());

            // Set the subject of the email
            helper.setSubject("Your Payment Invoice");

            // Construct the email body text, including the invoice URL
            String emailBody = "Dear "+paymentEntity.getVendorEntity().getFirst_name()+" "+paymentEntity.getVendorEntity().getLast_name()+"\n\n" +
                    "Thank you for your payment. You can download your invoice from the following link:\n\n" +
                    invoiceUrl + "\n\n" +
                    "Best regards,\n" +
                    "AAG App Team\n\n" +
                    "Please ensure to keep this information secure.";

            // Set the email body
            helper.setText(emailBody);

            // Send the email
            mailSender.send(message);
        } catch (MessagingException e) {
            // Handle any messaging errors (e.g., invalid addresses or issues with the email)
            throw new MessagingException("Error while sending invoice email: " + e.getMessage(), e);
        } catch (MailException e) {
            // Handle other mail-related exceptions (e.g., connection issues with SMTP server)
            throw new MessagingException("Error while sending invoice email: " + e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
