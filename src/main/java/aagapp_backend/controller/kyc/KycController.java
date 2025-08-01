package aagapp_backend.controller.kyc;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.KycDTO;
import aagapp_backend.dto.KycVerificationRequest;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.kyc.KycEntity;
import aagapp_backend.enums.KycStatus;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.kycRepository.KycRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.CommonService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.kycServices.KycService;
import aagapp_backend.services.s3services.S3Service;
import aagapp_backend.spec.KycSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/kyc")
public class KycController {

    @Autowired
    private KycService kycService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private KycRepository kycRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadKyc(
            @RequestParam("userOrVendorId") Long userOrVendorId,
            @RequestParam("role") String role,
            @RequestParam(value = "adharNo", required = false) String adharNo,
            @RequestParam("panNo") String panNo,
            @RequestParam(value = "adharImage", required = false) MultipartFile adharImage,
            @RequestParam("panImage") MultipartFile panImage
    ) {
        try {
            // Check if KYC already exists for the given user/vendor
            if (kycRepository.existsByUserOrVendorIdAndRole(userOrVendorId, role)) {
                return ResponseService.generateErrorResponse("KYC already submitted. You cannot submit again.", HttpStatus.CONFLICT);
            }

            panNo = panNo.toUpperCase();

            // Check for null or empty fields
            if (userOrVendorId == null || role == null || role.isBlank() || panNo == null || panNo.isBlank()) {
                return ResponseService.generateErrorResponse("Missing required fields", HttpStatus.BAD_REQUEST);
            }

            // Aadhaar validation only for vendor
            if (role.equalsIgnoreCase("vendor")) {
                if (adharNo == null || adharNo.isBlank()) {
                    return ResponseService.generateErrorResponse("Aadhaar number is required for vendors", HttpStatus.BAD_REQUEST);
                }

                if (!adharNo.matches("\\d{12}")) {
                    return ResponseService.generateErrorResponse("Invalid Aadhaar number. Must be 12 digits.", HttpStatus.BAD_REQUEST);
                }

                if (adharImage == null || adharImage.isEmpty()) {
                    return ResponseService.generateErrorResponse("Aadhaar image is required for vendors", HttpStatus.BAD_REQUEST);
                }

                if (adharImage.getSize() > 5 * 1024 * 1024) {
                    return ResponseService.generateErrorResponse("Aadhaar image must be under 5MB", HttpStatus.BAD_REQUEST);
                }
            }

            // PAN validation
            if (!panNo.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
                return ResponseService.generateErrorResponse("Invalid PAN number format.", HttpStatus.BAD_REQUEST);
            }

            if (panImage == null || panImage.isEmpty()) {
                return ResponseService.generateErrorResponse("PAN image is required", HttpStatus.BAD_REQUEST);
            }

            if (panImage.getSize() > 5 * 1024 * 1024) {
                return ResponseService.generateErrorResponse("PAN image must be under 5MB", HttpStatus.BAD_REQUEST);
            }

            // Check if user/vendor exists
            boolean exists = false;
            if (role.equalsIgnoreCase("vendor")) {
                exists = vendorRepository.existsById(userOrVendorId);
            } else if (role.equalsIgnoreCase("user")) {
                exists = customCustomerRepository.existsById(userOrVendorId);
            } else {
                return ResponseService.generateErrorResponse("Invalid role. Must be 'vendor' or 'user'", HttpStatus.BAD_REQUEST);
            }

            if (!exists) {
                return ResponseService.generateErrorResponse("User/Vendor not found for given ID and role", HttpStatus.NOT_FOUND);
            }

            // Call service with nullable Aadhaar info for users
            KycEntity kycEntity = kycService.submitKycRequest(
                    userOrVendorId, role,
                    adharNo != null ? adharNo : "",
                    panNo,
                    adharImage,
                    panImage
            );

            String notificationTitle = role.equalsIgnoreCase("vendor") ? "Vendor KYC Request" : "User KYC Request";
            String notificationMessage = "A new " + role.toLowerCase() + " KYC request has been submitted. Please review the KYC for +" + role.toLowerCase() + " ID: " + userOrVendorId + ".";

            String submittedByName = "";
            Long senderId = userOrVendorId;
            String senderRole = role.equalsIgnoreCase("vendor") ? Constant.ROLE_VENDOR : Constant.ROLE_CUSTOMER;
            if (role.equalsIgnoreCase("vendor")) {
                VendorEntity vendorEntity = vendorRepository.findById(userOrVendorId).orElse(null);
                submittedByName = vendorEntity != null ? vendorEntity.getName() : "";
            } else {
                CustomCustomer user = customCustomerRepository.findById(userOrVendorId).orElse(null);
                submittedByName = user != null ? user.getName() : "";
            }

            commonService.notifyAdminsByRole(
                    Constant.ADMIN_ROLE,
                    notificationTitle,
                    submittedByName,
                    userOrVendorId,
                    notificationMessage,
                     senderId,
                     senderRole
            );


            return ResponseService.generateSuccessResponse("KYC Request applied successfully. Kindly wait for verification.", kycEntity, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("KYC Request Failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /*    @GetMapping("/all")
    public ResponseEntity<?> getAllKycs(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<KycEntity> kycEntityPage;

            System.out.println("Role: " + role);
            System.out.println("Mobile Number: " + mobileNumber);

            if ((role == null || role.isEmpty()) && (mobileNumber == null || mobileNumber.isEmpty())) {
                kycEntityPage = kycRepository.findAll(pageable);
            } else if (role != null && !role.isEmpty() && (mobileNumber == null || mobileNumber.isEmpty())) {

                kycEntityPage = kycRepository.findByRole(role, pageable);
            } else if ((role == null || role.isEmpty()) && mobileNumber != null && !mobileNumber.isEmpty()) {
                kycEntityPage = kycRepository.findByMobileNumber(mobileNumber, pageable);
            } else {
                kycEntityPage = kycRepository.findByRoleAndMobileNumber(role, mobileNumber, pageable);
            }

            return ResponseService.generateSuccessResponseWithCount(
                    "KYC records retrieved successfully",
                    kycEntityPage.getContent(),
                    kycEntityPage.getTotalElements(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Failed to fetch KYC records: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
@GetMapping("/all")
public ResponseEntity<?> getAllKycs(
        @RequestParam(required = false) String role,
        @RequestParam(required = false) String mobileNumber,
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) String aadharNo,
        @RequestParam(required = false) String panNo,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdAfter,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdBefore,
        @RequestParam(required = false) KycStatus kycStatus,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    try {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Specification<KycEntity> spec = Specification
                .where(KycSpecification.hasRole(role))
                .and(KycSpecification.hasMobileNumber(mobileNumber))
                .and(KycSpecification.hasUserOrVendorId(id))
                .and(KycSpecification.hasAadharNo(aadharNo))
                .and(KycSpecification.hasPanNo(panNo))
                .and(KycSpecification.createdAfter(createdAfter))
                .and(KycSpecification.createdBefore(createdBefore))
                .and(KycSpecification.hasStatus(kycStatus))
                .and(KycSpecification.hasName(name))
                .and(KycSpecification.hasEmail(email))
                .and(KycSpecification.hasSearch(search));

        Page<KycEntity> kycPage = kycRepository.findAll(spec, pageable);

        List<KycDTO> responseList = kycPage.getContent().stream()
                .map(kyc -> new KycDTO(kyc, kyc.getKycStatus()))
                .collect(Collectors.toList());

        long totalCount = kycRepository.findAll().stream().count();
        long pendingCount = kycRepository.countByKycStatus(KycStatus.PENDING);
        long rejectedCount = kycRepository.countByKycStatus(KycStatus.REJECTED);
        long approvedCount = kycRepository.countByKycStatus(KycStatus.VERIFIED);

        return ResponseService.generateSuccessResponseForWithdrwalRequest(
                "KYC records retrieved successfully",
                responseList,
                totalCount,
                approvedCount,
                rejectedCount,
                pendingCount,
                HttpStatus.OK
        );

    } catch (Exception e) {
        return ResponseService.generateErrorResponse("Failed to fetch KYC records: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



    @PutMapping("/verify")
    public ResponseEntity<?> updateKycVerificationStatus(
            @RequestParam Long kycId,
            @RequestParam KycStatus isVerified
    ) {
        try {
            KycEntity updatedKyc = kycService.updateKycVerificationStatus(kycId, isVerified);
            return ResponseService.generateSuccessResponse("KYC status updated", updatedKyc, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Failed to update KYC status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/verify/bulk")
    public ResponseEntity<?> updateBulkKycVerificationStatus(
            @RequestBody List<KycVerificationRequest> requests
    ) {
        try {
            List<KycEntity> updatedKycs = kycService.updateBulkKycVerificationStatus(requests);
            return ResponseService.generateSuccessResponse("Bulk KYC status updated", updatedKycs, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Failed to update bulk KYC status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteKycById(@RequestParam Long kycId) {
        try {
            kycService.deleteKycById(kycId);
            return ResponseService.generateSuccessResponse("KYC deleted successfully", null, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Failed to delete KYC: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
