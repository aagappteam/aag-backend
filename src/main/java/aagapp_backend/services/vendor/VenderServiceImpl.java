package aagapp_backend.services.vendor;

import aagapp_backend.components.CommonData;
import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.*;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import io.github.bucket4j.Bucket;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class VenderServiceImpl implements VenderService {

    private EntityManager entityManager;
    private ExceptionHandlingImplement exceptionHandling;
    private CustomCustomerService customCustomerService;
    private JwtUtil jwtUtil;
    private ResponseService responseService;
    private TwilioService twilioService;

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
    public void setEntityManager( EntityManager entityManager) {
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

    /**
     * @param userId
     * @param updates
     * @return
     * @throws Exception
     */
    @Override
    public ResponseEntity<?> updateServiceProvider(Long userId, Map<String, Object> updates) throws Exception {
        return null;
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

        Map<String, Object> data = new HashMap<>();
        data.put("venderDetails", vendorEntity);
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


            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("OTP cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (otpEntered.equals(storedOtp)) {
                existingServiceProvider.setOtp(null);
                entityManager.merge(existingServiceProvider);
                String existingToken = existingServiceProvider.getToken();

                System.out.println("existingToken is " + existingToken);
//                Map<String,Object> serviceProviderResponse= sharedUtilityService.serviceProviderDetailsMap(existingServiceProvider);
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
                        entityManager.merge(existingServiceProvider);
                        responseBody.put("message", "User has been signed up");
                    }
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
            if(mobileNumber!=null) {
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
            }  else {
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


            if(existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {

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

}
