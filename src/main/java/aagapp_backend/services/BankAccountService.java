package aagapp_backend.services;

import aagapp_backend.dto.BankAccountDTO;
import aagapp_backend.entity.BankDetails;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class BankAccountService {


    /**
     * The Entity manager.
     */
    @Autowired
    EntityManager entityManager;

    @Autowired
    ExceptionHandlingImplement exceptionHandlingImplement;

    /**
     * Add bank account string.
     *
     * @param customCustomer the custom customer
     * @param bankAccountDTO the bank account dto
     * @return the string
     */
    @Transactional
    public String addBankAccount(CustomCustomer customCustomer, BankAccountDTO bankAccountDTO) {
        try {
            if (isAccountNumberExists(bankAccountDTO.getAccountNumber(), null)) {
                return "Account number already exists.";
            }

            if (!bankAccountDTO.getAccountNumber().equals(bankAccountDTO.getReEnterAccountNumber())) {
                return "Account numbers do not match.";
            }

            BankDetails bankDetails = new BankDetails();
            bankDetails.setId(bankAccountDTO.getId());
            bankDetails.setCustomerName(bankAccountDTO.getCustomerName());
            bankDetails.setAccountNumber(bankAccountDTO.getAccountNumber());
            bankDetails.setCustomer(customCustomer);
            bankDetails.setIfscCode(bankAccountDTO.getIfscCode());
            bankDetails.setBankName(bankAccountDTO.getBankName());
            bankDetails.setBranchName(bankAccountDTO.getBranchName());
            bankDetails.setAccountType(bankAccountDTO.getAccountType());

            entityManager.persist(bankDetails);

            Long generatedId = bankDetails.getId();

            return "Bank account added successfully! ID: " + generatedId;
        } catch (Exception e) {
            e.printStackTrace();
            exceptionHandlingImplement.handleException(e);
            return "Error adding bank account: " + e.getMessage();
        }
    }


    /**
     * Delete bank account string.
     *
     * @param accountId the account id
     * @return the string
     */
    @Transactional
    public String deleteBankAccount(Long accountId) {
        try {
            BankDetails bankDetails = entityManager.find(BankDetails.class, accountId);
            if (bankDetails != null) {
                entityManager.remove(bankDetails); // Delete the bank account
                return "Bank account deleted successfully!";
            } else {
                return "Bank account not found!";
            }
        } catch (Exception e) {
            e.printStackTrace();
            exceptionHandlingImplement.handleException(e);
            return "Error deleting bank account: " + e.getMessage();
        }
    }

    /* *//**
     * Gets bank account by id.
     *
     * @param accountId the account id
     * @return the bank account by id
     */
    @Transactional

    public Optional<BankDetails> getBankAccountById(Long accountId) {
        try {
            if (accountId == null) {
                return Optional.empty();
            }

            BankDetails bankDetails = entityManager.find(BankDetails.class, accountId);
            if (bankDetails == null) {
                return Optional.empty();
            }
            if (bankDetails != null) {

                return Optional.of(bankDetails);
            }
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            exceptionHandlingImplement.handleException(e);
            return Optional.empty();
        }
    }

    @Transactional
    public String updateBankAccount(Long accountId, Map<String, Object> params) {
        try {
            // Fetch the existing account from the database
            BankDetails existingAccount = entityManager.find(BankDetails.class, accountId);
            if (existingAccount == null) {
                return "Account update failed. Account not found.";
            }

            // Handle account number validation if provided
            String accountNumber = (String) params.get("accountNumber");
            String reEnterAccountNumber = (String) params.get("reEnterAccountNumber");

            // Check if account numbers match
            if (accountNumber != null && reEnterAccountNumber != null && !accountNumber.equals(reEnterAccountNumber)) {
                return "Account numbers do not match.";
            }

            // If account number is present, check if it already exists
            if (accountNumber != null && isAccountNumberExists(accountNumber, accountId)) {
                return "Account number already exists.";
            }

            // Use reflection to set the fields dynamically
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                if (value != null) {
                    // Construct the setter method name (e.g., setAccountNumber for accountNumber field)
                    String setterMethodName = "set" + StringUtils.capitalize(fieldName);
                    try {
                        // Find the corresponding setter method for the field and invoke it
                        Method setterMethod = BankDetails.class.getMethod(setterMethodName, value.getClass());
                        setterMethod.invoke(existingAccount, value);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return "Account update failed. Error updating field: " + fieldName;
                    }
                }
            }

            // Merge the updated account entity
            entityManager.merge(existingAccount);

            return "Bank account updated successfully!";
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return "Account update failed: " + e.getMessage();
        }
    }


    public BankAccountDTO convertToDTO(BankDetails bankDetails) {
        BankAccountDTO dto = new BankAccountDTO();
        dto.setId(bankDetails.getId());
        dto.setCustomerName(bankDetails.getCustomerName());
        dto.setAccountNumber(bankDetails.getAccountNumber());
        dto.setIfscCode(bankDetails.getIfscCode());
        dto.setBankName(bankDetails.getBankName());
        dto.setBranchName(bankDetails.getBranchName());
        dto.setAccountType(bankDetails.getAccountType());
        return dto;
    }

    /**
     * Check if the account number already exists, excluding the current account being updated.
     *
     * @param accountNumber the account number to check
     * @param accountId the ID of the account being updated (null for new accounts)
     * @return true if the account number exists, false otherwise
     */
    private boolean isAccountNumberExists(String accountNumber, Long accountId) {
        try {
            List<BankDetails> duplicateAccounts = entityManager.createQuery(
                            "SELECT b FROM BankDetails b WHERE b.accountNumber = :accountNumber", BankDetails.class)
                    .setParameter("accountNumber", accountNumber)
                    .getResultList();

            if (accountId != null) {
                duplicateAccounts.removeIf(bankDetails -> bankDetails.getId().equals(accountId));
            }

            return !duplicateAccounts.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            exceptionHandlingImplement.handleException(e);

            return false;
        }
    }

    /**
     * Gets bank accounts by customer id.
     *
     * @param customerId the customer id
     * @return the bank accounts by customer id
     */
    @Transactional

    public List<BankAccountDTO> getBankAccountsByCustomerId(Long customerId) {
        try {
            List<BankDetails> bankDetailsList = entityManager.createQuery(
                            "SELECT b FROM BankDetails b WHERE b.customer.id = :customerId", BankDetails.class)
                    .setParameter("customerId", customerId)
                    .getResultList();

            List<BankAccountDTO> bankAccountDTOList = new ArrayList<>();
            for (BankDetails bankDetails : bankDetailsList) {
                BankAccountDTO bankAccountDTO = new BankAccountDTO();
                bankAccountDTO.setId(bankDetails.getId());
                bankAccountDTO.setCustomerName(bankDetails.getCustomerName());
                bankAccountDTO.setAccountNumber(bankDetails.getAccountNumber());
                bankAccountDTO.setIfscCode(bankDetails.getIfscCode());
                bankAccountDTO.setBankName(bankDetails.getBankName());
                bankAccountDTO.setBranchName(bankDetails.getBranchName());
                bankAccountDTO.setAccountType(bankDetails.getAccountType());

                bankAccountDTOList.add(bankAccountDTO);
            }

            return bankAccountDTOList;
        } catch (Exception e) {
            e.printStackTrace();
            exceptionHandlingImplement.handleException(e);
            return Collections.emptyList();
        }
    }
}
