package aagapp_backend.services.referal;

import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReferralValidationService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private CustomCustomerRepository customerUserRepository;

    public ResponseEntity<?> validateReferralCode(String referralCode, Integer roleId) {
        if (roleId == null || referralCode == null || referralCode.isBlank()) {
            return responseService.generateErrorResponse("Missing referralCode or roleId.", HttpStatus.BAD_REQUEST);
        }

        if (roleId == 4) { // Vendor
            boolean exists = vendorRepository.findByReferralCode(referralCode).isPresent();
            if (exists) {
                return responseService.generateSuccessResponse(
                        "Referral code is valid.",
                        Map.of("referralCode", referralCode),
                        HttpStatus.OK
                );
            } else {
                return responseService.generateErrorResponse("Referral code is invalid.", HttpStatus.BAD_REQUEST);
            }
        } else if (roleId == 5) { // Customer
            boolean exists = customerUserRepository.findByReferralCode(referralCode).isPresent();
            if (exists) {
                return responseService.generateSuccessResponse(
                        "Referral code is valid.",
                        Map.of("referralCode", referralCode),
                        HttpStatus.OK
                );
            } else {
                return responseService.generateErrorResponse("Referral code is invalid.", HttpStatus.BAD_REQUEST);
            }
        } else {
            return responseService.generateErrorResponse("Invalid role ID.", HttpStatus.BAD_REQUEST);
        }
    }
}


