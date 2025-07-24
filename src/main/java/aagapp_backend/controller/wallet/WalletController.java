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
import aagapp_backend.services.bonus.BonusOfferService;
import aagapp_backend.services.dashboard.CouponService;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.referal.ReferralService;
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
    private ReferralService referralService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private BonusOfferService bonusOfferService;

    @Autowired
    private CustomCustomerService customCustomerService;


    @Autowired
    private RoleService roleService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ExceptionHandlingService exceptionHandling;



    @Autowired
    private VenderService vendorService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private InvoiceServiceAdmin invoiceServiceAdmin;

    @Autowired
    private CustomerWithdrawalRequestRepository customerWithdrawalRequestRepository;

    // Endpoint to add balance to the wallet
/*    @PostMapping("/addBalance")
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


            notification.setDescription("Wallet balance added"); // Example NotificationType for a successful
            notification.setAmount((double) amount);
//            notification.setDetails("Rs. " +amount + " added to Wallet"); // Example NotificationType for a successful
            BigDecimal formattedAmount = BigDecimal.valueOf(amount).stripTrailingZeros();
            notification.setDetails("Rs. " + formattedAmount.toPlainString() + " added to Wallet");

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
    }*/

    @PostMapping("/addBalance")
    public ResponseEntity<?> addBalance(@RequestBody AddBalanceRequest addBalanceRequest,
                                        @RequestHeader(value = "Authorization") String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
            }

            String token = authorization.substring(7);
            Long customerId = addBalanceRequest.getCustomerId();
            float amount = addBalanceRequest.getAmount();
            boolean isTest = addBalanceRequest.getIsTest();
            String couponCode = addBalanceRequest.getCouponCode(); // Optional

            // Validate user status
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);
            if (customer.getStatus() != VendorStatus.ACTIVE) {
                throw new BusinessException("You are Suspended or Blocked", HttpStatus.BAD_REQUEST);
            }

            // Validate token & user
            Long userId = jwtUtil.extractId(token);
            Integer role = jwtUtil.extractRoleId(token);

            if (userId == null) {
                return responseService.generateErrorResponse("Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            if (!userId.equals(customerId)) {
                return responseService.generateErrorResponse("You are not authorized to perform this action", HttpStatus.FORBIDDEN);
            }

            if (amount <= 0) {
                return responseService.generateErrorResponse("Amount must be greater than 0", HttpStatus.BAD_REQUEST);
            }

            // Check and apply download bonus if first recharge
            if (customCustomerService.isFirstRecharge(customerId)) {
                float downloadBonus = Constant.DOWNLOAD_BONUS;
                customCustomerService.addBonusAndUpdateCoupon(customerId, downloadBonus, "AAG_DOWNLOAD");

                Notification downloadBonusNotification = new Notification();
                downloadBonusNotification.setRole("Customer");
                downloadBonusNotification.setCustomerId(customerId);
                downloadBonusNotification.setDescription("Welcome Bonus!");
                downloadBonusNotification.setAmount((double) downloadBonus);
                downloadBonusNotification.setDetails(Constant.DOWNLOAD_BONUS_DESCRIPTION);
                notificationRepository.save(downloadBonusNotification);


                String referredBy = customer.getReferredBy();
                if (referredBy != null && !referredBy.trim().isEmpty()) {
                    referralService.updateReferrerEarnings(referredBy);
                }
            }


            Wallet updatedWallet = walletService.addBalanceToWallet(customerId, amount, isTest);

            float bonusAmount = 0f;
            if (amount == 200) {
                bonusAmount = 100;
//                appliedCoupon = "AUTO_50PERCENT_200";
            } else if (amount == 500) {
                bonusAmount = 500;
//                appliedCoupon = "AUTO_100PERCENT_500";
            } else if (amount == 1000) {
                bonusAmount = 500;
//                appliedCoupon = "AUTO_FLAT500_1000";
            }



            // Add bonus and save coupon code
            if (bonusAmount > 0) {
                customCustomerService.addBonusAndUpdateCoupon(customerId, bonusAmount, couponCode);

                // Send bonus notification
                Notification bonusNotification = new Notification();
                bonusNotification.setRole("Customer");
                bonusNotification.setCustomerId(customerId);
                bonusNotification.setDescription("Bonus Added!");
                bonusNotification.setAmount((double) bonusAmount);
                BigDecimal formattedBonus = BigDecimal.valueOf(bonusAmount).stripTrailingZeros();
                bonusNotification.setDetails("You received Rs. " + formattedBonus.toPlainString() + " as bonus");
                notificationRepository.save(bonusNotification);
            }

            // Notification for wallet balance
            Notification notification = new Notification();
            notification.setRole(role == Constant.VENDOR_ROLE ? "Vendor" : "Customer");

            if (role == Constant.VENDOR_ROLE) {
                VendorEntity vendor = vendorService.getServiceProviderById(userId);
                notification.setVendorId(vendor.getService_provider_id());
            } else {
                notification.setCustomerId(customerId);
            }

            notification.setDescription("Wallet balance added");
            notification.setAmount((double) amount);
            BigDecimal formattedAmount = BigDecimal.valueOf(amount).stripTrailingZeros();
            notification.setDetails("Rs. " + formattedAmount.toPlainString() + " added to Wallet");
            notificationRepository.save(notification);

            // Generate invoice
            invoiceServiceAdmin.createInvoiceForCustomer((double) amount, customerId);

            return responseService.generateSuccessResponse("Balance added successfully", updatedWallet, HttpStatus.OK);

        } catch (BusinessException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
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

            walletService.validateCustomer(dto.getCustomerId(), token);
            walletService.isEnoughAmount(dto.getAmount(), dto.getCustomerId());
            walletService.checkDailyLimit(dto.getCustomerId());
            walletService.validateAmount(dto.getAmount());

            String uniqueTxnId = "AAG" + System.currentTimeMillis();

            // Secure API call — will throw BusinessException on failure
//            walletService.callPayoutGateway(dto,uniqueTxnId , customer);

            Wallet updatedWallet = walletService.processWithdrawal(dto, uniqueTxnId);
            return responseService.generateSuccessResponse("Withdrawal initiated; final status updates via callback.", updatedWallet, HttpStatus.OK);


        } catch (BusinessException be) {
            return responseService.generateErrorResponse(be.getMessage(), HttpStatus.valueOf(be.getStatusCode()));
        } catch (Exception e) {
            return responseService.generateErrorResponse("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




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
