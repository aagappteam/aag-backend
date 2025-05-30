package aagapp_backend.services.wallet;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    private WalletRepository walletRepository;
    private CustomCustomerService customCustomerService;
    private ExceptionHandlingService exceptionHandlingService;
    private ResponseService responseService;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

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
            if(isTest){
                wallet.setIsTest(true);
            }
            // Add the balance to the wallet
            wallet.setUnplayedBalance(wallet.getUnplayedBalance() + amountToAdd);

            // Save the updated wallet and return it
            wallet = walletRepository.save(wallet);

            return wallet;
        } catch (Exception e) {
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

            return responseService.generateSuccessResponse("Wallet balance retrieved successfully", wallet, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error occurred while retrieving wallet balance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public Wallet deductAmountFromWallet(Long customerId, Float deducedAmount) {
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

            if (deducedAmount > wallet.getUnplayedBalance()) {
                throw new RuntimeException("Insufficient balance in the wallet");
            }

            // Deduct the balance from the wallet
            wallet.setUnplayedBalance(wallet.getUnplayedBalance() - deducedAmount);

            // Save the updated wallet
            walletRepository.save(wallet);

            // Return the updated balance
            return wallet;

        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while deducting balance from wallet", e);
        }
    }


    @Transactional
    public Wallet withdrawalAmountFromWallet(Long customerId, Float withdrawBalance) {
        try {
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);
            if (customer == null) {
                throw new RuntimeException("Customer not found for the given ID: " + customerId);
            }

            // Retrieve the wallet associated with the customer
            Wallet wallet = walletRepository.findByCustomCustomer(customer);
            if (wallet == null) {
                throw new RuntimeException("No wallet found for the customer");
            }


            BigDecimal withdrawBalanceBD = new BigDecimal(withdrawBalance);

            if (withdrawBalanceBD.compareTo(wallet.getWinningAmount()) > 0) {
                throw new RuntimeException("Insufficient balance in the wallet");
            }

// Withdraw the balance from the wallet
            wallet.setWinningAmount(wallet.getWinningAmount().subtract(withdrawBalanceBD));

            // Save the updated wallet
            walletRepository.save(wallet);

            return wallet;
        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while withdrawing balance from wallet", e);
        }
    }
}
