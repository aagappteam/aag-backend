package aagapp_backend.services.kycServices;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.kyc.KycEntity;
import aagapp_backend.enums.KycStatus;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.kycRepository.KycRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.EmailService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.s3services.S3Service;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class KycService {
    @Autowired
    private KycRepository kycRepository;

    @Autowired
    private S3Service s3Service;

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
                                      MultipartFile adharImage, MultipartFile panImage){

        try {
            // Fetch mobile number based on role
            String mobileNumber;
            String mailId;
            String name;

            if (role.equalsIgnoreCase("vendor")) {
                VendorEntity vendor = vendorRepository.findById(userOrVendorId)
                        .orElseThrow(() -> new RuntimeException("Vendor not found"));
                mobileNumber = vendor.getMobileNumber();
                mailId=vendor.getPrimary_email();
                vendor.setKycStatus(KycStatus.PENDING);
                name=vendor.getName();
            } else if (role.equalsIgnoreCase("user") || role.equalsIgnoreCase("customer")) {
                CustomCustomer user = customCustomerRepository.findById(userOrVendorId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                mobileNumber = user.getMobileNumber();
                mailId= user.getEmail();
                user.setKycStatus(KycStatus.PENDING);
                name=user.getName();
            } else {
                throw new RuntimeException("Invalid role");
            }

            // Upload Aadhaar image
            String adharExtension = adharImage.getOriginalFilename().substring(adharImage.getOriginalFilename().lastIndexOf("."));
            String adharKey = "kyc/adhar/" + System.currentTimeMillis() + adharExtension;
            s3Service.uploadPhoto(adharKey, adharImage);
            String adharUrl = s3Service.getFileUrl(adharKey);

            // Upload PAN image
            String panExtension = panImage.getOriginalFilename().substring(panImage.getOriginalFilename().lastIndexOf("."));
            String panKey = "kyc/pan/" + System.currentTimeMillis() + panExtension;
            s3Service.uploadPhoto(panKey, panImage);
            String panUrl = s3Service.getFileUrl(panKey);

            emailService.sendKycUploadEmail(mailId,name);

            // Save KYC entry
            KycEntity kycEntity = new KycEntity();
            kycEntity.setUserOrVendorId(userOrVendorId);
            kycEntity.setRole(role);
            kycEntity.setMobileNumber(mobileNumber);
            kycEntity.setMailId(mailId);
            kycEntity.setAadharNo(adharNo);
            kycEntity.setPanNo(panNo);
            kycEntity.setAadharImage(adharUrl);
            kycEntity.setPanImage(panUrl);
            return kycRepository.save(kycEntity);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Transactional
    public KycEntity updateKycVerificationStatus(Long kycId, KycStatus isVerified) {
        KycEntity kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC not found"));
        kycRepository.save(kyc);
        String email = kyc.getMailId();

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

            } else if (isVerified == KycStatus.REJECTED) {
               if(email!=null){
                   emailService.sendKycRejectedEmail(email, name);
               }
            }
        } catch (IOException e) {
            throw new RuntimeException("KYC updated but failed to send email: " + e.getMessage(), e);
        }

        return kyc;
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
