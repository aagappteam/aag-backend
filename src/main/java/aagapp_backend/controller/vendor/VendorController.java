package aagapp_backend.controller.vendor;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.BankAccountDTO;
import aagapp_backend.dto.WithdrawalRequestDTO;
import aagapp_backend.dto.WithdrawalRequestSubmitDto;
import aagapp_backend.entity.VendorBankDetails;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import aagapp_backend.entity.withdrawrequest.WithdrawalRequest;
import aagapp_backend.repository.earning.InfluencerMonthlyEarningRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.repository.withdrawrequest.WithdrawalRequestRepository;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.social.UserVendorFollowService;
import aagapp_backend.services.vendor.VenderService;
import aagapp_backend.services.vendor.VenderServiceImpl;
import aagapp_backend.services.vendor.VendorBankAccountService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vendor")

public class VendorController {

    @Autowired
    private InfluencerMonthlyEarningRepository earningRepo;

    @Autowired
    private WithdrawalRequestRepository withdrawalRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VendorBankAccountService bankAccountService;

    @Autowired
    private VenderServiceImpl serviceProviderService;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private UserVendorFollowService followService;

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

    @Autowired
    private VendorRepository vendorRepository;


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
    public ResponseEntity<?> getAllServiceProviders(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int limit,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(value = "status", required = false) String status,
                                                    @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                    @RequestParam(value = "email", required = false) String email,
                                                    @RequestParam(value = "firstName", required = false) String firstName) {
        try {
            int startPosition = page * limit;

            // If vendorId is provided, fetch vendor details by vendorId
            if (vendorId != null) {
                return this.getVendorDetailsById(vendorId, userId);
            }

            // Base query builders
            StringBuilder queryString = new StringBuilder("SELECT s FROM VendorEntity s");
            StringBuilder countQueryString = new StringBuilder("SELECT COUNT(s) FROM VendorEntity s");

            // Where conditions
            List<String> conditions = new ArrayList<>();

            // Add condition for status filter
            if (status != null && !status.isEmpty()) {
                boolean isActive = Boolean.parseBoolean(status); // "true" or "false" string to boolean
                conditions.add("s.isActive = :status");
            }

            // Add condition for email filter
            if (email != null && !email.isEmpty()) {
                conditions.add("LOWER(s.primary_email) LIKE LOWER(CONCAT('%', :email, '%'))");
            }

            // Add condition for first name filter
            if (firstName != null && !firstName.trim().isEmpty()) {
                String[] nameParts = firstName.split(" ");
                String firstNamePart = nameParts[0].trim();
                String lastNamePart = nameParts.length > 1 ? nameParts[1].trim() : "";

                // Case 1: If there's only one part in the input, treat it as matching both first and last names
                if (nameParts.length == 1) {
                    conditions.add("(LOWER(s.first_name) LIKE LOWER(CONCAT('%', :firstNamePart, '%')) " +
                            "OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :firstNamePart, '%')))");
                }
                // Case 2: If there are multiple parts, match first name and last name explicitly
                else if (nameParts.length > 1) {
                    conditions.add("(LOWER(s.first_name) LIKE LOWER(CONCAT('%', :firstNamePart, '%')))");
                    conditions.add("(LOWER(s.last_name) LIKE LOWER(CONCAT('%', :lastNamePart, '%')))");
                }
            }

            // Apply WHERE clause if there are any conditions
            if (!conditions.isEmpty()) {
                String whereClause = String.join(" AND ", conditions);
                queryString.append(" WHERE ").append(whereClause);
                countQueryString.append(" WHERE ").append(whereClause);
            }

            // Ordering by service_provider_id descending
            queryString.append(" ORDER BY s.service_provider_id DESC");

            // Create queries (Make sure EntityManager is correctly injected)
            Query countQuery = entityManager.createQuery(countQueryString.toString());
            Query query = entityManager.createQuery(queryString.toString(), VendorEntity.class);

            // Set parameters for status, email, first name, and last name
            if (status != null && !status.isEmpty()) {
                boolean isActive = Boolean.parseBoolean(status); // Assuming status = "true"/"false"
                countQuery.setParameter("status", isActive);
                query.setParameter("status", isActive);
            }
            if (email != null && !email.isEmpty()) {
                countQuery.setParameter("email", email);
                query.setParameter("email", email);
            }

            // Set parameters for firstName and lastName if they exist
            if (firstName != null && !firstName.trim().isEmpty()) {
                String[] nameParts = firstName.split(" ");
                String firstNamePart = nameParts[0].trim();
                String lastNamePart = (nameParts.length > 1) ? nameParts[1].trim() : "";

                countQuery.setParameter("firstNamePart", firstNamePart);
                query.setParameter("firstNamePart", firstNamePart);
                if (!lastNamePart.isEmpty()) {
                    countQuery.setParameter("lastNamePart", lastNamePart);
                    query.setParameter("lastNamePart", lastNamePart);
                }
            }

            // Pagination
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);

            // Execute queries
            Long totalCount = (Long) countQuery.getSingleResult();
            List<VendorEntity> results = query.getResultList();

            List<Map<String, Object>> vendorDetailList = new ArrayList<>();

            for (VendorEntity vendor : results) {
                Map<String, Object> vendorDetailsData = serviceProviderService.VendorDetails(vendor, userId).getBody();
                if (vendorDetailsData != null && vendorDetailsData.containsKey("data")) {
                    vendorDetailList.add((Map<String, Object>) vendorDetailsData.get("data"));
                }
            }

            // Return result with count
            return ResponseService.generateSuccessResponseWithCount("List of vendors", vendorDetailList, totalCount, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



/*    @Transactional
    @GetMapping("/get-all-vendors")
    public ResponseEntity<?> getAllServiceProviders(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int limit,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(value = "status", required = false) String status,
                                                    @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                    @RequestParam(value = "email", required = false) String email,
                                                    @RequestParam(value = "firstName", required = false) String firstName) {
        try {
            int startPosition = page * limit;

            if (vendorId != null) {
                   return this.getVendorDetailsById(vendorId,userId);
            }

            // Base query builders
            StringBuilder queryString = new StringBuilder("SELECT s FROM VendorEntity s");
            StringBuilder countQueryString = new StringBuilder("SELECT COUNT(s) FROM VendorEntity s");

            // Where conditions
            List<String> conditions = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                boolean isActive = Boolean.parseBoolean(status); // "true" or "false" string to boolean
                conditions.add("s.isActive = :status");
            }
            if (email != null && !email.isEmpty()) {
                conditions.add("LOWER(s.primary_email) LIKE LOWER(CONCAT('%', :email, '%'))");
            }

            if (firstName != null && !firstName.trim().isEmpty()) {
                String[] nameParts = firstName.split(" ");
                String firstNamePart = nameParts[0].trim();
                String lastNamePart = (nameParts.length > 1) ? nameParts[1].trim() : "";

                // Case 1: If there is only one part in the input, try to match it to both first and last names
                if (nameParts.length == 1) {
                    conditions.add("(LOWER(s.first_name) LIKE LOWER(CONCAT('%', :firstNamePart, '%')) " +
                            "OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :firstNamePart, '%')))");
                }
                // Case 2: If there are two parts, filter by first and last name explicitly
                else if (nameParts.length == 2) {
                    conditions.add("(LOWER(s.first_name) LIKE LOWER(CONCAT('%', :firstNamePart, '%')))");

                    conditions.add("(LOWER(s.last_name) LIKE LOWER(CONCAT('%', :lastNamePart, '%')))");
                }
            }


            // Apply WHERE clause if there are any conditions
            if (!conditions.isEmpty()) {
                String whereClause = String.join(" AND ", conditions);
                queryString.append(" WHERE ").append(whereClause);
                countQueryString.append(" WHERE ").append(whereClause);
            }

            queryString.append(" ORDER BY s.service_provider_id DESC");

            // Create queries
            Query countQuery = entityManager.createQuery(countQueryString.toString());
            Query query = entityManager.createQuery(queryString.toString(), VendorEntity.class);

            // Set parameters
            if (status != null && !status.isEmpty()) {
                boolean isActive = Boolean.parseBoolean(status); // Assuming status = "true"/"false"
                countQuery.setParameter("status", isActive);
                query.setParameter("status", isActive);
            }
            if (email != null && !email.isEmpty()) {
                countQuery.setParameter("email", email);
                query.setParameter("email", email);
            }
*//*            if (firstName != null && !firstName.trim().isEmpty()) {
                countQuery.setParameter("firstName", firstName);
                query.setParameter("firstName", firstName);
            }*//*

            // Pagination
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);


            // Execute queries
            Long totalCount = (Long) countQuery.getSingleResult();
            List<VendorEntity> results = query.getResultList();


            List<Map<String, Object>> vendorDetailList = new ArrayList<>();

            for (VendorEntity vendor : results) {


                Map<String, Object> vendorDetailsData = serviceProviderService.VendorDetails(vendor,userId).getBody();
                if (vendorDetailsData != null && vendorDetailsData.containsKey("data")) {
                    vendorDetailList.add((Map<String, Object>) vendorDetailsData.get("data"));
                }
            }


            // Return result
            return ResponseService.generateSuccessResponseWithCount("List of vendors", vendorDetailList, totalCount, HttpStatus.OK);

//            return ResponseService.generateSuccessResponseWithCount("List of vendors", results, totalCount, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }*/
    @GetMapping("/get-vendor/{serviceProviderId}")
    public ResponseEntity<?> getVendorDetailsById(@PathVariable Long serviceProviderId,  @RequestParam(required = false) Long userId) {
        try {
            VendorEntity serviceProviderEntity = vendorService.getServiceProviderById(serviceProviderId);
            if (serviceProviderEntity == null) {
                return responseService.generateErrorResponse("Service provider not found " + serviceProviderId, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> responseBody = serviceProviderService.VendorDetails( serviceProviderEntity,null).getBody();
            if (userId != null) {
                boolean isFollowing = followService.isUserFollowing(userId, serviceProviderEntity.getService_provider_id());
                serviceProviderEntity.setIsFollowing(isFollowing);
            }

            return ResponseEntity.ok(responseBody);
//            return responseService.generateSuccessResponse("Service provider details are", responseBody, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    @Transactional
    @GetMapping("/get-all-details/{serviceProviderId}")
    public ResponseEntity<?> getAllDetails(@PathVariable Long serviceProviderId,
                                           @RequestParam(required = false) Long userId) {
        try {
            // Fetch the vendor by ID
            VendorEntity serviceProviderEntity = entityManager.find(VendorEntity.class, serviceProviderId);
            if (serviceProviderEntity == null) {
                return ResponseService.generateErrorResponse("Service provider not found", HttpStatus.NOT_FOUND);
            }

            if (userId != null) {
                boolean isFollowing = followService.isUserFollowing(userId, serviceProviderEntity.getService_provider_id());
                System.out.println("Is following: " + isFollowing + " - User ID: " + userId + " - Service Provider ID: " + serviceProviderEntity.getService_provider_id());
                serviceProviderEntity.setIsFollowing(isFollowing);
            }

            return ResponseService.generateSuccessResponse("Service Provider details retrieved successfully", serviceProviderEntity, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service provider details: " + e.getMessage(), HttpStatus.BAD_REQUEST);
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
        List<Map<String, Object>> topInvitees = topInvities.stream().map(vendor -> {
            Map<String, Object> vendorData = new HashMap<>();
            vendorData.put("service_provider_id", vendor.getService_provider_id());
            vendorData.put("price", vendor.getRefferalbalance());
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

@GetMapping("/leaderboards")
public ResponseEntity<?> leaderboards(@RequestHeader("Authorization") String token,
                                      @RequestParam("filterType") String filterType,
                                      @RequestParam("page") int page,
                                      @RequestParam("size") int size) {
    try {
        // Extract the token and find the authenticated vendor
        String jwtToken = token.replace("Bearer ", "");
        Long authorizedVendorId = jwtUtil.extractId(jwtToken);
        VendorEntity authenticatedVendor = entityManager.find(VendorEntity.class, authorizedVendorId);

        // List to store the filtered vendor data
        List<VendorEntity> allVendors = new ArrayList<>();

        // Apply the filter based on filterType and fetch all data
        switch (filterType.toLowerCase()) {
            case "referrals":
                allVendors = vendorRepository.findAll(Sort.by(Sort.Order.desc("refferalbalance"))); // Sorting by referral count
                break;

            case "totalwallet":
                allVendors = vendorRepository.findAll(Sort.by(Sort.Order.desc("totalWalletBalance"))); // Sorting by wallet balance
                break;

            case "participants":
                allVendors = vendorRepository.findAll(Sort.by(Sort.Order.desc("totalParticipatedInGameTournament"))); // Sorting by participants
                break;

            default:
                return new ResponseEntity<>(Map.of(
                        "status", "ERROR",
                        "message", "Invalid filter type provided",
                        "status_code", 400
                ), HttpStatus.BAD_REQUEST);
        }

        // Map the data for leaderboard
        List<Map<String, Object>> leaderboard = allVendors.stream().map(vendor -> {
            Map<String, Object> vendorData = new HashMap<>();
            vendorData.put("service_provider_id", vendor.getService_provider_id());
            vendorData.put("profileImage", Optional.ofNullable(vendor.getProfilePic()).orElse(Constant.PROFILE_IMAGE_URL));
            vendorData.put("vendorName", Optional.ofNullable(vendor.getFirst_name())
                    .map(firstName -> firstName + " " + vendor.getLast_name())
                    .orElse(null));

            // Add specific data based on the filter type
            if ("referrals".equalsIgnoreCase(filterType)) {
                vendorData.put("referralCount", vendor.getRefferalbalance());
            } else if ("totalwallet".equalsIgnoreCase(filterType)) {
                vendorData.put("total_wallet_balance", vendor.getTotalWalletBalance());
            } else if ("participants".equalsIgnoreCase(filterType)) {
                vendorData.put("total_participated", vendor.getTotalParticipatedInGameTournament());
            }

            return vendorData;
        }).collect(Collectors.toList());

        // Rank the vendors based on sorted list (1-based index)
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).put("rank", i + 1); // 1-based ranking
        }

        // Calculate the rank of the logged-in vendor
        int loggedInVendorRank = -1;
        for (int i = 0; i < leaderboard.size(); i++) {
            Map<String, Object> vendorData = leaderboard.get(i);
            Long vendorId = (Long) vendorData.get("service_provider_id");
            if (vendorId.equals(authenticatedVendor.getService_provider_id())) {
                loggedInVendorRank = i + 1;  // Vendor ranks are 1-based
                break;
            }
        }

        // Paginate the leaderboard based on the requested page and size
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, leaderboard.size());
        List<Map<String, Object>> paginatedLeaderboard = leaderboard.subList(fromIndex, toIndex);

        // Prepare response data
        Map<String, Object> data = Map.of(
                "total_earning", authenticatedVendor.getRefferalbalance(),
                "referral_code", authenticatedVendor.getReferralCode(),
                "total_referrals", authenticatedVendor.getReferralCount(),
                "logged_in_vendor_rank", loggedInVendorRank,
                "top_invitees", paginatedLeaderboard,
                "message", "Top vendors fetched successfully!"
        );

        return new ResponseEntity<>(Map.of(
                "status", "OK",
                "data", data,
                "message", "Top vendors fetched successfully!",
                "status_code", 200
        ), HttpStatus.OK);

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
                "total_earning", authenticatedVendor.getRefferalbalance(),
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

//    Vendor Dashboard api

    @GetMapping("/dashboard/{serviceProviderId}")
    public ResponseEntity<?> getDashboardData(@PathVariable Long serviceProviderId) {
        try{

            Map<String, Object> responseBody =  serviceProviderService.getDashboardData(serviceProviderId);

            return responseService.generateSuccessResponse("Dashboard data fetched successfully", responseBody, HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(), HttpStatus.BAD_REQUEST);}//catch

    }


    @PostMapping("/withdraw-request")
    public ResponseEntity<?> requestWithdraw(@RequestBody WithdrawalRequestSubmitDto dto) {
        try {
            String month = LocalDate.now().toString().substring(0, 7);

            VendorEntity vendor = vendorRepository.findById(dto.getInfluencerId()).orElse(null);
            if (vendor == null)
                return responseService.generateErrorResponse("Vendor not found", HttpStatus.BAD_REQUEST);

            WithdrawalRequest req = new WithdrawalRequest();
            req.setInfluencerId(dto.getInfluencerId());
            req.setAmount(dto.getAmount());
            req.setMonthYear(month);
            req.setStatus("PENDING");
            req.setRequestedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

            if (dto.getReason() != null && !dto.getReason().trim().isEmpty()) {
                req.setReason(dto.getReason());
            }

            withdrawalRepo.save(req);

            return responseService.generateSuccessResponse(
                    "Withdraw request submitted",
                    null,
                    HttpStatus.OK
            );
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }


    @GetMapping("/withdraw-history")
    public ResponseEntity<?> getWithdrawHistory(
            @RequestParam Long influencerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));

            Page<WithdrawalRequest> requestsPage = withdrawalRepo.findByInfluencerId(influencerId, pageable);

            VendorEntity vendor = vendorRepository.findById(influencerId).orElse(null);
            if (vendor == null){
                return responseService.generateErrorResponse("Vendor not found", HttpStatus.BAD_REQUEST);

            }

            // Assuming you have a method to get the influencer's name
            String influencerName = vendor.getFirst_name()!=null ? vendor.getFirst_name() + " " + vendor.getLast_name()!=null ? vendor.getLast_name() : "" : "";

            // Convert to DTOs
            List<WithdrawalRequestDTO> dtoList = requestsPage.getContent()
                    .stream()
                    .map(request -> new WithdrawalRequestDTO(request, influencerName))
                    .collect(Collectors.toList());

            return responseService.generateSuccessResponseWithCount(
                    "Withdrawal history fetched successfully",
                    dtoList,
                    requestsPage.getTotalElements(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }


}
