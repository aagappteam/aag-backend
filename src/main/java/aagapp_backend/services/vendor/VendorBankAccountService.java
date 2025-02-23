package aagapp_backend.services.vendor;
import aagapp_backend.dto.BankAccountDTO;
import aagapp_backend.entity.VendorBankDetails;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorBankDetails;
import aagapp_backend.entity.VendorEntity;
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
public class VendorBankAccountService  {


    /**
     * The Entity manager.
     */
    @Autowired
    EntityManager entityManager;


    @Autowired
    ExceptionHandlingImplement exceptionHandling;

    @Transactional
    public String addBankAccount(VendorEntity vendorEntity, BankAccountDTO bankAccountDTO) {
        try {
            if (isAccountNumberExists(bankAccountDTO.getAccountNumber(), null)) {
                return "Account number already exists.";
            }

            if (!bankAccountDTO.getAccountNumber().equals(bankAccountDTO.getReEnterAccountNumber())) {
                return "Account numbers do not match.";
            }

            VendorBankDetails bankDetails = new VendorBankDetails();
            bankDetails.setId(bankAccountDTO.getId());
            bankDetails.setCustomerName(bankAccountDTO.getCustomerName());
            bankDetails.setAccountNumber(bankAccountDTO.getAccountNumber());
            bankDetails.setVendorEntity(vendorEntity);
            bankDetails.setIfscCode(bankAccountDTO.getIfscCode());
            bankDetails.setBankName(bankAccountDTO.getBankName());
            bankDetails.setBranchName(bankAccountDTO.getBranchName());
            bankDetails.setAccountType(bankAccountDTO.getAccountType());

            entityManager.persist(bankDetails);

            Long generatedId = bankDetails.getId();

            return "Bank account added successfully! ID: " + generatedId;
        } catch (Exception e) {
            exceptionHandling.handleException(e);

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
            VendorBankDetails bankDetails = entityManager.find(VendorBankDetails.class, accountId);
            if (bankDetails != null) {
                entityManager.remove(bankDetails); // Delete the bank account
                return "Bank account deleted successfully!";
            } else {
                return "Bank account not found!";
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);

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
    public Optional<VendorBankDetails> getBankAccountById(Long accountId) {
        try {
            if (accountId == null) {
                return Optional.empty();
            }

            VendorBankDetails bankDetails = entityManager.find(VendorBankDetails.class, accountId);
            if (bankDetails == null) {
                return Optional.empty();
            }
            if (bankDetails != null) {

                return Optional.of(bankDetails);
            }
            return Optional.empty();
        } catch (Exception e) {
            exceptionHandling.handleException(e);

            return Optional.empty();
        }
    }

    @Transactional
    public String updateBankAccount(Long accountId, Map<String, Object> params) {
        try {
            // Fetch the existing bank account from the database
            VendorBankDetails existingAccount = entityManager.find(VendorBankDetails.class, accountId);
            if (existingAccount == null) {
                return "Account update failed. Account not found.";
            }

            // Check if the required fields (account number and re-entered account number) are present in params
            String accountNumber = (String) params.get("accountNumber");
            String reEnterAccountNumber = (String) params.get("reEnterAccountNumber");

            // Validate if account numbers match
            if (accountNumber != null && reEnterAccountNumber != null && !accountNumber.equals(reEnterAccountNumber)) {
                return "Account numbers do not match.";
            }

            // If account number is present, check if it already exists (except for the current account)
            if (accountNumber != null && isAccountNumberExists(accountNumber, accountId)) {
                return "Account number already exists.";
            }

            // Reflection to set the fields dynamically
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                if (value != null) {
                    // Create a method name for the setter (e.g., setAccountNumber for accountNumber field)
                    String setterMethodName = "set" + StringUtils.capitalize(fieldName);
                    try {
                        // Find the corresponding setter method for the field and invoke it
                        Method setterMethod = VendorBankDetails.class.getMethod(setterMethodName, value.getClass());
                        setterMethod.invoke(existingAccount, value);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        return "Account update failed. Error setting field: " + fieldName;
                    }
                }
            }

            // Merge the updated entity into the database
            entityManager.merge(existingAccount);

            return "Bank account updated successfully!";
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return "Account update failed: " + e.getMessage();
        }
    }



    public BankAccountDTO convertToDTO(VendorBankDetails bankDetails) {
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
            List<VendorBankDetails> duplicateAccounts = entityManager.createQuery(
                            "SELECT b FROM VendorBankDetails b WHERE b.accountNumber = :accountNumber", VendorBankDetails.class)
                    .setParameter("accountNumber", accountNumber)
                    .getResultList();

            if (accountId != null) {
                duplicateAccounts.removeIf(bankDetails -> bankDetails.getId().equals(accountId));
            }

            return !duplicateAccounts.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Gets bank accounts by customer id.
     *
     * @param customerId the customer id
     * @return the bank accounts by customer id
     */
    public List<BankAccountDTO> getBankAccountsByCustomerId(Long customerId) {
        try {
            List<VendorBankDetails> bankDetailsList = entityManager.createQuery(
                            "SELECT b FROM VendorBankDetails b WHERE b.vendorEntity.service_provider_id = :customerId", VendorBankDetails.class)
                    .setParameter("customerId", customerId)
                    .getResultList();


            List<BankAccountDTO> bankAccountDTOList = new ArrayList<>();
            for (VendorBankDetails bankDetails : bankDetailsList) {
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
            exceptionHandling.handleException(e);

            return Collections.emptyList();
        }
    }
}
