package aagapp_backend.services.wallet;

import aagapp_backend.dto.WalletBalanceDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.BusinessException;
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

    @Transactional
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
    }



    @Transactional
    public Wallet withdrawalAmountFromWallet(Long customerId, Float withdrawBalance) {
        try {
            CustomCustomer customer = customCustomerService.getCustomerById(customerId);
            if (customer == null) {
                throw new BusinessException("Customer not found for the given ID: " + customerId, HttpStatus.BAD_REQUEST);
            }

            // Retrieve the wallet associated with the customer
            Wallet wallet = walletRepository.findByCustomCustomer(customer);
            if (wallet == null) {
                throw new BusinessException("No wallet found for the customer", HttpStatus.BAD_REQUEST);
            }


            BigDecimal withdrawBalanceBD = new BigDecimal(withdrawBalance);

            if (withdrawBalanceBD.compareTo(wallet.getWinningAmount()) > 0) {
                throw new BusinessException("Insufficient balance in the wallet" , HttpStatus.BAD_REQUEST);
            }

            // Withdraw the balance from the wallet
            wallet.setWinningAmount(wallet.getWinningAmount().subtract(withdrawBalanceBD));

            // Save the updated wallet
            walletRepository.save(wallet);

            return wallet;
        } catch (BusinessException e){
            exceptionHandlingService.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        }

        catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while withdrawing balance from wallet", e);
        }
    }
}
