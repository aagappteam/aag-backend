package aagapp_backend.services.kycServices;

import aagapp_backend.dto.KycVerificationRequest;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.kyc.KycEntity;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.enums.KycStatus;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.kycRepository.KycRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.EmailService;
import aagapp_backend.services.admin.AdminLogService;
import aagapp_backend.services.firebase.NotoficationFirebase;
import aagapp_backend.services.s3services.S3Service;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class KycService {
    @Autowired
    private KycRepository kycRepository;

    @Autowired
    private NotoficationFirebase notificationFirebase;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AdminLogService adminLogsService;


    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;


    @Transactional
    public KycEntity submitKycRequest(Long userOrVendorId, String role, String adharNo, String panNo,
                                      MultipartFile adharImage, MultipartFile panImage) {
        try {
            String mobileNumber;
            String mailId;
            String name;

            if (role.equalsIgnoreCase("vendor")) {
                VendorEntity vendor = vendorRepository.findById(userOrVendorId)
                        .orElseThrow(() -> new RuntimeException("Vendor not found"));
                mobileNumber = vendor.getMobileNumber();
                mailId = vendor.getPrimary_email();
                name = vendor.getName();
                vendor.setKycStatus(KycStatus.PENDING);
            } else if (role.equalsIgnoreCase("user") || role.equalsIgnoreCase("customer")) {
                CustomCustomer user = customCustomerRepository.findById(userOrVendorId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                mobileNumber = user.getMobileNumber();
                mailId = user.getEmail();
                name = user.getName();
                user.setKycStatus(KycStatus.PENDING);
            } else {
                throw new RuntimeException("Invalid role");
            }

            String adharUrl = null;
            if (adharImage != null && !adharImage.isEmpty()) {
                String adharExtension = adharImage.getOriginalFilename()
                        .substring(adharImage.getOriginalFilename().lastIndexOf("."));
                String adharKey = "kyc/adhar/" + System.currentTimeMillis() + adharExtension;
                s3Service.uploadPhoto(adharKey, adharImage);
                adharUrl = s3Service.getFileUrl(adharKey);
            }

            // Upload PAN image
            String panExtension = panImage.getOriginalFilename()
                    .substring(panImage.getOriginalFilename().lastIndexOf("."));
            String panKey = "kyc/pan/" + System.currentTimeMillis() + panExtension;
            s3Service.uploadPhoto(panKey, panImage);
            String panUrl = s3Service.getFileUrl(panKey);
            if(mailId!=null) {
                emailService.sendKycUploadEmail(mailId, name);

            }


            // Save KYC
            KycEntity kycEntity = new KycEntity();
            kycEntity.setUserOrVendorId(userOrVendorId);
            kycEntity.setRole(role);
            kycEntity.setMobileNumber(mobileNumber);
            kycEntity.setEmail(mailId!=null?mailId:"");
            kycEntity.setName(name);
            kycEntity.setAadharNo(adharNo);
            kycEntity.setPanNo(panNo);
            kycEntity.setAadharImage(adharUrl); // can be null for user
            kycEntity.setPanImage(panUrl);
            kycEntity.setKycStatus(KycStatus.PENDING);
            return kycRepository.save(kycEntity);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Transactional
    public KycEntity updateKycVerificationStatus(Long kycId, KycStatus isVerified) throws IOException {
        KycEntity kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC not found"));

        String email = kyc.getEmail();
        Long userOrVendorId = kyc.getUserOrVendorId();
        String role = kyc.getRole();
        String name;
        String description;
        String details;
        String fcmToken = null;

        Notification notification = new Notification();
        notification.setRole(role);

        if ("VENDOR".equalsIgnoreCase(role)) {
            VendorEntity vendor = vendorRepository.findById(userOrVendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));

            vendor.setKycStatus(isVerified);
            name = vendor.getName();
            fcmToken = vendor.getFcmToken();
            vendorRepository.save(vendor);

            notification.setVendorId(vendor.getService_provider_id());
            notification.setName(name);

        } else if ("USER".equalsIgnoreCase(role)) {
            CustomCustomer customer = customCustomerRepository.findById(userOrVendorId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            customer.setKycStatus(isVerified);
            name = customer.getName();
            fcmToken = customer.getFcmToken();
            customCustomerRepository.save(customer);

            notification.setCustomerId(customer.getId());
            notification.setName(name);
        } else {
            throw new RuntimeException("Invalid role specified in KYC record");
        }

        String title;
        if (isVerified == KycStatus.VERIFIED) {
            description = "KYC Verified";
            details = "Your KYC has been successfully verified.";
            title = "KYC Verified Successfully";
            if (email != null) emailService.sendKycVerifiedEmail(email, name);
        } else if (isVerified == KycStatus.REJECTED) {
            description = "KYC Rejected";
            details = "Your KYC verification has been rejected.";
            title = "KYC Rejected";
            if (email != null) emailService.sendKycRejectedEmail(email, name);
        } else {
            description = "KYC Status Updated";
            details = "Your KYC status was changed to: " + isVerified.name();
            title = "KYC Status Changed";
        }

        // Save in-app notification
        notification.setDescription(description);
        notification.setDetails(details);
        notification.setAmount(null);
        notificationRepository.save(notification);

        if (fcmToken != null && !fcmToken.isEmpty()) {
            try {
                notificationFirebase.sendNotification(fcmToken, title, details);
            } catch (Exception e) {
                throw new RuntimeException("KYC updated but failed to send push notification: " + e.getMessage(), e);
            }
        }

        kyc.setKycStatus(isVerified);
        kycRepository.save(kyc);

        // Log admin action
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        String activity = "KYC status updated to " + isVerified + " for " + role + " ID " + userOrVendorId;
        adminLogsService.logAction(activity, role, performedBy, userOrVendorId, "KYC Verification");

        return kyc;
    }



/*    @Transactional
    public KycEntity updateKycVerificationStatus(Long kycId, KycStatus isVerified) {
        KycEntity kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC not found"));

        String email = kyc.getEmail();

        // Get the role and update corresponding entity
        Long userOrVendorId = kyc.getUserOrVendorId();
        String role = kyc.getRole();
        String name;

        if ("VENDOR".equalsIgnoreCase(role)) {
            VendorEntity vendor = vendorRepository.findById(userOrVendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found"));
            vendor.setKycStatus(isVerified);
            name=vendor.getName();
            vendorRepository.save(vendor);
        } else if ("USER".equalsIgnoreCase(role)) {
            CustomCustomer user = customCustomerRepository.findById(userOrVendorId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setKycStatus(isVerified);
            name=user.getName();
            customCustomerRepository.save(user);
        } else {
            throw new RuntimeException("Invalid role specified in KYC record");
        }

        try {
            if (isVerified == KycStatus.VERIFIED) {
                if(email!=null){
                    emailService.sendKycVerifiedEmail(email, name);
                }
                Notification notification = new Notification();
                CustomCustomer customer = customCustomerService.getCustomerById(userOrVendorId);
                notification.setCustomerId(customer.getId());
                notification.setDescription("Wallet balance deducted"); // Example NotificationType for a successful
                notification.setAmount(entryFee);
                notification.setDetails("Rs. " + entryFee + " deducted for playing " + game.getName()); // Example NotificationType for a successful

                notificationRepository.save(notification);

            } else if (isVerified == KycStatus.REJECTED) {
               if(email!=null){
                   emailService.sendKycRejectedEmail(email, name);
               }
            }
        } catch (IOException e) {
            throw new RuntimeException("KYC updated but failed to send email: " + e.getMessage(), e);
        }

        kyc.setKycStatus(isVerified);
        kycRepository.save(kyc);

//        String performedBy = getLoggedInAdminUsername();
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        String targetType = role; // "USER" or "VENDOR" from KYC record

        String activity = "KYC status updated to " + isVerified + " for " + targetType + " ID " + userOrVendorId;

        adminLogsService.logAction(activity, targetType, performedBy, userOrVendorId, "KYC Verification");


        return kyc;
    }*/


    @Transactional
    public List<KycEntity> updateBulkKycVerificationStatus(List<KycVerificationRequest> requests) {
        List<KycEntity> updatedList = new ArrayList<>();

        for (KycVerificationRequest request : requests) {
            Long kycId = request.getKycId();
            KycStatus isVerified = request.getStatus();

            KycEntity kyc = kycRepository.findById(kycId)
                    .orElseThrow(() -> new RuntimeException("KYC not found for ID: " + kycId));

            kyc.setKycStatus(isVerified);
            String email = kyc.getEmail();
            Long userOrVendorId = kyc.getUserOrVendorId();
            String role = kyc.getRole();
            String name;

            if ("VENDOR".equalsIgnoreCase(role)) {
                VendorEntity vendor = vendorRepository.findById(userOrVendorId)
                        .orElseThrow(() -> new RuntimeException("Vendor not found for ID: " + userOrVendorId));
                vendor.setKycStatus(isVerified);
                name = vendor.getName();
                vendorRepository.save(vendor);
            } else if ("USER".equalsIgnoreCase(role)) {
                CustomCustomer user = customCustomerRepository.findById(userOrVendorId)
                        .orElseThrow(() -> new RuntimeException("User not found for ID: " + userOrVendorId));
                user.setKycStatus(isVerified);
                name = user.getName();
                customCustomerRepository.save(user);
            } else {
                throw new RuntimeException("Invalid role specified for KYC ID: " + kycId);
            }

            try {
                if (isVerified == KycStatus.VERIFIED && email != null) {
                    if(email!=null){
                        emailService.sendKycVerifiedEmail(email, name);

                    }
                } else if (isVerified == KycStatus.REJECTED && email != null) {
                    if(email!=null){
                        emailService.sendKycRejectedEmail(email, name);

                    }
                }
            } catch (IOException e) {
                // Logging the email failure but not stopping the bulk process
                System.err.println("Email failed for KYC ID " + kycId + ": " + e.getMessage());
            }

            updatedList.add(kyc);
        }

        return updatedList;
    }


    public void deleteKycById(Long kycId) {
        if (!kycRepository.existsById(kycId)) {
            throw new RuntimeException("KYC not found");
        }
        kycRepository.deleteById(kycId);
    }

    public String getKycStatus(KycEntity kycEntity) {
        if ("vendor".equalsIgnoreCase(kycEntity.getRole())) {
            VendorEntity vendor = entityManager.find(VendorEntity.class, kycEntity.getUserOrVendorId());
            return vendor != null && vendor.getKycStatus() != null
                    ? vendor.getKycStatus().name()
                    : "UNKNOWN";
        } else if ("user".equalsIgnoreCase(kycEntity.getRole())) {
            CustomCustomer user = entityManager.find(CustomCustomer.class, kycEntity.getUserOrVendorId());
            return user != null && user.getKycStatus() != null
                    ? user.getKycStatus().name()
                    : "UNKNOWN";
        }
        return "UNKNOWN";
    }



}
