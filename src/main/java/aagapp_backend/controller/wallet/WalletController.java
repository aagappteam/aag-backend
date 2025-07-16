package aagapp_backend.controller.wallet;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.AddBalanceRequest;
import aagapp_backend.dto.CustomerWithdrawalRequestDto;
import aagapp_backend.dto.KwickPayResponse;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.entity.withdrawrequest.CustomerWithdrawalRequest;
import aagapp_backend.enums.NotificationType;
import aagapp_backend.enums.VendorStatus;
import aagapp_backend.enums.WithdrawalStatus;
import aagapp_backend.enums.WithdrawalType;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.withdrawrequest.CustomerWithdrawalRequestRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.RoleService;
import aagapp_backend.services.admin.InvoiceServiceAdmin;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.vendor.VenderService;
import aagapp_backend.services.wallet.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;


@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private VenderService vendorService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private InvoiceServiceAdmin invoiceServiceAdmin;

    @Autowired
    private CustomerWithdrawalRequestRepository customerWithdrawalRequestRepository;

    // Endpoint to add balance to the wallet
    @PostMapping("/addBalance")
    public ResponseEntity<?> addBalance(@RequestBody AddBalanceRequest addBalanceRequest, @RequestHeader(value = "Authorization") String authorization) {
        try {
            // Validate Authorization header
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }

            String token = authorization.substring(7);
            Long customerId = addBalanceRequest.getCustomerId();

            CustomCustomer customer1 = customCustomerService.getCustomerById(customerId);

            if (customer1.getStatus() != VendorStatus.ACTIVE) {
                throw new BusinessException("You are Suspended or Blocked", HttpStatus.BAD_REQUEST);
            }


            // Extract user ID from the token and validate it
            Long userId = jwtUtil.extractId(token);
            Integer role = jwtUtil.extractRoleId(token);

            if (userId == null) {
                return responseService.generateErrorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Check if the user is authorized to add balance to this wallet
            if (!userId.equals(customerId)) {
                return responseService.generateErrorResponse("You are not authorized to perform this action", HttpStatus.FORBIDDEN);
            }

            // Validate the amount to add
            float amount = addBalanceRequest.getAmount();
            if (amount <= 0) {
                return responseService.generateErrorResponse("Amount must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            boolean isTest = addBalanceRequest.getIsTest();

            // Call the wallet service to add balance to the wallet
            Wallet updatedWallet = walletService.addBalanceToWallet(customerId, amount, isTest);


            // Now create a single notification for the vendor
            Notification notification = new Notification();
            notification.setRole(role == Constant.VENDOR_ROLE ? "Vendor" : "Customer");

            if (role == Constant.VENDOR_ROLE) {
                VendorEntity vendor = vendorService.getServiceProviderById(userId);
                notification.setVendorId(vendor.getService_provider_id());
            } else if (role == Constant.CUSTOMER_ROLE) {
                CustomCustomer customer = customCustomerService.getCustomerById(userId);
                notification.setCustomerId(customer.getId());
            }
/*
            notification.setType(NotificationType.WALLET_CREDIT);  // Example NotificationType for a successful payment
*/
            notification.setDescription("Wallet balance added"); // Example NotificationType for a successful
            notification.setAmount((double) amount);
            notification.setDetails("Rs. " +amount + " added to Wallet"); // Example NotificationType for a successful

            notificationRepository.save(notification);
            invoiceServiceAdmin.createInvoiceForCustomer((double)amount, customerId);

            return responseService.generateSuccessResponse("Balance added successfully", updatedWallet, HttpStatus.OK);
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            // Handle specific case when the wallet is not found
            return responseService.generateErrorResponse("No wallet found for customer with ID " + addBalanceRequest.getCustomerId(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error adding balance: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Method to handle the POST request to get balance
    @GetMapping("/getBalance/{customerId}")
    public ResponseEntity<?> getBalance(@PathVariable("customerId") Long customerId, @RequestHeader(value = "Authorization") String authorization) {
        try {
            // Validate Authorization header
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }

            String token = authorization.substring(7);
            Integer roleId = jwtUtil.extractRoleId(token);

            String roleName = roleService.findRoleName(roleId);


            // Validate customerId
            if (customerId == null) {
                return responseService.generateErrorResponse("Customer ID is required", HttpStatus.BAD_REQUEST);
            }

            // Extract user ID from the token and validate the token
            Long userId = jwtUtil.extractId(token);
            if (userId == null) {
                return responseService.generateErrorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Check if the authenticated user is allowed to access the balance
//            if (!userId.equals(customerId)) {
//                return responseService.generateErrorResponse("You are not authorized to perform this action", HttpStatus.FORBIDDEN);
//            }

            if (!("Admin".equalsIgnoreCase(roleName) || "SuperAdmin".equalsIgnoreCase(roleName))) {
                if (!userId.equals(customerId)) {
                    return responseService.generateErrorResponse("You are not authorized to perform this action", HttpStatus.FORBIDDEN);
                }
            }

            // Call the wallet service to retrieve the balance
            ResponseEntity<?> wallet = walletService.getBalanceToWallet(customerId);
            if (wallet == null) {
                return responseService.generateResponse(HttpStatus.OK, "No wallet found for the user" ,null);

            }
            return wallet;
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving balance: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*@PostMapping("/deductAmount")
    public ResponseEntity<?> deductAmount(@RequestBody AddBalanceRequest addBalanceRequest, @RequestHeader(value = "Authorization") String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }

            String token = authorization.substring(7);
            Long customerId = addBalanceRequest.getCustomerId();
            Integer role = jwtUtil.extractRoleId(token);

            // Validate JWT Token
            Long userId = jwtUtil.extractId(token);
            if (userId == null) {
                return responseService.generateErrorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Check if the user has permission to perform the action
            if (!userId.equals(customerId)) {
                return responseService.generateErrorResponse("You are not authorized to perform this action", HttpStatus.FORBIDDEN);
            }

            // Validate amount
            float amount = addBalanceRequest.getAmount();
            if (amount <= 0) {
                return responseService.generateErrorResponse("Amount must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            if (amount > 1000000) {
                return responseService.generateErrorResponse("Amount is too large", HttpStatus.BAD_REQUEST);
            }

            if (amount < 0.01) {
                return responseService.generateErrorResponse("Amount is too small", HttpStatus.BAD_REQUEST);
            }

            // Call the wallet service to deduct the amount
            Wallet updatedWallet = walletService.deductAmountFromWallet(customerId, amount);


            // Now create a single notification for the vendor
            Notification notification = new Notification();
            notification.setRole(role == Constant.VENDOR_ROLE ? "Vendor" : "Customer");

            if (role == Constant.VENDOR_ROLE) {
                VendorEntity vendor = vendorService.getServiceProviderById(userId);
                notification.setVendorId(vendor.getService_provider_id());
            } else if (role == Constant.CUSTOMER_ROLE) {
                CustomCustomer customer = customCustomerService.getCustomerById(userId);
                notification.setCustomerId(customer.getId());
            }
*//*
            notification.setType(NotificationType.WALLET_DEBIT);  // Example NotificationType for a successful payment
*//*
            notification.setDescription("Wallet balance deducted"); // Example NotificationType for a successful
            notification.setAmount((double) amount);
            notification.setDetails("Rs. "  + amount + "debited from Wallet"); // Example NotificationType for a successful

            notificationRepository.save(notification);
            return responseService.generateSuccessResponse("Balance deducted successfully", updatedWallet, HttpStatus.OK);

        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error deducting balance: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @PostMapping("/withdrawalAmount")
    public ResponseEntity<?> withdrawalAmount(
            @RequestBody CustomerWithdrawalRequestDto dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }

            String token = authHeader.substring(7);
            Long customerId = dto.getCustomerId();
            CustomCustomer customer = walletService.validateCustomer(customerId, token);

            walletService.isEnoughAmount(dto.getAmount(), customerId);
            walletService.checkDailyLimit(customerId);
            walletService.validateAmount(dto.getAmount());

            String uniqueTxnId = "AAG" + System.currentTimeMillis();

            // 🔐 Secure API call — will throw BusinessException on failure
            KwickPayResponse kpResp = walletService.callPayoutGateway(dto,uniqueTxnId , customer);


//            // 🔧 Use dummy response for testing
//            KwickPayResponse kpResp = new KwickPayResponse();
//            kpResp.setStatus("TXN");
//            kpResp.setMessage("Transaction successful");
//            kpResp.setTxnid(uniqueTxnId);
//            kpResp.setUtr("UTR1234567890");

            // 🧩 Handle based on gateway response
//            switch (kpResp.getStatus().toUpperCase()) {
//                case "TXN":
                    // TXN treated as success: deduct wallet & save record
                    Wallet updatedWallet = walletService.processWithdrawal(dto, kpResp);
                    return responseService.generateSuccessResponse("Withdrawal initiated; final status updates via callback.", updatedWallet, HttpStatus.OK);

//                case "FAILED":
//                    return responseService.generateErrorResponse("Gateway payout failed: " + kpResp.getMessage(), HttpStatus.BAD_REQUEST);
//                case "PENDING":
//                    Wallet pendingWallet = walletService.processWithdrawal(dto, kpResp);
//                    return responseService.generateSuccessResponse("Withdrawal initiated; final status updates via callback.", pendingWallet, HttpStatus.OK);
//                default:
//                    throw new BusinessException("Gateway payout failed: " + kpResp.getMessage(), HttpStatus.BAD_REQUEST);
//            }

        } catch (BusinessException be) {
            return responseService.generateErrorResponse(be.getMessage(), HttpStatus.valueOf(be.getStatusCode()));
        } catch (Exception e) {
            return responseService.generateErrorResponse("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /*@PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody KwickPayCallbackDto callbackDto) {
        String gatewayTxnId = callbackDto.getApitxnid();
        String status = callbackDto.getStatus(); // e.g., "TXN", "FAILED"

        CustomerWithdrawalRequest withdrawal = withdrawalRepo.findByGatewayTxnId(gatewayTxnId);
        if (withdrawal == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Withdrawal not found");

        if ("TXN".equalsIgnoreCase(status)) {
            withdrawal.setStatus(WithdrawalStatus.PAID);
        } else if ("FAILED".equalsIgnoreCase(status)) {
            withdrawal.setStatus(WithdrawalStatus.FAILED);

            // Refund wallet
            Wallet wallet = walletRepository.findByCustomCustomer_Id(withdrawal.getCustomer().getId());
            if (wallet != null) {
                wallet.setWinningAmount(wallet.getWinningAmount().add(withdrawal.getAmount()));
                walletRepository.save(wallet);
            }
        }

        withdrawal.setGatewayMessage("Callback: " + status);
        withdrawalRepo.save(withdrawal);

        return ResponseEntity.ok("Callback processed");
    }
*/


    @GetMapping("/withdrawalRequests/{customerId}")
    public ResponseEntity<?> getWithdrawalRequestsByCustomerId(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) WithdrawalType withdrawalType,
            @RequestParam(required = false) WithdrawalStatus status) {
        try {
            // Check if customer exists
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestDate"));

            // Call repository with filters
            Page<CustomerWithdrawalRequest> withdrawalRequests = customerWithdrawalRequestRepository
                    .findByCustomerIdAndFilters(customerId, withdrawalType, status, pageable);

            if (withdrawalRequests.isEmpty()) {
                return responseService.generateSuccessResponse("No withdrawal requests found", withdrawalRequests.getContent(), HttpStatus.OK);
            }

            return responseService.generateSuccessResponseWithCount("Withdrawal requests fetched successfully",
                    withdrawalRequests.getContent(),
                    withdrawalRequests.getTotalElements(),
                    HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching withdrawal requests: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
