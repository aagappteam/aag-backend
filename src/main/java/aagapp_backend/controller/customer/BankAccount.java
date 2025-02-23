package aagapp_backend.controller.customer;

import aagapp_backend.dto.BankAccountDTO;
import aagapp_backend.entity.BankDetails;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.services.BankAccountService;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("user-bank")
public class BankAccount {


    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    /**
     * The Entity manager.
     */
    @Autowired
    EntityManager entityManager;

    @Autowired
    private CustomCustomerService customerService;


    @PostMapping("/add/{customerId}")
    public ResponseEntity<?> addBankAccount(
            @PathVariable Long customerId,

            @RequestBody @Valid BankAccountDTO bankAccountDTO) {

        try {
            if (customerId == null) {
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            }

            CustomCustomer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            }

            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
            String result = bankAccountService.addBankAccount(customCustomer, bankAccountDTO);

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



    /**
     * Gets bank accounts by customer id.
     *
     * @param customerId the customer id
     * @return the bank accounts by customer id
     */
    @GetMapping("/get/{customerId}")
    public ResponseEntity<?> getBankAccountsByCustomerId(@PathVariable Long customerId) {
        try{
            if (customerId == null) {
                return ResponseService.generateErrorResponse("Customer Id not specified", HttpStatus.BAD_REQUEST);
            }

            CustomCustomer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            }

            List<BankAccountDTO> bankAccounts = bankAccountService.getBankAccountsByCustomerId(customerId);

            if (bankAccounts.isEmpty()) {
                return ResponseService.generateErrorResponse("No bank accounts found for this customer", HttpStatus.NOT_FOUND);
            }

            return ResponseService.generateSuccessResponse("Bank accounts fetched successfully!", bankAccounts, HttpStatus.OK);

        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }    }

    @PutMapping("/update/{accountId}")
    public ResponseEntity<?> updateBankAccount(
            @PathVariable Long accountId,
            @RequestBody Map<String, Object> params) {
        try {
            if (accountId == null) {
                return ResponseService.generateErrorResponse("Account ID is required", HttpStatus.BAD_REQUEST);
            }

            Optional<BankDetails> existingAccount = bankAccountService.getBankAccountById(accountId);
            if (!existingAccount.isPresent()) {
                return ResponseService.generateErrorResponse("Bank account not found for this Id", HttpStatus.NOT_FOUND);
            }

            // Pass the params map to the service method for updating
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

            Optional<BankDetails> updatedAccount = bankAccountService.getBankAccountById(accountId);
            if (updatedAccount.isEmpty()) {
                return ResponseService.generateErrorResponse("Updated bank account not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Return the updated account as DTO (optional)
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
    @DeleteMapping("/delete/{accountId}")
    public ResponseEntity<?> deleteBankAccount(@PathVariable Long accountId) {
        try {
            if (accountId == null) {
                return ResponseService.generateErrorResponse("Account ID is required", HttpStatus.BAD_REQUEST);
            }


            Optional<BankDetails> existingAccount = bankAccountService.getBankAccountById(accountId);
            if (!existingAccount.isPresent()) {
                return ResponseService.generateErrorResponse("Bank account not found for this Id", HttpStatus.NOT_FOUND);
            }

            String result = bankAccountService.deleteBankAccount(accountId);
            if (result.equals("Account deletion failed")) {
                return ResponseService.generateErrorResponse("Failed to delete bank account", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return ResponseService.generateSuccessResponse("Bank account deleted successfully!", null, HttpStatus.NO_CONTENT);

        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}
