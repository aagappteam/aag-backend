package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.PermissionUpdateRequest;
import aagapp_backend.entity.CustomCustomer;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.ProfileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomCustomerService {

    @Autowired
    private RestTemplate restTemplate;

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
            CustomCustomer existingCustomer = entityManager.find(CustomCustomer.class, customerId);
            if (existingCustomer == null) {
                return ResponseEntity.status(404).body("Customer with ID " + customerId + " not found");
            }

            String oldName = existingCustomer.getName();

            if (updates.containsKey("mobileNumber")) {
                updates.remove("mobileNumber");
            }

            String updatedName = null;

            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();

                if (newValue == null || newValue.toString().isEmpty()) {
                    continue;
                }

                // Email validation
                if ("email".equals(fieldName)) {
                    if (!isValidEmail((String) newValue)) {
                        return ResponseEntity.badRequest().body("Invalid email format");
                    }
                }

                // Mobile number should not be updated, but still handled
                if ("mobileNumber".equals(fieldName)) {
                    if (!isValidMobileNumber((String) newValue)) {
                        return ResponseEntity.badRequest().body("Invalid mobile number format");
                    }
                }

                // Capture name for gender-based logic
                if ("name".equals(fieldName)) {
                    updatedName = newValue.toString();
                }

                // Set value using reflection
                try {
                    Field field = CustomCustomer.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(existingCustomer, newValue);
                } catch (NoSuchFieldException ignored) {
                    // Unknown fields are ignored
                }
            }

            // Gender-based profilePic assignment only if gender has changed
            if (updatedName != null && !updatedName.isBlank()) {
                String oldGender = getGenderByName(oldName);
                String newGender = getGenderByName(updatedName);

                if (!oldGender.equalsIgnoreCase(newGender)) {
                    if ("male".equalsIgnoreCase(newGender)) {
                        existingCustomer.setProfilePic("https://aag-data.s3.ap-south-1.amazonaws.com/avtars/maleAvtars/image+10.png");
                    } else if ("female".equalsIgnoreCase(newGender)) {
                        existingCustomer.setProfilePic("https://aag-data.s3.ap-south-1.amazonaws.com/avtars/femaleAvatars/image+51.png");
                    } else {
                        existingCustomer.setProfilePic("https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg");
                    }
                }
            }

            entityManager.merge(existingCustomer);
            return ResponseEntity.ok().body("Customer updated successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating customer: " + e.getMessage());
        }
    }


    public boolean isValidEmail(String email) {
        return email != null && email.matches(Constant.EMAIL_REGEXP);
    }

    public String getGenderByName(String name) {
        String url = "https://api.genderize.io?name=" + name;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map body = response.getBody();
            return (String) body.get("gender");
        }
        return "unknown";
    }

    @Transactional
    public ResponseEntity<?> updateProfilePic(Long customerId, String profilePicUrl) {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
            if (customer == null) {
                return ResponseEntity.status(404).body("Customer not found");
            }

            customer.setProfilePic(profilePicUrl);
            entityManager.merge(customer);
            return ResponseService.generateSuccessResponse("Profile picture updated successfully", customer, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error to update profile picture: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Boolean> updatePermissions(Long customerId, PermissionUpdateRequest request) {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
            if (customer == null) {
                throw new RuntimeException("Customer not found");
            }

            if (request.getSmsPermission() != null) {
                customer.setSmsPermission(request.getSmsPermission());
            }
            if (request.getWhatsappPermission() != null) {
                customer.setWhatsappPermission(request.getWhatsappPermission());
            }

            entityManager.merge(customer);

            Map<String, Boolean> response = new HashMap<>();
            response.put("smsPermission", customer.getSmsPermission());
            response.put("whatsappPermission", customer.getWhatsappPermission());

            return response;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update permissions", e);
        }
    }

}


