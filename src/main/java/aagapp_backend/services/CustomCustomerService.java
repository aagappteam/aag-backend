package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;

import aagapp_backend.enums.ProfileStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomCustomerService {

    private EntityManager entityManager;

    public CustomCustomerService(EntityManager em) {
        this.entityManager = em;
    }


    public Boolean validateInput(CustomCustomer customer) {
        if (customer.getMobileNumber().isEmpty() || customer.getMobileNumber() == null || customer.getPassword() == null || customer.getPassword().isEmpty())
            return false;
        if (!isValidMobileNumber(customer.getMobileNumber()))
            return false;

        return true;
    }

    public boolean isValidMobileNumber(String mobileNumber) {

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }
        String mobileNumberPattern = "^\\d{9,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    @Transactional

    public CustomCustomer findCustomCustomerByPhone(String mobileNumber, String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return entityManager.createQuery(Constant.PHONE_QUERY, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }



    @Transactional
    public CustomCustomer findCustomCustomerById(Long customerId) {
        // Check if customerId is valid
        if (customerId == null) {
            return null;
        }

        return entityManager.createQuery("SELECT c FROM CustomCustomer c WHERE c.id = :customerId", CustomCustomer.class)
                .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }


    @Transactional
    public CustomCustomer findCustomCustomerByPhoneWithOtp(String mobileNumber, String countryCode) {

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }
        if (entityManager == null) {
            throw new IllegalStateException("EntityManager is not initialized");
        }
        return entityManager.createQuery(Constant.PHONE_QUERY_OTP, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .setParameter("profileStatus", ProfileStatus.ACTIVE)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Transactional

    public CustomCustomer readCustomerById(Long customerId) {
        return entityManager.createQuery("SELECT c FROM CustomCustomer c WHERE c.id = :customerId", CustomCustomer.class)
                .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst()
                .orElse(null);

    }


    public CustomCustomer getCustomerById(Long customerId) {
        return entityManager.createQuery("SELECT c FROM CustomCustomer c WHERE c.id = :customerId", CustomCustomer.class)
                .setParameter("customerId", customerId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public CustomCustomer findCustomCustomerByReferralCode(String referralCode) {
        return entityManager.createQuery(Constant.REFERRAL_CODE_QUERY, CustomCustomer.class)
                .setParameter("referralCode", referralCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public CustomCustomer save(CustomCustomer customer) {
        entityManager.persist(customer);
        return customer;
    }


    @Transactional
    public ResponseEntity<?> updateCustomer(Long customerId, Map<String, Object> updates) {
        try {
            // Retrieve the existing customer entity by ID
            CustomCustomer existingCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (existingCustomer == null) {
                return ResponseEntity.status(404).body("Customer with ID " + customerId + " not found");
            }

            // Prevent the mobile number from being updated
            if (updates.containsKey("mobileNumber")) {
                updates.remove("mobileNumber"); // Remove mobileNumber from updates to prevent modification
            }

            // Validate the provided updates
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();

                // Use reflection to set the fields dynamically
                Field field = CustomCustomer.class.getDeclaredField(fieldName);
                field.setAccessible(true);

                // Skip fields that are not valid for update or cannot be empty
                if (newValue == null || newValue.toString().isEmpty()) {
                    continue;
                }

                // Handle specific validations for each field
                if ("email".equals(fieldName)) {
                    // You can add your own email validation logic here if required
                    if (newValue != null && !isValidEmail((String) newValue)) {
                        return ResponseEntity.badRequest().body("Invalid email format");
                    }
                }

                if ("mobileNumber".equals(fieldName)) {
                    // Validate mobile number if it's being updated (this should not happen)
                    if (newValue != null && !isValidMobileNumber((String) newValue)) {
                        return ResponseEntity.badRequest().body("Invalid mobile number format");
                    }
                }

                // Set the value if no validation errors occurred
                if (newValue != null && !newValue.toString().isEmpty()) {
                    field.set(existingCustomer, newValue);
                }
            }

            // Persist the updated customer entity
            entityManager.merge(existingCustomer);
            return ResponseEntity.ok().body("Customer updated successfully");
        } catch (NoSuchFieldException e) {
            return ResponseEntity.badRequest().body("Invalid field name: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating customer: " + e.getMessage());
        }
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches(Constant.EMAIL_REGEXP);
    }

}