package aagapp_backend.controller.vendor;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.BankAccountDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorBankDetails;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import aagapp_backend.services.vendor.VenderServiceImpl;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vendor")

public class VendorController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VendorBankAccountService bankAccountService;

    @Autowired
    private VenderServiceImpl serviceProviderService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private VenderService vendorService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CustomCustomerService customCustomerService;


    @Transactional
    @PostMapping("create-or-update-password")
    public ResponseEntity<?> createOrUpdatePassword(@RequestBody Map<String, Object> passwordDetails, @RequestParam long serviceProviderId) {
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
            Map<String, Object> responseBody = serviceProviderService.VendorDetails( serviceProviderEntity).getBody();


            return ResponseEntity.ok(responseBody);
//            return responseService.generateSuccessResponse("Service provider details are", responseBody, HttpStatus.OK);

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
            else entityManager.remove(serviceProviderToBeDeleted);
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
    public ResponseEntity<?> getAllServiceProviders(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit,
                                                    @RequestParam(value = "status", required = false) String status,
                                                    @RequestParam(value = "vendorId", required = false) Long vendorId) {
        try {
            int startPosition = page * limit;

            if (vendorId != null) {
                VendorEntity serviceProvider = entityManager.find(VendorEntity.class, vendorId);
                if (serviceProvider == null) {
                    return ResponseService.generateErrorResponse("Service provider does not found", HttpStatus.NOT_FOUND);
                }
                return ResponseService.generateSuccessResponse("Service provider details fetched successfully", serviceProvider, HttpStatus.OK);
            }

            // Create base query for fetching all vendors
            StringBuilder queryString = new StringBuilder(Constant.GET_ALL_SERVICE_PROVIDERS);

            // If status is provided, add status filtering to the query
          /*  if (status != null && !status.isEmpty()) {
                queryString.append(" WHERE v.status = :status");
            }*/

            // Create the query for counting rows
            StringBuilder countQueryString = new StringBuilder("SELECT COUNT(v) FROM VendorEntity v");

            // If status is provided, add status filtering to the count query
           /* if (status != null && !status.isEmpty()) {
                countQueryString.append(" WHERE v.status = :status");
            }*/

            // Execute the count query to get the total number of matching vendors
            Query countQuery = entityManager.createQuery(countQueryString.toString());
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }
            Long totalCount = (Long) countQuery.getSingleResult();

            // Create the query for fetching vendors
            Query query = entityManager.createQuery(queryString.toString(), VendorEntity.class);

            // Set parameter for status if provided
            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }

            query.setFirstResult(startPosition);
            query.setMaxResults(limit);

            // Execute the query and get the list of vendors
            List<VendorEntity> results = query.getResultList();

            // Return the response including the list of vendors and the total count
            return ResponseService.generateSuccessResponseWithCount("List of vendors", results, totalCount, HttpStatus.OK);
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

            // If the account is paused
            if (isPaused) {
                return responseService.generateSuccessResponse("Your account has been paused.", "isPaused "+isPaused, HttpStatus.OK);
            } else {
                // If the account is unpaused
                return responseService.generateSuccessResponse("Your account has been unpaused.", "isPaused "+isPaused, HttpStatus.OK);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating pause duration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add/{serviceProviderId}")
    public ResponseEntity<?> addBankAccount(@PathVariable Long serviceProviderId,

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
            BankAccountDTO responseDTO = new BankAccountDTO(generatedId, bankAccountDTO.getCustomerName(), bankAccountDTO.getAccountNumber(), bankAccountDTO.getReEnterAccountNumber(), bankAccountDTO.getIfscCode(), bankAccountDTO.getBankName(), bankAccountDTO.getBranchName(), bankAccountDTO.getAccountType());


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
    public ResponseEntity<?> updateBankAccount(@PathVariable Long accountId, @RequestBody Map<String, Object> params) {
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

   /* @GetMapping("/get-top-invites")
    public ResponseEntity<?> topInvites(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long authorizedVendorId = jwtUtil.extractId(jwtToken);
            VendorEntity authenticatedVendor = vendorService.getServiceProviderById(authorizedVendorId);
            List<VendorEntity> topInvities = vendorService.getTopInvitiesVendor();

            if (topInvities.isEmpty()) {
                return new ResponseEntity<>(new HashMap<String, Object>() {{
                    put("status", "OK");
                    put("data", new HashMap<String, Object>() {{
                        put("total_earning", 0.0);
                        put("referral_code", authenticatedVendor.getReferralCode());
                        put("total_referrals", 0);
                        put("top_invitees", new ArrayList<>());
                        put("message", "Top vendors fetched successfully!");
                    }});
                    put("message", "Top vendors fetched successfully!");
                    put("status_code", 200);
                }}, HttpStatus.OK);
            }

            // Prepare the top invitees data
            List<Map<String, Object>> topInvitees = new ArrayList<>();
            int rank = 1;
            for (VendorEntity vendor : topInvities) {
                Map<String, Object> vendorData = new HashMap<>();
                vendorData.put("service_provider_id", vendor.getService_provider_id());
                vendorData.put("price", vendor.getWalletBalance());
                vendorData.put("rank", rank++);
                vendorData.put("profileImage", vendor.getProfilePic() != null ? vendor.getProfilePic() : "https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg");
                vendorData.put("vendorName", vendor.getFirst_name() != null && vendor.getLast_name() != null ? vendor.getFirst_name() + " " + vendor.getLast_name() : null);
                topInvitees.add(vendorData);
            }

            // Prepare the final response
            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("status", "OK");
            finalResponse.put("data", new HashMap<String, Object>() {{
                put("total_earning", authenticatedVendor.getWalletBalance());
                put("referral_code", authenticatedVendor.getReferralCode());
                put("total_referrals", authenticatedVendor.getReferralCount());
                put("top_invitees", topInvitees);
                put("message", "Top vendors fetched successfully!");
            }});
            finalResponse.put("message", "Top vendors fetched successfully!");
            finalResponse.put("status_code", 200);

            // Return the response manually
            return new ResponseEntity<>(finalResponse, HttpStatus.OK);
        } catch (Exception e) {
            // Handle exception
            exceptionHandling.handleException(e);
            return new ResponseEntity<>(new HashMap<String, Object>() {{
                put("status", "ERROR");
                put("message", e.getMessage());
                put("status_code", 500);
            }}, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
*/

    @GetMapping("/get-top-invites")
    public ResponseEntity<?> topInvites(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.replace("Bearer ", "");
            Long authorizedVendorId = jwtUtil.extractId(jwtToken);

            // Fetching both authenticated vendor and top vendors in one call
            Map<String, Object> result = vendorService.getTopInvitiesVendorWithAuth(authorizedVendorId);

            VendorEntity authenticatedVendor = (VendorEntity) result.get("authenticatedVendor");
            List<VendorEntity> topInvities = (List<VendorEntity>) result.get("topInvities");

            // If there are no top invitees, return empty data
            if (topInvities.isEmpty()) {
                return createResponse(authenticatedVendor, new ArrayList<>());
            }

            // Prepare the top invitees data
/*            List<Map<String, Object>> topInvitees = new ArrayList<>();
            int rank = 1;
            for (VendorEntity vendor : topInvities) {
                Map<String, Object> vendorData = new HashMap<>();
                vendorData.put("service_provider_id", vendor.getService_provider_id());
                vendorData.put("price", vendor.getWalletBalance());
                vendorData.put("rank", rank++);
                vendorData.put("profileImage", Optional.ofNullable(vendor.getProfilePic())
                        .orElse(Constant.PROFILE_IMAGE_URL));
                vendorData.put("vendorName", Optional.ofNullable(vendor.getFirst_name())
                        .map(firstName -> firstName + " " + vendor.getLast_name())
                        .orElse(null));

                topInvitees.add(vendorData);
            }*/

            List<Map<String, Object>> topInvitees = topInvities.stream().map(vendor -> {
                Map<String, Object> vendorData = new HashMap<>();
                vendorData.put("service_provider_id", vendor.getService_provider_id());
                vendorData.put("price", vendor.getWalletBalance());
                vendorData.put("rank", topInvities.indexOf(vendor) + 1);
                vendorData.put("profileImage", Optional.ofNullable(vendor.getProfilePic()).orElse(Constant.PROFILE_IMAGE_URL));
                vendorData.put("vendorName", Optional.ofNullable(vendor.getFirst_name())
                        .map(firstName -> firstName + " " + vendor.getLast_name())
                        .orElse(null));

                return vendorData;
            }).collect(Collectors.toList());

            return createResponse(authenticatedVendor, topInvitees);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage(),
                    "status_code", 500
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private ResponseEntity<?> createResponse(VendorEntity authenticatedVendor, List<Map<String, Object>> topInvitees) {
        Map<String, Object> data = Map.of(
                "total_earning", authenticatedVendor.getWalletBalance(),
                "referral_code", authenticatedVendor.getReferralCode(),
                "total_referrals", authenticatedVendor.getReferralCount(),
                "top_invitees", topInvitees,
                "message", "Top vendors fetched successfully!"
        );

        Map<String, Object> response = Map.of(
                "status", "OK",
                "data", data,
                "message", "Top vendors fetched successfully!",
                "status_code", 200
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /*@GetMapping("/view/{ticketId}")
    public ResponseEntity<?> viewTicket(@PathVariable Long ticketId) {
        try {
            // Find the ticket by ticketId
            Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);

            // If ticket is not found, return an error response
            if (!ticketOptional.isPresent()) {
                return responseService.generateErrorResponse("Ticket not found with ID: " + ticketId, HttpStatus.NOT_FOUND);
            }

            // Return the ticket details
            Ticket ticket = ticketOptional.get();
            return responseService.generateSuccessResponse("Ticket details retrieved successfully", ticket, HttpStatus.OK);

        } catch (Exception e) {
            // Handle exceptions and return an error response
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching ticket details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/


}
