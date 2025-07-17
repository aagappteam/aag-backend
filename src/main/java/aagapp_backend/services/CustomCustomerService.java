package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.PermissionUpdateRequest;
import aagapp_backend.entity.CustomCustomer;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import aagapp_backend.enums.ProfileStatus;
import aagapp_backend.services.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomCustomerService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    private String createCustomerUsername(CustomCustomer customer) {
        String namePart = customer.getName() != null
                ? customer.getName().replaceAll("\\s+", "").toLowerCase()
                : "aaguser";

        String mobile = customer.getMobileNumber();  // make sure this exists
        String lastFour = (mobile != null && mobile.length() >= 4)
                ? mobile.substring(mobile.length() - 4)
                : "0000";

        return namePart + lastFour;
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


            if (updates.containsKey("password")) {
                Object passwordValue = updates.get("password");
                if (passwordValue != null && !passwordValue.toString().trim().isEmpty()) {
                    String rawPassword = passwordValue.toString().trim();
                    String encryptedPassword = passwordEncoder.encode(rawPassword);
                    existingCustomer.setPassword(encryptedPassword);
                }
                updates.remove("password");
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

                if ("mobileNumber".equals(fieldName)) {
                    if (!isValidMobileNumber((String) newValue)) {
                        return ResponseEntity.badRequest().body("Invalid mobile number format");
                    }
                }

                if ("name".equals(fieldName)) {
                    updatedName = newValue.toString();
                }
                if ("user_name".equals(fieldName)) {
                    existingCustomer.setUser_name(newValue.toString().trim());
                    continue;
                }

                try {
                    Field field = CustomCustomer.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(existingCustomer, newValue);
                } catch (NoSuchFieldException e) {
                    return ResponseEntity.status(500)
                            .body("Field '" + fieldName + "' not found in CustomCustomer class.");
                }
            }

            if ((existingCustomer.getUser_name() == null || existingCustomer.getUser_name().trim().isEmpty())
                    && (updates.get("user_name") == null)) {

                String generatedUsername = createCustomerUsername(existingCustomer);
                existingCustomer.setUser_name(generatedUsername);
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

        }catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            String message = (rootCause != null) ? rootCause.getMessage() : e.getMessage();

            if (message != null) {
                if (message.contains("user_name")) {
                    return ResponseEntity.badRequest().body("Username already exists.");
                } else if (message.contains("email")) {
                    return ResponseEntity.badRequest().body("Email is already in use.");
                } else if (message.contains("mobile_number")) {
                    return ResponseEntity.badRequest().body("Mobile number is already registered.");
                } else if (message.contains("referral_code")) {
                    return ResponseEntity.badRequest().body("Referral code already exists.");
                } else {
                    return ResponseEntity.badRequest().body("Duplicate value violates a unique constraint.");
                }
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A unique constraint was violated. Please check your input.");
        }


        catch (Exception e) {
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


    @Transactional
    public CustomCustomer provideBonus(CustomCustomer user, BigDecimal bonusAmount) {
        BigDecimal currentBonus = user.getBonusBalance() != null ? user.getBonusBalance() : BigDecimal.ZERO;
        user.setBonusBalance(currentBonus.add(bonusAmount));
        return entityManager.merge(user);
    }


    public ResponseEntity<?> getProfilePicById(Long id) {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, id);
            if (customer == null) {
                throw new BusinessException("Customer not found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Profile picture", customer.getProfilePic(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error to get profile picture: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


