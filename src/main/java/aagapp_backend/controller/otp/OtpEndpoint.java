package aagapp_backend.controller.otp;

import aagapp_backend.components.CommonData;
import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.*;
import aagapp_backend.services.admin.AdminService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderServiceImpl;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.client.HttpClientErrorException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/otp")
public class OtpEndpoint {

    private AdminService adminService;
    private ExceptionHandlingImplement exceptionHandling;
    private JwtUtil jwtUtil;
    private TwilioService twilioService;
    private CustomCustomerService customCustomerService;
    private RateLimiterService rateLimiterService;
    private EntityManager em;
    private VenderServiceImpl serviceProviderService;
    private RoleService roleService;
    private ResponseService responseService;
    @Value("${twilio.accountSid}")
    private String accountSid;

    @Autowired
    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    @Lazy
    public void setTwilioService(TwilioService twilioService) {
        this.twilioService = twilioService;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setRateLimiterService(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PersistenceContext
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setCustomerService(CustomCustomerService customerService) {
        this.customCustomerService = customerService;  // Same as customCustomerService, decide which to keep
    }

    @Autowired
    public void setServiceProviderService(VenderServiceImpl serviceProviderService) {
        this.serviceProviderService = serviceProviderService;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Value("${twilio.authToken}")
    private String authToken;


    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody CustomCustomer customerDetails, HttpSession session) throws UnsupportedEncodingException {
        try {
            if (customerDetails.getMobileNumber() == null || customerDetails.getMobileNumber().isEmpty()) {
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_NULL_OR_EMPTY, HttpStatus.NOT_ACCEPTABLE);
            }

            String mobileNumber = customerDetails.getMobileNumber().startsWith("0")
                    ? customerDetails.getMobileNumber().substring(1)
                    : customerDetails.getMobileNumber();

            String countryCode = customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()
                    ? Constant.COUNTRY_CODE
                    : customerDetails.getCountryCode();

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhoneWithOtp(customerDetails.getMobileNumber(), countryCode);
            if (existingCustomer != null) {
                return responseService.generateErrorResponse(ApiConstants.CUSTOMER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }

            Bucket bucket = rateLimiterService.resolveBucket(customerDetails.getMobileNumber(), "/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);

                }

                ResponseEntity<Map<String, Object>> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                Map<String, Object> responseBody = otpResponse.getBody();

                if (responseBody.get("otp")!=null) {
                    return responseService.generateSuccessResponse((String) responseBody.get("message"), responseBody.get("otp"), HttpStatus.OK);
                } else {
                    return responseService.generateErrorResponse((String) responseBody.get("message"), HttpStatus.BAD_REQUEST);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.RATE_LIMIT_EXCEEDED, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, Object> loginDetails, jakarta.servlet.http.HttpSession session, HttpServletRequest request) {
        try {

            if (loginDetails == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            String otpEntered = (String) loginDetails.get("otpEntered");
            Integer role = (Integer) loginDetails.get("role");
            String countryCode = (String) loginDetails.get("countryCode");
            String mobileNumber = (String) loginDetails.get("mobileNumber");


            if (role == null) {
                return responseService.generateErrorResponse(ApiConstants.ROLE_EMPTY, HttpStatus.BAD_REQUEST);
            }

            if (roleService.findRoleName(role).equals(Constant.roleUser)) {
                 if (mobileNumber == null) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                if (otpEntered == null || otpEntered.trim().isEmpty()) {
                    return responseService.generateErrorResponse("otp is null ", HttpStatus.BAD_REQUEST);
                }

                CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

                if (existingCustomer == null) {
                    return responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                }

                String storedOtp = existingCustomer.getOtp();
                String ipAddress = request.getRemoteAddr();

                String userAgent = request.getHeader("User-Agent");
                String tokenKey = "authToken_" + mobileNumber;
                CustomCustomer customer = customCustomerService.readCustomerById(existingCustomer.getId());

                if (otpEntered.equals(storedOtp)) {
                    existingCustomer.setOtp(null);
                    em.persist(existingCustomer);


                    String existingToken = existingCustomer.getToken();

                    if (existingToken!= null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                        ApiResponse response = new ApiResponse(existingToken,customer, HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
                        return ResponseEntity.ok(response);

                    } else {
                        String newToken = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
                        session.setAttribute(tokenKey, newToken);
                        existingCustomer.setToken(newToken);
                        em.persist(existingCustomer);

                        ApiResponse response = new ApiResponse(newToken,customer, HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
                        return ResponseEntity.ok(response);

                    }
                } else {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);
                }
            } else if (roleService.findRoleName(role).equals(Constant.rolevendor)) {
                return serviceProviderService.verifyOtp(loginDetails, session, request);
            }

            else if(roleService.findRoleName(role).equals(Constant.ADMIN) ||roleService.findRoleName(role).equals(Constant.SUPER_ADMIN) ||roleService.findRoleName(role).equals(Constant.SUPPORT)) {
                return adminService.verifyOtpForAdmin(loginDetails,session,request);
            }
            else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_ROLE, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Otp verification error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/vendor-signup")
    public ResponseEntity<?> sendOtpToMobile(@RequestBody Map<String, Object> signupDetails) {
        try {
            String mobileNumber = (String) signupDetails.get("mobileNumber");
            String countryCode = (String) signupDetails.get("countryCode");


            mobileNumber = mobileNumber.startsWith("0") ? mobileNumber.substring(1) : mobileNumber;
            if (customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode) != null) {
                return responseService.generateErrorResponse(ApiConstants.NUMBER_REGISTERED_AS_CUSTOMER, HttpStatus.BAD_REQUEST);
            }

            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            if (!CommonData.isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
            }

            Twilio.init(accountSid, authToken);
            String otp = twilioService.generateOTP();

            VendorEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber, countryCode);

            if (existingServiceProvider == null) {
                VendorEntity vendorEntity = new VendorEntity();
                vendorEntity.setCountry_code(countryCode);
                vendorEntity.setMobileNumber(mobileNumber);
                vendorEntity.setOtp(otp);
                vendorEntity.setRole(4);
                em.persist(vendorEntity);
            } else if (existingServiceProvider.getOtp() != null) {
                existingServiceProvider.setOtp(otp);
                em.merge(existingServiceProvider);
            } else {
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_REGISTERED, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            String maskedNumber = twilioService.genereateMaskednumber(mobileNumber);
            details.put("otp", otp);
            return responseService.generateSuccessResponse(ApiConstants.OTP_SENT_SUCCESSFULLY + " on " +maskedNumber, otp, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse(ApiConstants.UNAUTHORIZED_ACCESS , HttpStatus.UNAUTHORIZED);
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return responseService.generateErrorResponse(ApiConstants.ERROR_SENDING_OTP + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.ERROR_SENDING_OTP + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PostMapping("/admin-signup")
    public ResponseEntity<?> sendOtpToMobileAdmin(@RequestBody Map<String, Object> signupDetails) {
        try {
            String mobileNumber = (String) signupDetails.get("mobileNumber");
            String countryCode = (String) signupDetails.get("countryCode");


            mobileNumber = mobileNumber.startsWith("0") ? mobileNumber.substring(1) : mobileNumber;
            if (customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode) != null) {
                return responseService.generateErrorResponse(ApiConstants.NUMBER_REGISTERED_AS_CUSTOMER, HttpStatus.BAD_REQUEST);
            }

            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            if (!CommonData.isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
            }

            Twilio.init(accountSid, authToken);
            String otp = twilioService.generateOTP();

            CustomAdmin existingcustomAdmin = adminService.findAdminByPhone(mobileNumber,countryCode);

            if (existingcustomAdmin == null) {
                CustomAdmin customAdmin = new CustomAdmin();
                customAdmin.setCountry_code(countryCode);
                customAdmin.setMobileNumber(mobileNumber);
                customAdmin.setOtp(otp);
                customAdmin.setRole(1);
                em.persist(customAdmin);
            } else if (existingcustomAdmin.getOtp() != null) {
                existingcustomAdmin.setOtp(otp);
                em.merge(existingcustomAdmin);
            } else {
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_REGISTERED, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            String maskedNumber = twilioService.genereateMaskednumber(mobileNumber);
            details.put("otp", otp);
            return responseService.generateSuccessResponse(ApiConstants.OTP_SENT_SUCCESSFULLY + " on " +maskedNumber, otp, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse(ApiConstants.UNAUTHORIZED_ACCESS , HttpStatus.UNAUTHORIZED);
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return responseService.generateErrorResponse(ApiConstants.ERROR_SENDING_OTP + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.ERROR_SENDING_OTP + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }





    public static class ApiResponse {
        private Data data;
        private int status_code;
        private String status;
        private String message;
        private String token;


        public ApiResponse(String token, CustomCustomer customerDetails, int statusCodeValue, String statusCode, String message) {
            this.data = new Data(customerDetails);
            this.status_code = statusCodeValue;
            this.status = statusCode;
            this.message = message;
            this.token = token;
        }

        public Data getData() {
            return data;
        }

        public int getStatus_code() {
            return status_code;
        }

        public String getStatus() {
            return status;
        }

        public String getToken() {
            return token;
        }

        public String getMessage() {
            return message;
        }

        public  class Data {
            private CustomCustomer userDetails;

            public Data(CustomCustomer customerDetails) {
                this.userDetails = customerDetails;
            }

            public CustomCustomer getUserDetails() {
                return userDetails;
            }
        }
    }

}
