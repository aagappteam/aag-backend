package aagapp_backend.services.admin;

import aagapp_backend.components.CommonData;
import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.services.*;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService
{
    private EntityManager entityManager;
    private ExceptionHandlingImplement exceptionHandling;
    private VenderService serviceProviderService;
    private CustomCustomerService customCustomerService;
    private String accountSid;
    private String authToken;
    private TwilioServiceForAdmin twilioService;
    private PasswordEncoder passwordEncoder;
    private ResponseService responseService;
    private JwtUtil jwtUtil;
    private RateLimiterService rateLimiterService;
    private RoleService roleService;


    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }
    @Autowired
    public void setServiceProviderService(VenderService serviceProviderService) {
        this.serviceProviderService = serviceProviderService;
    }
    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Value("${twilio.accountSid}")
    public void setAccountSid(String accountSid) {
        this.accountSid = accountSid;
    }

    @Value("${twilio.authToken}")
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    @Autowired
    @Lazy
    public void setTwilioService(TwilioServiceForAdmin twilioService) {
        this.twilioService = twilioService;
    }
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }
    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Autowired
    public void setRateLimiterService(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }
    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }
    public CustomAdmin findAdminByPhone(String mobile_number, String countryCode) {
        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }

        return entityManager.createQuery(Constant.PHONE_QUERY_ADMIN, CustomAdmin.class)
                .setParameter("mobileNumber", mobile_number)
                .setParameter("country_code", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ResponseEntity<?> sendOtpForAdmin(String mobileNumber, String countryCode, HttpSession session) throws UnsupportedEncodingException {
        try {
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            if (countryCode == null)
                countryCode = Constant.COUNTRY_CODE;
            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/admin/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse("Invalid mobile number", HttpStatus.BAD_REQUEST);

                }
                ResponseEntity<?> otpResponse = twilioService.sendOtpToMobileForAdmin(mobileNumber, countryCode);
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
    public ResponseEntity<?> verifyOtpForAdmin(Map<String, Object> adminDetails, HttpSession session, HttpServletRequest request) {
        try {
            String username = (String) adminDetails.get("username");
            String otpEntered = (String) adminDetails.get("otpEntered");
            String mobileNumber = (String) adminDetails.get("mobileNumber");
            String countryCode = (String) adminDetails.get("countryCode");
            Integer role = (Integer) adminDetails.get("role");
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            CustomAdmin customAdmin=null;
             if (mobileNumber!=null) {

                if (!CommonData.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse("Invalid mobile number ", HttpStatus.BAD_REQUEST);

                }
                if (mobileNumber.startsWith("0"))
                    mobileNumber = mobileNumber.substring(1);
                customAdmin = findAdminByPhone(mobileNumber, countryCode);
                if(roleService.findRoleName(role).equals(Constant.ADMIN))
                {
                    if(customAdmin.getRole()!=2)
                    {
                        return responseService.generateErrorResponse("Custom Admin with username "+ mobileNumber+" does not have role "+ roleService.findRoleName(role), HttpStatus.BAD_REQUEST);
                    }
                }
/*                else if(roleService.findRoleName(role).equals(Constant.SUPER_ADMIN))
                {
                    if(customAdmin.getRole()!=1)
                    {
                        return responseService.generateErrorResponse("Custom Admin with username "+ mobileNumber+" does not have role "+ roleService.findRoleName(role), HttpStatus.BAD_REQUEST);
                    }
                }*/
                else if(roleService.findRoleName(role).equals(Constant.SUPPORT))
                {
                    if(customAdmin.getRole()!=1)
                    {
                        return responseService.generateErrorResponse(" SUPPORT with username "+ mobileNumber+" does not have role "+ roleService.findRoleName(role), HttpStatus.BAD_REQUEST);
                    }
                }

            }
            else
            {
                return responseService.generateErrorResponse("Invalid Role Provided ", HttpStatus.UNAUTHORIZED);
            }

            if (customAdmin == null) {
                return responseService.generateErrorResponse("Invalid Data Provided ", HttpStatus.UNAUTHORIZED);

            }

            String storedOtp = customAdmin.getOtp();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenAdmin_" + mobileNumber;


            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("OTP cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (otpEntered.equals(storedOtp)) {
                customAdmin.setOtp(null);
                entityManager.merge(customAdmin);


                String existingToken = customAdmin.getToken();
//                Map<String,Object> serviceProviderResponse= sharedUtilityService.adminDetailsMap(customAdmin);

                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {


                    Map<String, Object> responseBody = createAuthResponseForAdmin(existingToken, customAdmin).getBody();


                    return ResponseEntity.ok(responseBody);
                } else {
                    String newToken = jwtUtil.generateToken(customAdmin.getAdmin_id(), role, ipAddress, userAgent);

                    customAdmin.setToken(newToken);
                    entityManager.persist(customAdmin);
                    Map<String, Object> responseBody = createAuthResponseForAdmin(newToken, customAdmin).getBody();
/*                    if(customAdmin.getSignedUp()==0) {
                        customAdmin.setSignedUp(1);
                        entityManager.merge(customAdmin);
                        responseBody.put("message", "User has been signed up");
                    }*/
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

    private ResponseEntity<Map<String, Object>> createAuthResponseForAdmin(String token, CustomAdmin adminEntity) {
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("customAdminDetails",adminEntity);
        responseBody.put("status_code", HttpStatus.OK.value());
        responseBody.put("data", data);
        responseBody.put("token", token);
        responseBody.put("message", "Admin has been logged in");
        responseBody.put("status", "OK");

        return ResponseEntity.ok(responseBody);
    }

    public ResponseEntity<?> loginWithPasswordForAdmin(@RequestBody Map<String, Object> customAdminDetails, HttpServletRequest request, HttpSession session) {
        try {
            String mobileNumber = (String) customAdminDetails.get("mobileNumber");
            if(mobileNumber!=null) {
                if (mobileNumber.startsWith("0"))
                    mobileNumber = mobileNumber.substring(1);
            }

            String username = (String) customAdminDetails.get("username");
            String password = (String) customAdminDetails.get("password");
            String countryCode = (String) customAdminDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);
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

    public ResponseEntity<?> authenticateByPhone(String mobileNumber, String countryCode, String password, HttpServletRequest request, HttpSession session) {
        CustomAdmin existingAdmin = findAdminByPhone(mobileNumber, countryCode);

        System.out.println("found Admin" + existingAdmin);
        return validateAdmin(existingAdmin, password, request, session);
    }

    public ResponseEntity<?> authenticateByUsername(String username, String password, HttpServletRequest request, HttpSession session) {
        CustomAdmin existingCustomAdmin = findAdminByUsername(username);
        return validateAdmin(existingCustomAdmin, password, request, session);
    }

    public CustomAdmin findAdminByUsername(String username) {

        return entityManager.createQuery(Constant.USERNAME_QUERY_CUSTOM_ADMIN, CustomAdmin.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ResponseEntity<?> validateAdmin(CustomAdmin customAdmin, String password, HttpServletRequest request, HttpSession session) {
        if (customAdmin == null) {
            return responseService.generateErrorResponse("No Records Found", HttpStatus.NOT_FOUND);
        }
        if (passwordEncoder.matches(password, customAdmin.getPassword())) {
            System.out.println("inside the match");
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenAdmin_" + customAdmin.getMobileNumber();


            String existingToken = customAdmin.getToken();


            if(existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {

                Map<String, Object> responseBody = createAuthResponseForAdmin(existingToken, customAdmin).getBody();

                return ResponseEntity.ok(responseBody);
            } else {
                String newToken = jwtUtil.generateToken(customAdmin.getAdmin_id(), customAdmin.getRole(), ipAddress, userAgent);
                session.setAttribute(tokenKey, newToken);

                customAdmin.setToken(newToken);
                entityManager.persist(customAdmin);

                Map<String, Object> responseBody = createAuthResponseForAdmin(newToken, customAdmin).getBody();


                return ResponseEntity.ok(responseBody);
            }
        } else {
            return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ResponseEntity<?> updateDetails(Long userId, Map<String, Object> adminDetails) {
        try {
            CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, userId);

            if (customAdmin == null) {
                return ResponseService.generateErrorResponse("Admin with provided Id not found", HttpStatus.NOT_FOUND);
            }

            // Handle the update of fields
/*            if (adminDetails.containsKey("role")) {
                customAdmin.setRole((Integer) adminDetails.get("role"));
            }

            if (adminDetails.containsKey("user_name")) {
                customAdmin.setUser_name((String) adminDetails.get("user_name"));
            }*/

            if (adminDetails.containsKey("password")) {
                customAdmin.setPassword(passwordEncoder.encode((String) adminDetails.get("password")));
            }

            customAdmin.setUpdated_at(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            entityManager.merge(customAdmin);

            return ResponseService.generateSuccessResponse("Admin details updated successfully", customAdmin, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error updating admin details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}