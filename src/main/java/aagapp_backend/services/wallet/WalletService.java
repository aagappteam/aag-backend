package aagapp_backend.services.wallet;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.CustomerWithdrawalRequestDto;
import aagapp_backend.dto.KwickPayResponse;
import aagapp_backend.dto.WalletBalanceDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.entity.withdrawrequest.CustomerWithdrawalRequest;
import aagapp_backend.entity.withdrawrequest.WithdrawalRequest;
import aagapp_backend.enums.VendorStatus;
import aagapp_backend.enums.WithdrawalStatus;
import aagapp_backend.enums.WithdrawalType;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.repository.withdrawrequest.CustomerWithdrawalRequestRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.NotificationService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WalletService {

    private WalletRepository walletRepository;
    private CustomCustomerService customCustomerService;
    private ExceptionHandlingService exceptionHandlingService;
    private ResponseService responseService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private CustomerWithdrawalRequestRepository customerWithdrawalRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    public WalletService(WalletRepository walletRepository,
                         CustomCustomerService customCustomerService,
                         ExceptionHandlingService exceptionHandlingService,
                         ResponseService responseService) {
        this.walletRepository = walletRepository;
        this.customCustomerService = customCustomerService;
        this.exceptionHandlingService = exceptionHandlingService;
        this.responseService = responseService;
    }


    public Wallet addBalanceToWallet(Long customerId, float amountToAdd, boolean isTest) {
        try {

            // Retrieve the customer by ID
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);

            if (customer == null) {
                throw new BusinessException("Customer not found for the given ID: " + customerId , HttpStatus.BAD_REQUEST);
            }

            // Retrieve the wallet associated with the customer
            Wallet wallet = walletRepository.findByCustomCustomer(customer);
            if(isTest){
                wallet.setIsTest(true);
            }
            wallet.setUnplayedBalance(wallet.getUnplayedBalance() + amountToAdd);
            // Save the updated wallet and return it
            wallet = walletRepository.save(wallet);

            return wallet;
        }catch (BusinessException e){
            exceptionHandlingService.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        }
            catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while adding balance to wallet", e);
        }
    }


    public ResponseEntity<?> getBalanceToWallet(Long customerId) {
        try {
            // Retrieve the customer by ID
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("Customer not found for the given ID: " + customerId, HttpStatus.NOT_FOUND);
            }


            // Retrieve the wallet associated with the customer
            Wallet wallet = walletRepository.findByCustomCustomer(customer);
            if (wallet == null) {
                return responseService.generateErrorResponse("No wallet found for the customer with ID: " + customerId, HttpStatus.NOT_FOUND);
            }

            // Convert to DTO
            WalletBalanceDTO dto = new WalletBalanceDTO(
                    wallet.getWalletId(),
                    wallet.getUnplayedBalance(),
                    wallet.getWinningAmount(),
                    customer.getBonusBalance(),
                    wallet.getIsTest(),
                    wallet.getCreatedAt(),
                    wallet.getUpdatedAt()
            );

            return responseService.generateSuccessResponse("Wallet balance retrieved successfully", dto, HttpStatus.OK);
        }catch (BusinessException e){
            exceptionHandlingService.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error occurred while retrieving wallet balance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*@Transactional
    public Wallet deductAmountFromWallet(Long customerId, Float deducedAmount) {
        try {
            // Retrieve the customer by ID
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);
            if (customer == null) {
                throw new BusinessException("Customer not found for the given ID: " + customerId, HttpStatus.BAD_REQUEST);
            }

            // Retrieve the wallet associated with the customer
            Wallet wallet = walletRepository.findByCustomCustomer(customer);
            if (wallet == null) {
                throw new BusinessException("No wallet found for the customer", HttpStatus.BAD_REQUEST);
            }

            Double unplayedBalance = wallet.getUnplayedBalance();
            BigDecimal winningAmount = wallet.getWinningAmount();
            double totalAvailable = unplayedBalance + winningAmount.doubleValue();

            if (deducedAmount > totalAvailable) {
                throw new BusinessException("Insufficient balance in the wallet", HttpStatus.BAD_REQUEST);
            }

            // Deduct from unplayed first
            if (deducedAmount <= unplayedBalance) {
                wallet.setUnplayedBalance(unplayedBalance - deducedAmount);
            } else {
                double remainingAmount = deducedAmount - unplayedBalance;

                // Set unplayed balance to 0
                wallet.setUnplayedBalance(0.0);

                // Deduct remaining from winning amount
                BigDecimal newWinningAmount = winningAmount.subtract(BigDecimal.valueOf(remainingAmount));
                wallet.setWinningAmount(newWinningAmount);
            }

            // Save the updated wallet
            walletRepository.save(wallet);

            return wallet;

        } catch (BusinessException e) {
            exceptionHandlingService.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while deducting balance from wallet", e);
        }
    }*/


    // ✅ Validate customer
    public CustomCustomer validateCustomer(Long customerId, String jwtToken) {
        CustomCustomer customer = customCustomerService.getCustomerById(customerId);
        if (customer == null) throw new BusinessException("Customer not found", HttpStatus.NOT_FOUND);
        if (customer.getStatus() != VendorStatus.ACTIVE) throw new BusinessException("You are Suspended or Blocked", HttpStatus.BAD_REQUEST);
        Long tokenUserId = jwtUtil.extractId(jwtToken);
        if (!customerId.equals(tokenUserId)) throw new BusinessException("Unauthorized", HttpStatus.FORBIDDEN);

        return customer;
    }

    public void isEnoughAmount(float amount, Long customerId) {
        Wallet wallet = walletRepository.findByCustomCustomer_Id(customerId);
        if (wallet == null) throw new BusinessException("Wallet not found", HttpStatus.NOT_FOUND);
        BigDecimal requestedAmount = BigDecimal.valueOf(amount);
        if (wallet.getWinningAmount().compareTo(requestedAmount) < 0) {
            throw new BusinessException("Insufficient balance in the wallet", HttpStatus.BAD_REQUEST);
        }
    }

    // ✅ Check daily withdrawal limit
    public void checkDailyLimit(Long customerId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        int count = customerWithdrawalRequestRepository.countByCustomerIdAndRequestDateBetween(customerId, start, end);
        if (count >= 3) throw new BusinessException("Max 3 withdrawals per day", HttpStatus.BAD_REQUEST);
    }

    // ✅ Validate amount constraints
    public void validateAmount(float amount) {
        if (amount <= 0) throw new BusinessException("Amount must be > 0", HttpStatus.BAD_REQUEST);
        if (amount < 106) throw new BusinessException("Minimum withdrawal Rs.106", HttpStatus.BAD_REQUEST);
        if (amount > 1000000) throw new BusinessException("Amount too large", HttpStatus.BAD_REQUEST);
    }

    // ✅ Send request to KwickPay gateway
    public KwickPayResponse callPayoutGateway(CustomerWithdrawalRequestDto dto,  String txnId, CustomCustomer customer) {

        try {

                Map<String, Object> body = new HashMap<>();
                body.put("token", Constant.KwickPayToken);
                body.put("transactionType", Constant.KwickPayTransactionType);
                body.put("apitxnid", txnId);
                body.put("amount", dto.getAmount().intValue());
                body.put("firstName", dto.getAccountHolderFirstName());
                body.put("lastName", dto.getAccountHolderLastName());
                body.put("email", customer.getEmail());
                body.put("mobile", customer.getMobileNumber());
                body.put("mode", Constant.KwickPayTransactionMode);
                body.put("accountNumber", dto.getAccountNumber());
                body.put("ifsc", dto.getIfscCode());
                body.put("bank", dto.getBankName());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);


            ResponseEntity<KwickPayResponse> response = restTemplate.postForEntity(Constant.KwickPayUrl, entity, KwickPayResponse.class);
            KwickPayResponse respBody = response.getBody();

            if (respBody != null && "ERR".equalsIgnoreCase(respBody.getStatuscode())) {
                String msg = respBody.getMessage();
                    throw new BusinessException("KwickPay Error: " + msg, HttpStatus.BAD_REQUEST);
            }

            CustomerWithdrawalRequest withdrawal = new CustomerWithdrawalRequest();
            withdrawal.setClientId(txnId);
            customerWithdrawalRequestRepository.save(withdrawal);

            return respBody;
        } catch (Exception e) {
//            e.printStackTrace();
            throw new BusinessException("Failed to connect to KwickPay: " + e.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    // ✅ Process transaction only if KwickPay returns TXN
    @Transactional
    public Wallet processWithdrawal(CustomerWithdrawalRequestDto dto,  String uniqueClientId) {
        Wallet wallet = walletRepository.findByCustomCustomer_Id(dto.getCustomerId());
        if (wallet == null) throw new BusinessException("No wallet found", HttpStatus.NOT_FOUND);

        BigDecimal requestedAmount = BigDecimal.valueOf(dto.getAmount());

        wallet.setWinningAmount(wallet.getWinningAmount().subtract(requestedAmount));
        walletRepository.save(wallet);


        // Save request regardless of TXN/PENDING (don't save if gateway fails completely)
        CustomerWithdrawalRequest withdrawal = customerWithdrawalRequestRepository.findByClientId(uniqueClientId);
        withdrawal.setCustomer(customCustomerService.getCustomerById(dto.getCustomerId()));
        withdrawal.setAmount(requestedAmount);
        withdrawal.setAccountNumber(dto.getAccountNumber());
        withdrawal.setIfscCode(dto.getIfscCode());
        withdrawal.setBankName(dto.getBankName());
        withdrawal.setAccountHolderName(dto.getAccountHolderFirstName() + " " + dto.getAccountHolderLastName());
//        withdrawal.setWithdrawalType(dto.getWithdrawalType());
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        withdrawal.setClientId(uniqueClientId);

        customerWithdrawalRequestRepository.save(withdrawal);

        // Notify
        Notification n = new Notification();
        n.setRole("Customer");
        n.setCustomerId(dto.getCustomerId());
        n.setDescription("Withdrawal Request Submitted");
        n.setDetails("Your Rs." + dto.getAmount() + " " + "dto.getWithdrawalType()" + " withdrawal is " + withdrawal.getStatus());
        n.setAmount(dto.getAmount().doubleValue());
        notificationRepository.save(n);

        return wallet;
    }


    @Transactional
    public void refundAmountToWallet(String clientTxnId, float amount) {
        CustomerWithdrawalRequest withdrawalRequest = customerWithdrawalRequestRepository.findByClientId(clientTxnId);
        if (withdrawalRequest == null) throw new BusinessException("No withdrawal request found", HttpStatus.NOT_FOUND);
        if (withdrawalRequest.getStatus() != WithdrawalStatus.FAILED) throw new BusinessException("Withdrawal request is not failed", HttpStatus.BAD_REQUEST);
        Long customerId = withdrawalRequest.getCustomer().getId();
        Wallet wallet = walletRepository.findByCustomCustomer_Id(customerId);
        if (wallet == null) throw new BusinessException("Wallet not found for refund", HttpStatus.NOT_FOUND);

        BigDecimal refundAmount = BigDecimal.valueOf(amount);
        wallet.setWinningAmount(wallet.getWinningAmount().add(refundAmount));
        walletRepository.save(wallet);

        // Notify the customer about the refund
        Notification notification = new Notification();
        notification.setRole("Customer");
        notification.setCustomerId(customerId);
        notification.setDescription("Withdrawal Refunded");
        notification.setDetails("Refund of Rs." + amount + " processed due to failure.");
        notification.setAmount((double) amount);
        notificationRepository.save(notification);
    }

}
