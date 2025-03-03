package aagapp_backend.controller.vendor;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.BankAccountDTO;
import aagapp_backend.entity.VendorBankDetails;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import aagapp_backend.services.vendor.VendorBankAccountService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/vendor")

public class VendorController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VendorBankAccountService bankAccountService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private VenderService vendorService;


    @Transactional
    @PostMapping("create-or-update-password")
    public ResponseEntity<?> createOrUpdatePassword(
            @RequestBody Map<String, Object> passwordDetails,
            @RequestParam long serviceProviderId) {
        try {
            String password = (String) passwordDetails.get("password");
            String newPassword = (String) passwordDetails.get("newPassword");

            VendorEntity serviceProvider = entityManager.find(VendorEntity.class, serviceProviderId);
            if (serviceProvider == null) {
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);
            }

            // Case 1: If the user doesn't have a password yet (first-time password creation)
            if (serviceProvider.getPassword() == null) {
                if (password == null || password.isEmpty()) {
                    return responseService.generateErrorResponse("Password is required", HttpStatus.BAD_REQUEST);
                }
                serviceProvider.setPassword(passwordEncoder.encode(password));
                entityManager.merge(serviceProvider);
                return responseService.generateSuccessResponse("Password created successfully", serviceProvider, HttpStatus.CREATED);
            } else {
                // Case 2: User already has a password, so allow updating
                if (newPassword == null || newPassword.isEmpty()) {
                    return responseService.generateErrorResponse("New password is required", HttpStatus.BAD_REQUEST);
                }

                // Verify the old password matches the stored one
                if (password == null || !passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    return responseService.generateErrorResponse("Old password does not match", HttpStatus.BAD_REQUEST);
                }

                // Check if the new password is different from the old password
                if (passwordEncoder.matches(newPassword, serviceProvider.getPassword())) {
                    return responseService.generateErrorResponse("New password cannot be the same as the old password", HttpStatus.BAD_REQUEST);
                }

                serviceProvider.setPassword(passwordEncoder.encode(newPassword));
                entityManager.merge(serviceProvider);
                return responseService.generateSuccessResponse("Password updated successfully", serviceProvider, HttpStatus.OK);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error changing/updating password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-vendor/{serviceProviderId}")
    public ResponseEntity<?> getVendorDetailsById(@PathVariable Long serviceProviderId) {
        try {
            VendorEntity serviceProviderEntity = vendorService.getServiceProviderById(serviceProviderId);
            if (serviceProviderEntity == null) {
                return responseService.generateErrorResponse("Service provider not found " + serviceProviderId, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            details.put("status", ApiConstants.STATUS_SUCCESS);
            details.put("status_code", HttpStatus.OK);
            details.put("data", serviceProviderEntity);
            return responseService.generateSuccessResponse("Service provider details are", details, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @DeleteMapping("delete/{serviceProviderId}")
    public ResponseEntity<?> deleteServiceProvider(@PathVariable Long serviceProviderId) {
        try {
            VendorEntity serviceProviderToBeDeleted = entityManager.find(VendorEntity.class, serviceProviderId);
            if (serviceProviderToBeDeleted == null)
                return responseService.generateErrorResponse("No record found", HttpStatus.NOT_FOUND);
            else
                entityManager.remove(serviceProviderToBeDeleted);
            return responseService.generateSuccessResponse("Service Provider Deleted", null, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting: " + e.getMessage());
        }
    }

    @Transactional
    @PatchMapping("update/{serviceProviderId}")
    public ResponseEntity<?> updateServiceProvider(@PathVariable Long serviceProviderId, @RequestBody Map<String, Object> serviceProviderDetails) throws Exception {
        try {
            VendorEntity serviceProvider = entityManager.find(VendorEntity.class, serviceProviderId);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Vendor with provided Id not found", HttpStatus.NOT_FOUND);
            return vendorService.updateServiceProvider(serviceProviderId, serviceProviderDetails);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @GetMapping("/get-all-vendors")
    public ResponseEntity<?> getAllServiceProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            int startPosition = page * limit;
            Query query = entityManager.createQuery(Constant.GET_ALL_SERVICE_PROVIDERS, VendorEntity.class);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);

            List<VendorEntity> results = query.getResultList();

            return ResponseService.generateSuccessResponse("List of vendors: ", results, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @GetMapping("/get-all-details/{serviceProviderId}")
    public ResponseEntity<?> getAllDetails(@PathVariable Long serviceProviderId) {
        try {
            VendorEntity serviceProviderEntity = entityManager.find(VendorEntity.class, serviceProviderId);
            if (serviceProviderEntity == null) {
                return ResponseService.generateErrorResponse("Service provider does not found", HttpStatus.NOT_FOUND);
            }

//            Map<String,Object> serviceProviderMap= sharedUtilityService.serviceProviderDetailsMap(serviceProviderEntity);
            return ResponseService.generateSuccessResponse("Service Provider details retrieved successfully", serviceProviderEntity, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service provider details " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/account-private")
    public ResponseEntity<?> createAccountPrivate(@RequestParam Long serviceProviderId, @RequestBody Map<String, Object> params) {
        try {
            Boolean isPrivate = (Boolean) params.get("isPrivate");
            if (isPrivate == null) {
                return responseService.generateErrorResponse("isPrivate field is required", HttpStatus.BAD_REQUEST);
            }
            VendorEntity vendorEntity = vendorService.getServiceProviderById(serviceProviderId);
            if (vendorEntity == null)
                return responseService.generateErrorResponse("No vendor found with provided ID", HttpStatus.NOT_FOUND);

            vendorEntity.setIsPrivate(isPrivate);
            vendorService.saveOrUpdate(vendorEntity);
            return responseService.generateSuccessResponse("Private account status updated successfully", vendorEntity, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating private account status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @PostMapping("/account-pause")
    public ResponseEntity<?> makeAccountPause(@RequestParam Long serviceProviderId, @RequestBody Map<String, Object> params) {
        try {
            Boolean isPaused = (Boolean) params.get("isPaused");
            String pauseReason = (String) params.get("pauseReason");
            if (isPaused == null || pauseReason == null) {
                return responseService.generateErrorResponse("isPaused and pauseReason fields are required", HttpStatus.BAD_REQUEST);
            }
            VendorEntity vendorEntity = vendorService.getServiceProviderById(serviceProviderId);
            if (vendorEntity == null) {
                return responseService.generateErrorResponse("No vendor found with provided ID", HttpStatus.NOT_FOUND);
            }
            vendorEntity.setIsPaused(isPaused);
            vendorEntity.setPauseReason(pauseReason);

            return responseService.generateSuccessResponse("your account has been paused ", pauseReason, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating pause duration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add/{serviceProviderId}")
    public ResponseEntity<?> addBankAccount(
            @PathVariable Long serviceProviderId,

            @RequestBody @Valid BankAccountDTO bankAccountDTO) {

        try {
            if (serviceProviderId == null) {
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            }
            VendorEntity serviceProvider = entityManager.find(VendorEntity.class, serviceProviderId);

            if (serviceProvider == null) {
                return ResponseService.generateErrorResponse("Vendor not found for this Id", HttpStatus.NOT_FOUND);
            }

            String result = bankAccountService.addBankAccount(serviceProvider, bankAccountDTO);

            if (result.contains("Account numbers do not match.")) {
                return ResponseService.generateErrorResponse(result, HttpStatus.BAD_REQUEST);
            }
            if (result.contains("Account number already exists.")) {
                return ResponseService.generateErrorResponse(result, HttpStatus.BAD_REQUEST);
            }
            String[] resultParts = result.split("ID: ");
            Long generatedId = Long.parseLong(resultParts[1].trim());
            BankAccountDTO responseDTO = new BankAccountDTO(
                    generatedId,
                    bankAccountDTO.getCustomerName(),
                    bankAccountDTO.getAccountNumber(),
                    bankAccountDTO.getReEnterAccountNumber(),
                    bankAccountDTO.getIfscCode(),
                    bankAccountDTO.getBankName(),
                    bankAccountDTO.getBranchName(),
                    bankAccountDTO.getAccountType()
            );


            return ResponseService.generateSuccessResponse("Bank account added successfully!", responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/getbankdetails/{serviceProviderId}")
    public ResponseEntity<?> getBankAccountsByCustomerId(@PathVariable Long serviceProviderId) {
        try {
            if (serviceProviderId == null) {
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            }

            VendorEntity serviceProvider = entityManager.find(VendorEntity.class, serviceProviderId);
            if (serviceProvider == null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            }

            List<BankAccountDTO> bankAccounts = bankAccountService.getBankAccountsByCustomerId(serviceProviderId);

            if (bankAccounts.isEmpty()) {
                return ResponseService.generateErrorResponse("No bank accounts found for this vendor", HttpStatus.NOT_FOUND);
            }

            return ResponseService.generateSuccessResponse("Bank accounts fetched successfully!", bankAccounts, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }


    @PutMapping("/update-bank/{accountId}")
    public ResponseEntity<?> updateBankAccount(
            @PathVariable Long accountId,
            @RequestBody Map<String, Object> params) {
        try {
            if (accountId == null) {
                return ResponseService.generateErrorResponse("Account ID is required", HttpStatus.BAD_REQUEST);
            }

            Optional<VendorBankDetails> existingAccount = bankAccountService.getBankAccountById(accountId);
            if (!existingAccount.isPresent()) {
                return ResponseService.generateErrorResponse("Bank account not found for this Id", HttpStatus.NOT_FOUND);
            }

            String result = bankAccountService.updateBankAccount(accountId, params);
            if (result.equals("Account number already exists.")) {
                return ResponseService.generateErrorResponse(result, HttpStatus.BAD_REQUEST);
            }
            if (result.equals("Account numbers do not match.")) {
                return ResponseService.generateErrorResponse(result, HttpStatus.BAD_REQUEST);
            }
            if (result.equals("Account update failed")) {
                return ResponseService.generateErrorResponse("Failed to update bank account", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Optional<VendorBankDetails> updatedAccount = bankAccountService.getBankAccountById(accountId);
            if (updatedAccount.isEmpty()) {
                return ResponseService.generateErrorResponse("Updated bank account not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            BankAccountDTO updatedBankAccountDTO = bankAccountService.convertToDTO(updatedAccount.get());

            return ResponseService.generateSuccessResponse("Bank account updated successfully!", updatedBankAccountDTO, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Delete bank account response entity.
     *
     * @param accountId the account id
     * @return the response entity
     */
    @DeleteMapping("/delete-bank-account/{accountId}")
    public ResponseEntity<?> deleteBank(@PathVariable Long accountId) {
        try {
            if (accountId == null) {
                return ResponseService.generateErrorResponse("Account ID is required", HttpStatus.BAD_REQUEST);
            }


            Optional<VendorBankDetails> existingAccount = bankAccountService.getBankAccountById(accountId);
            if (!existingAccount.isPresent()) {
                return ResponseService.generateErrorResponse("Bank account not found for this Id", HttpStatus.NOT_FOUND);
            }

            String result = bankAccountService.deleteBankAccount(accountId);
            if (result.equals("Account deletion failed")) {
                return ResponseService.generateErrorResponse("Failed to delete bank account", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return ResponseService.generateSuccessResponse("Bank account deleted successfully!", null, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping("/get-top-invites")
    public ResponseEntity<?> topInvities() {
        try {
            List<VendorEntity> topInvities = vendorService.getTopInvitiesVendor();

            if (topInvities.isEmpty()) {
                return ResponseService.generateErrorResponse("No top vendors found", HttpStatus.NOT_FOUND);
            }

            List<Map<String, Object>> responseList = new ArrayList<>();

            int rank = 1;
            for (VendorEntity vendor : topInvities) {
                Map<String, Object> vendorData = new HashMap<>();
                vendorData.put("vendorName", vendor.getFirst_name() + " " + vendor.getLast_name());
                vendorData.put("profileImage", vendor.getProfilePic());
                vendorData.put("price", vendor.getWalletBalance());
                vendorData.put("service_provider_id", vendor.getService_provider_id());
                vendorData.put("rank", rank++);
                responseList.add(vendorData);
            }

            return ResponseService.generateSuccessResponse("Top vendors fetched successfully!", responseList, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
