package aagapp_backend.services.vendor;

import aagapp_backend.components.CommonData;
import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.ReferralDTO;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorReferral;
import aagapp_backend.repository.vendor.VendorReferralRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.*;
import aagapp_backend.services.devicemange.DeviceMange;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.referal.ReferralService;
import io.github.bucket4j.Bucket;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class VenderServiceImpl implements VenderService {

    private EntityManager entityManager;
    private ExceptionHandlingImplement exceptionHandling;
    private CustomCustomerService customCustomerService;
    private JwtUtil jwtUtil;
    private ResponseService responseService;
    private TwilioService twilioService;
    private DeviceMange deviceMange;
    private ReferralService referralService;
    private VendorReferralRepository vendorReferralRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    public void setVendorReferralRepository(VendorReferralRepository vendorReferralRepository) {
        this.vendorReferralRepository = vendorReferralRepository;
    }

    @Autowired
    @Lazy
    public void setDeviceMange(DeviceMange deviceMange) {
        this.deviceMange = deviceMange;
    }


    public VendorEntity findServiceProviderByReferralCode(String referralCode) {
        return vendorRepository.findServiceProviderByReferralCode(referralCode);
    }

    @Autowired
    @Lazy
    public void setReferralService(ReferralService referralService) {
        this.referralService = referralService;
    }

    @Autowired
    public void setTwilioService(@Lazy
                                 TwilioService twilioService) {
        this.twilioService = twilioService;
    }


    @Autowired
    @Lazy
    RateLimiterService rateLimiterService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    public VendorEntity findServiceProviderByUserName(String username) {

        return entityManager.createQuery(Constant.USERNAME_QUERY_SERVICE_PROVIDER, VendorEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public VendorEntity saveServiceProvider(VendorEntity VendorEntity) {
        try {
            entityManager.persist(VendorEntity);
            return VendorEntity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving service provider entity", e);
        }
    }

    @Transactional
    public ResponseEntity<?> updateServiceProvider(Long userId, Map<String, Object> updates) {
        try {
            // Trimming string values before proceeding
            updates = CommonData.trimStringValues(updates);
            List<String> errorMessages = new ArrayList<>();

            VendorEntity existingServiceProvider = entityManager.find(VendorEntity.class, userId);
            if (existingServiceProvider == null) {
                errorMessages.add("VendorEntity with ID " + userId + " not found");
            }

            // Validating username and email uniqueness
            if (updates.containsKey("user_name")) {
                String userName = (String) updates.get("user_name");
                VendorEntity existingSPByUsername = findServiceProviderByUserName(userName);
                if (existingSPByUsername != null && !existingSPByUsername.getService_provider_id().equals(userId)) {
                    return responseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);
                }
            }

            if (updates.containsKey("primary_email")) {
                String primaryEmail = (String) updates.get("primary_email");
                VendorEntity existingSPByEmail = findSPbyEmail(primaryEmail);
                if (existingSPByEmail != null && !existingSPByEmail.getService_provider_id().equals(userId)) {
                    return responseService.generateErrorResponse("Email not available", HttpStatus.BAD_REQUEST);
                }
            }

            // Preventing the mobile number from being updated (if provided in the request)
            if (updates.containsKey("mobileNumber")) {
                updates.remove("mobileNumber"); // Remove mobileNumber from the update map to prevent updating it
            }

            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();

                // Skip fields that are not valid for update or cannot be empty
                Field field = VendorEntity.class.getDeclaredField(fieldName);
                field.setAccessible(true);

                // Check nullable and size constraints
                Column columnAnnotation = field.getAnnotation(Column.class);
                boolean isColumnNotNull = (columnAnnotation != null && !columnAnnotation.nullable());
                boolean isNullable = field.isAnnotationPresent(Nullable.class);

                if (newValue.toString().isEmpty() && !isNullable) {
                    errorMessages.add(fieldName + " cannot be null");
                    continue;
                }
                if (newValue.toString().isEmpty() && isNullable) {
                    continue; // Skip nullable fields if empty
                }

                if (field.isAnnotationPresent(Size.class)) {
                    Size sizeAnnotation = field.getAnnotation(Size.class);
                    int min = sizeAnnotation.min();
                    int max = sizeAnnotation.max();
                    if (newValue.toString().length() < min || newValue.toString().length() > max) {
                        errorMessages.add(fieldName + " size should be between " + min + " and " + max);
                        continue;
                    }
                }

                if (field.isAnnotationPresent(Email.class) && !this.isValidEmail((String) newValue)) {
                    errorMessages.add(fieldName + " is invalid");
                    continue;
                }

                if (field.isAnnotationPresent(Pattern.class) && !newValue.toString().matches(field.getAnnotation(Pattern.class).regexp())) {
                    errorMessages.add(fieldName + " is invalid");
                    continue;
                }

                if ("date_of_birth".equals(fieldName)) {
                    String dobString = (String) newValue;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    try {
                        LocalDate dob = LocalDate.parse(dobString, formatter);
                        if (dob.isAfter(LocalDate.now())) {
                            errorMessages.add("Date of birth cannot be in the future");
                        }
                    } catch (DateTimeParseException e) {
                        errorMessages.add("Invalid date format for " + fieldName + ". Expected format is DD-MM-YYYY.");
                    }
                }

                // Set the field value if no validation errors occurred
                if (errorMessages.isEmpty() && newValue != null) {
                    if (field.getType().isAssignableFrom(newValue.getClass())) {
                        field.set(existingServiceProvider, newValue);
                    }
                }
            }

            if (!errorMessages.isEmpty()) {
                return ResponseService.generateErrorResponse(errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }
            entityManager.merge(existingServiceProvider);

            return responseService.generateSuccessResponse("Service Provider Updated Successfully", existingServiceProvider, HttpStatus.OK);
        } catch (NoSuchFieldException e) {
            return ResponseService.generateErrorResponse("No such field present: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating Service Provider: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

/*    @Transactional
    public ResponseEntity<?> updateServiceProvider(Long userId, Map<String, Object> updates) {
        try {
          updates = CommonData.trimStringValues(updates);
            List<String> errorMessages = new ArrayList<>();


            VendorEntity existingServiceProvider = entityManager.find(VendorEntity.class, userId);
            if (existingServiceProvider == null) {
                errorMessages.add("VendorEntity with ID " + userId + " not found");
            }


            String mobileNumber = (String) updates.get("mobileNumber");

            // Validate and check for unique constraints
            VendorEntity existingSPByUsername = null;
            VendorEntity existingSPByEmail = null;

            if (updates.containsKey("user_name")) {
                updates.remove("user_name");
            }
            if (updates.containsKey("primary_mobile_number")) {
                String userName = (String) updates.get("user_name");
                existingSPByUsername = findServiceProviderByUserName(userName);
            }

            if (updates.containsKey("primary_email")) {
                String primaryEmail = (String) updates.get("primary_email");
                existingSPByEmail = findSPbyEmail(primaryEmail);
            }

            if ((existingSPByUsername != null) || existingSPByEmail != null) {
                if (existingSPByUsername != null && !existingSPByUsername.getService_provider_id().equals(userId)) {
                    return responseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);
                }
                if (existingSPByEmail != null && !existingSPByEmail.getService_provider_id().equals(userId)) {
                    return responseService.generateErrorResponse("Email not available", HttpStatus.BAD_REQUEST);
                }
            }

           if (updates.containsKey("date_of_birth")) {
                String dob = (String) updates.get("date_of_birth");
                if (CommonData.isFutureDate(dob))
                    errorMessages.add("DOB cannot be in future");
            }
*//*            if (updates.containsKey("pan_number") && ((String) updates.get("pan_number")).isEmpty())
                errorMessages.add("pan number cannot be empty");*//*
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();

                Field field = VendorEntity.class.getDeclaredField(fieldName);
                System.out.println(field);
                Column columnAnnotation = field.getAnnotation(Column.class);
                boolean isColumnNotNull = (columnAnnotation != null && !columnAnnotation.nullable());
                // Check if the field has the @Nullable annotation
                boolean isNullable = field.isAnnotationPresent(Nullable.class);
                field.setAccessible(true);
                if (newValue.toString().isEmpty() && !isNullable)
                    errorMessages.add(fieldName + " cannot be null");
                if (newValue.toString().isEmpty() && isNullable)
                    continue;
                if (newValue != null) {
                    if (field.isAnnotationPresent(Size.class)) {
                        Size sizeAnnotation = field.getAnnotation(Size.class);
                        int min = sizeAnnotation.min();
                        int max = sizeAnnotation.max();
                        if (newValue.toString().length() > max || newValue.toString().length() < min) {
                            if (max == min)
                                errorMessages.add(fieldName + " size should be of size " + max);
                            else
                                errorMessages.add(fieldName + " size should be in between " + min + " " + max);
                            continue;
                        }
                    }
                    if (field.isAnnotationPresent(Email.class)) {
                        Email emailAnnotation = field.getAnnotation(Email.class);
                        String message = emailAnnotation.message();
                        if (!this.isValidEmail((String) newValue)) {
                            errorMessages.add(message.replace("{field}", fieldName));
                            continue;
                        }
                    }

                    if (field.isAnnotationPresent(Pattern.class)) {
                        Pattern patternAnnotation = field.getAnnotation(Pattern.class);
                        String regex = patternAnnotation.regexp();
                        String message = patternAnnotation.message(); // Get custom message
                        if (!newValue.toString().matches(regex)) {
                            errorMessages.add(fieldName + "is invalid"); // Use a placeholder
                            continue;
                        }
                    }

                    if (fieldName.equals("date_of_birth")) {
                        String dobString = (String) newValue;
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        try {
                            LocalDate dob = LocalDate.parse(dobString, formatter);
                            if (dob.isAfter(LocalDate.now())) {
                                errorMessages.add("Date of birth cannot be in the future");
                            }
                        } catch (DateTimeParseException e) {
                            errorMessages.add("Invalid date format for " + fieldName + ". Expected format is DD-MM-YYYY.");
                        }
                    }
                }
                field.setAccessible(true);
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(existingServiceProvider, newValue);
                }
            }
            if (!errorMessages.isEmpty())
                return ResponseService.generateErrorResponse(errorMessages.toString(), HttpStatus.BAD_REQUEST);

            entityManager.merge(existingServiceProvider);
            entityManager.merge(existingServiceProvider);


            return responseService.generateSuccessResponse("Service Provider Updated Successfully", existingServiceProvider, HttpStatus.OK);
        } catch (NoSuchFieldException e) {
            return ResponseService.generateErrorResponse("No such field present :" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating Service Provider : ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    public boolean isValidEmail(String email) {
        return email != null && email.matches(Constant.EMAIL_REGEXP);
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public VendorEntity getServiceProviderById(Long userId) {
        return entityManager.find(VendorEntity.class, userId);
    }

    /**
     * @param mobileNumber
     * @param countryCode
     * @return
     */
    @Override
    public VendorEntity findServiceProviderByPhone(String mobileNumber, String countryCode) {
        return entityManager.createQuery(Constant.PHONE_QUERY_SERVICE_PROVIDER, VendorEntity.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("country_code", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }


    public List<VendorEntity> getAllSp(int page, int limit) {
        int startPosition = page * limit;
        TypedQuery<VendorEntity> query = entityManager.createQuery(Constant.GET_ALL_SERVICE_PROVIDERS, VendorEntity.class);
        query.setFirstResult(startPosition);
        query.setMaxResults(limit);
        List<VendorEntity> results = query.getResultList();
        return results;
    }

    public List<VendorEntity> findServiceProviderListByUsername(String username) {
        username = username + "%";
        return entityManager.createQuery(Constant.SP_USERNAME_QUERY, VendorEntity.class)
                .setParameter("username", username)
                .getResultList();
    }

    private ResponseEntity<Map<String, Object>> createAuthResponse(String token, VendorEntity vendorEntity) {
        Map<String, Object> responseBody = new HashMap<>();

        List<ReferralDTO> sentReferrals = getSentReferrals(vendorEntity.getService_provider_id());
        ReferralDTO receivedReferral = getReceivedReferral(vendorEntity.getService_provider_id());

        Map<String, Object> data = new HashMap<>();
        data.put("venderDetails", vendorEntity);
        data.put("sentReferrals", sentReferrals);
        data.put("receivedReferral", receivedReferral); // Assuming this is a single referral
        responseBody.put("status_code", HttpStatus.OK.value());
        responseBody.put("data", data);
        responseBody.put("token", token);
        responseBody.put("message", "User has been logged in");
        responseBody.put("status", "OK");

        return ResponseEntity.ok(responseBody);
    }

    public VendorEntity findSPbyEmail(String email) {

        return entityManager.createQuery(Constant.SP_EMAIL_QUERY, VendorEntity.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ResponseEntity<?> verifyOtp(Map<String, Object> loginDetails, HttpSession session, HttpServletRequest request) {
        try {
            String otpEntered = (String) loginDetails.get("otpEntered");
            String mobileNumber = (String) loginDetails.get("mobileNumber");
            String countryCode = (String) loginDetails.get("countryCode");
            String referralCode = (String) loginDetails.get("referralCode");
            Integer role = (Integer) loginDetails.get("role");
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("mobile number can not be null ", HttpStatus.BAD_REQUEST);

            }

            if (!CommonData.isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse("Invalid mobile number ", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber.startsWith("0"))
                mobileNumber = mobileNumber.substring(1);
            VendorEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);


            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Invalid Data Provided ", HttpStatus.UNAUTHORIZED);

            }

            String storedOtp = existingServiceProvider.getOtp();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            VendorEntity referrer = findServiceProviderByReferralCode(referralCode);



            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("OTP cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (otpEntered.equals(storedOtp)) {
                existingServiceProvider.setOtp(null);

                // Generate and assign a referral code if it's a new Vendor
                if (existingServiceProvider.getReferralCode() == null || existingServiceProvider.getReferralCode().isEmpty()) {
                    String newReferralCode = referralService.generateVendorReferralCode(existingServiceProvider);
                    existingServiceProvider.setReferralCode(newReferralCode);
                }

                entityManager.merge(existingServiceProvider);
                String existingToken = existingServiceProvider.getToken();

                // After obtaining the referrer using the referral code
                if (referralCode != null && !referralCode.isEmpty() && existingServiceProvider.getSignedUp()==0) {
                    // Find the referrer by referral code
//                    VendorEntity referrer = findServiceProviderByReferralCode(referralCode);


                    if (referrer != null) {
                        // Create a VendorReferral entry
                        VendorReferral vendorReferral = new VendorReferral();
                        vendorReferral.setReferrerId(referrer);
                        vendorReferral.setReferredId(existingServiceProvider);

                        // Persist the VendorReferral entry
                        entityManager.persist(vendorReferral);

                        // Update the referrer's referral count
                        referrer.setReferralCount(referrer.getReferralCount() + 1);
                        entityManager.merge(referrer);
                    }
                }



                /*Long userId = existingServiceProvider.getService_provider_id();
                boolean isNewDevice = deviceMange.isVendorLoginFromNewDevice(userId, ipAddress, userAgent);
                if (isNewDevice) {
                    deviceMange.recordVendorLoginDevice(existingServiceProvider, ipAddress, userAgent, jwtUtil.generateToken(existingServiceProvider.getService_provider_id(), role, ipAddress, userAgent));
                }*/

                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {


                    Map<String, Object> responseBody = createAuthResponse(existingToken, existingServiceProvider).getBody();


                    return ResponseEntity.ok(responseBody);
                } else {
                    String newToken = jwtUtil.generateToken(existingServiceProvider.getService_provider_id(), role, ipAddress, userAgent);

                    existingServiceProvider.setToken(newToken);
                    entityManager.persist(existingServiceProvider);
                    Map<String, Object> responseBody = createAuthResponse(newToken, existingServiceProvider).getBody();
                    if(existingServiceProvider.getSignedUp()==0) {
                        existingServiceProvider.setSignedUp(1);
                        VendorReferral vendorReferral= vendorReferralRepository.findByReferredId(existingServiceProvider);
                        entityManager.merge(existingServiceProvider);
                    }
                    responseBody.put("message", "User has been signed up");
                    return ResponseEntity.ok(responseBody);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Otp verification error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, Object> serviceProviderDetails, HttpServletRequest request, HttpSession session) {
        try {
            String mobileNumber = (String) serviceProviderDetails.get("mobileNumber");
            if (mobileNumber != null) {
                if (mobileNumber.startsWith("0"))
                    mobileNumber = mobileNumber.substring(1);
            }

            String username = (String) serviceProviderDetails.get("username");
            String password = (String) serviceProviderDetails.get("password");
            String countryCode = (String) serviceProviderDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);
            // Check for empty password
            if (password == null || password.isEmpty()) {
                return responseService.generateErrorResponse("Password cannot be empty", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                return authenticateByPhone(mobileNumber, countryCode, password, request, session);
            } else {
                return responseService.generateErrorResponse("Empty Phone Number or username", HttpStatus.BAD_REQUEST);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> sendOtp(String mobileNumber, String countryCode, HttpSession session) {
        try {
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            if (countryCode == null)
                countryCode = Constant.COUNTRY_CODE;
            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/vender/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!CommonData.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse("Invalid mobile number", HttpStatus.BAD_REQUEST);

                }
                ResponseEntity<?> otpResponse = twilioService.sendOtpToMobileVendor(mobileNumber, countryCode);
                return otpResponse;
            } else {
                return responseService.generateErrorResponse("You can send OTP only once in 1 minute", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ResponseEntity<?> validateServiceProvider(VendorEntity serviceProvider, String password, HttpServletRequest request, HttpSession session) {
        if (serviceProvider == null) {
            return responseService.generateErrorResponse("No Records Found", HttpStatus.NOT_FOUND);
        }
        if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + serviceProvider.getMobileNumber();


            String existingToken = serviceProvider.getToken();

//            Map<String,Object> serviceProviderResponse= sharedUtilityService.serviceProviderDetailsMap(serviceProvider);


            if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {

                Map<String, Object> responseBody = createAuthResponse(existingToken, serviceProvider).getBody();

                return ResponseEntity.ok(responseBody);
            } else {
                String newToken = jwtUtil.generateToken(serviceProvider.getService_provider_id(), serviceProvider.getRole(), ipAddress, userAgent);
                session.setAttribute(tokenKey, newToken);

                serviceProvider.setToken(newToken);
                entityManager.persist(serviceProvider);

                Map<String, Object> responseBody = createAuthResponse(newToken, serviceProvider).getBody();


                return ResponseEntity.ok(responseBody);


            }
        } else {
            return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> authenticateByPhone(String mobileNumber, String countryCode, String password, HttpServletRequest request, HttpSession session) {
        VendorEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
        return validateServiceProvider(existingServiceProvider, password, request, session);
    }


    @Override
    @Transactional
    public VendorEntity saveOrUpdate(VendorEntity VendorEntity) {
        try {
            return entityManager.merge(VendorEntity);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error saving or updating vendor: " + e.getMessage());
        }

    }

    public ReferralDTO getReceivedReferral(Long vendorId) {
        VendorEntity vendorEntity = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        VendorReferral vendorReferral = vendorReferralRepository.findByReferredId(vendorEntity);

        if (vendorReferral != null) {
            Long referrerId = vendorReferral.getReferrerId().getService_provider_id(); // Get the referrer ID
            Long referredId = vendorReferral.getReferredId().getService_provider_id(); // Get the referred ID
            return new ReferralDTO(vendorReferral.getId(), referrerId, referredId);
        }

        return null; // or throw an exception if you prefer
    }

    public List<ReferralDTO> getSentReferrals(Long vendorId) {
        VendorEntity vendorEntity = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        List<VendorReferral> sentReferrals = vendorReferralRepository.findByReferrerId(vendorEntity);

        // Convert to DTO
        List<ReferralDTO> referralDTOs = new ArrayList<>();
        for (VendorReferral referral : sentReferrals) {
            referralDTOs.add(new ReferralDTO(referral.getId(),
                    referral.getReferrerId().getService_provider_id(),
                    referral.getReferredId().getService_provider_id()));
        }

        return referralDTOs;
    }


}
