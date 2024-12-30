package aagapp_backend.services.wallet;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WalletService {

    private WalletRepository walletRepository;
    private CustomCustomerService customCustomerService;
    private ExceptionHandlingService exceptionHandlingService;
    private ResponseService responseService;

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
                throw new IllegalStateException("Customer not found for the given ID: " + customerId);
            }


            // Retrieve the wallet associated with the customer
            Wallet wallet = walletRepository.findByCustomCustomer(customer);
            if (wallet == null) {
                // If wallet doesn't exist, create a new one
                wallet = new Wallet();
                wallet.setCustomCustomer(customer);
                wallet.setUnplayedBalance(0); // Initial balance
            }
            if (isTest) {
                //implement this logic according to realtime wallet
                //TODO: implement
                System.out.println("wallet created  successfully for test wallet " + customer);
            } else {
                //TODO: implement
                System.out.println("wallet created successfully for test wallet ");
            }

            // Add the balance to the wallet
            wallet.setUnplayedBalance(wallet.getUnplayedBalance() + amountToAdd);

            // Save the updated wallet and return it
            wallet = walletRepository.save(wallet);

            return wallet;
        } catch (Exception e) {
            // Log the error for debugging purposes
//            logger.error("Error occurred while adding balance to wallet for customer ID: " + customerId, e);
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

            Map<String, Object> balanceData = new HashMap<>();
            balanceData.put("unplayedBalance", wallet.getUnplayedBalance());
            balanceData.put("bonusBalance", customer.getBonusBalance());
            balanceData.put("winningAmount", wallet.getWinningAmount());

            // Return the wallet balance
            return responseService.generateSuccessResponse("Wallet balance retrieved successfully", balanceData, HttpStatus.OK);
        } catch (Exception e) {
//            logger.error("Error occurred while retrieving wallet balance for customer ID: " + customerId, e);
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error occurred while retrieving wallet balance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> deductAmountFromWallet(Long customerId, Float deducedAmount) {
        try {
            // Retrieve the customer by ID
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);
            if (customer == null) {
                throw new RuntimeException("Customer not found for the given ID: " + customerId);
            }


            // Retrieve the wallet associated with the customer
            Wallet wallet = walletRepository.findByCustomCustomer(customer);
            if (wallet == null) {
                throw new RuntimeException("No wallet found for the customer");
            }

            // Validate balance before deduction
            if (deducedAmount > wallet.getUnplayedBalance()) {
                return responseService.generateErrorResponse("Insufficient balance in the wallet", HttpStatus.BAD_REQUEST);
            }

            // Apply concurrency control (e.g., optimistic locking) or transaction management here if needed

            // Deduct the balance from the wallet
            wallet.setUnplayedBalance(wallet.getUnplayedBalance() - deducedAmount);

            // Save the updated wallet
            walletRepository.save(wallet);

            // Return the updated wallet balance
            return responseService.generateSuccessResponse("Wallet amount deducted", Float.toString(wallet.getUnplayedBalance()), HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while deducting balance from wallet", e);
        }
    }

    public ResponseEntity<?> withdrawalAmountFromWallet(Long customerId, Float withdrawBalance) {
        try {
            // Retrieve the customer by ID
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);
            if (customer == null) {
                throw new RuntimeException("Customer not found for the given ID: " + customerId);
            }

            // Retrieve the wallet associated with the customer
            Wallet wallet = walletRepository.findByCustomCustomer(customer);
            if (wallet == null) {
                throw new RuntimeException("No wallet found for the customer");
            }

            // Validate balance before withdrawal
            if (withdrawBalance > wallet.getWinningAmount()) {
                return responseService.generateErrorResponse("Insufficient balance in the wallet", HttpStatus.BAD_REQUEST);
            }

            // Apply concurrency control (e.g., optimistic locking) or transaction management here if needed

            // Withdraw the balance from the wallet
            wallet.setWinningAmount(wallet.getWinningAmount() - withdrawBalance);

            // Save the updated wallet
            walletRepository.save(wallet);

            // Return the updated wallet balance
            return responseService.generateSuccessResponse("Wallet amount withdrawn", Float.toString(wallet.getUnplayedBalance()), HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while withdrawing balance from wallet", e);
        }
    }
}
