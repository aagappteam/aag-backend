package aagapp_backend.controller.wallet;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.AddBalanceRequest;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.NotificationType;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.vendor.VenderService;
import aagapp_backend.services.wallet.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

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


            // Extract user ID from the token and validate it
            Long userId = jwtUtil.extractId(token);
//            get Role from token
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
            notification.setDetails(amount + "added to Wallet"); // Example NotificationType for a successful

            notificationRepository.save(notification);

            // Return success response
            return responseService.generateSuccessResponse("Balance added successfully", updatedWallet, HttpStatus.OK);
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
            if (!userId.equals(customerId)) {
                return responseService.generateErrorResponse("You are not authorized to perform this action", HttpStatus.FORBIDDEN);
            }

            // Call the wallet service to retrieve the balance
            ResponseEntity<?> wallet = walletService.getBalanceToWallet(customerId);
            if (wallet == null) {
                return responseService.generateResponse(HttpStatus.OK, "No wallet found for the user" ,null);

            }
            return wallet;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving balance: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deductAmount")
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
            float updatedWallet = (float) walletService.deductAmountFromWallet(customerId, amount);


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
            notification.setType(NotificationType.WALLET_DEBIT);  // Example NotificationType for a successful payment
*/
            notification.setDescription("Wallet balance deducted"); // Example NotificationType for a successful
            notification.setAmount((double) amount);
            notification.setDetails(amount + "debited from Wallet"); // Example NotificationType for a successful

            notificationRepository.save(notification);
            return responseService.generateSuccessResponse("Balance deducted successfully", updatedWallet, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error deducting balance: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/withdrawalAmount")
    public ResponseEntity<?> withdrawalAmount(@RequestBody AddBalanceRequest addBalanceRequest, @RequestHeader(value = "Authorization") String authorization) {
        try{
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }

            String token = authorization.substring(7);
            Long customerId = addBalanceRequest.getCustomerId();

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
            // Call the wallet service to withdraw the amount
            ResponseEntity<?> updatedWallet = walletService.withdrawalAmountFromWallet(customerId, amount);

            return responseService.generateSuccessResponse("Balance withdrawn successfully", updatedWallet, HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error withdrawing balance: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
