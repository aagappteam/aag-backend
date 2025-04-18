package aagapp_backend.controller.otp;

import aagapp_backend.components.CommonData;
import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.CustomerDeviceDTO;
import aagapp_backend.dto.VendorDeviceDTO;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.devices.UserDevice;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.ProfileStatus;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.*;
import aagapp_backend.services.admin.AdminService;
import aagapp_backend.services.devicemange.DeviceMange;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.otp.Otp;
import aagapp_backend.services.referal.ReferralService;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
    private ReferralService referralService;
    private DeviceMange deviceMange;
    private WalletRepository walletRepository;

    private Otp otpservice;
    @Autowired
    public void setOtpservice(Otp otpservice) {
        this.otpservice = otpservice;
    }

    @Autowired
    private  PlayerRepository playerRepository;

    @Autowired
    public void setWalletRepository(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Autowired
    public void setDeviceMange(DeviceMange deviceMange) {
        this.deviceMange = deviceMange;
    }


    @Autowired
    public void setReferralService(ReferralService referralService) {
        this.referralService = referralService;
    }

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
        this.customCustomerService = customerService;
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

            if (customerDetails.getReferralCode() != null && !customerDetails.getReferralCode().isEmpty()) {
                CustomCustomer referralCustomer = customCustomerService.findCustomCustomerByReferralCode(customerDetails.getReferralCode());
                if (referralCustomer == null) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_REFERRAL_CODE, HttpStatus.BAD_REQUEST);
                }
                customerDetails.setReferralCode(String.valueOf(referralCustomer.getId()));
            }


            Bucket bucket = rateLimiterService.resolveBucket(customerDetails.getMobileNumber(), "/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);

                }

                ResponseEntity<Map<String, Object>> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                Map<String, Object> responseBody = otpResponse.getBody();

                if (responseBody.get("otp") != null) {
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
            String referredCode = (String) loginDetails.get("referralCode");
            String fcm_token = (String) loginDetails.get("fcm_token");

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
                Long userId = existingCustomer.getId();

                String referralCode = referralService.generateReferralCode(existingCustomer);
//                @todo:- need to work on new devices & existing device checks for multiple login devices
               /* boolean isNewDevice = deviceMange.isLoggedInFromNewDevice(userId, ipAddress, userAgent);
                if (isNewDevice) {
                    deviceMange.recordLoginDevice(customer, ipAddress, userAgent, jwtUtil.generateToken(userId, role, ipAddress, userAgent));
                }*/
                if(fcm_token!=null){
                    existingCustomer.setFcmToken(fcm_token);

                }

                if (existingCustomer.getProfileStatus() == ProfileStatus.PENDING) {
                    existingCustomer.setReferralCode(referralCode);
                    em.persist(existingCustomer);
                }
                if (referredCode != null && existingCustomer.getProfileStatus() == ProfileStatus.PENDING) {
                    referralService.updateReferrerEarnings(referredCode);

                }

                if (otpEntered.equals(storedOtp)) {
                    if (existingCustomer.getProfileStatus() == ProfileStatus.PENDING) {
                        existingCustomer.setProfileStatus(ProfileStatus.ACTIVE);
                        // Only create player if not already present
                        if (existingCustomer.getPlayer() == null) {
                            Player player = new Player();
                            player.setCustomer(existingCustomer); // Yeh automatically ID bhi set karega
                            player.setCreatedAt(LocalDateTime.now());
                            player.setUpdatedAt(LocalDateTime.now());
                            existingCustomer.setPlayer(player); // bidirectional link
                            playerRepository.save(player);
                        }
/*
                        sendOnboardingEmail(existingCustomer.getEmail(), existingCustomer.getName(), existingCustomer.getLastName());
*/
                    }
                    Wallet referrerWallet = walletRepository.findByCustomCustomer(existingCustomer);
                    if (referrerWallet == null) {
                        // Create a new wallet for the referrer if it doesn't exist
                        referrerWallet = new Wallet();
                        referrerWallet.setCustomCustomer(existingCustomer);
                        referrerWallet.setUnplayedBalance(0F);  // Set default value
                        referrerWallet.setWinningAmount(BigDecimal.ZERO);  // Set default value
                        existingCustomer.setBonusBalance(BigDecimal.ZERO);  // Set default value
                        referrerWallet.setIsTest(false);  // Set default value (assuming it's not a test account)

                        // Save the wallet
                        walletRepository.save(referrerWallet);  // Make sure you have a walletRepository
                    }


                    existingCustomer.setOtp(null);
                    em.persist(existingCustomer);

                    String existingToken = existingCustomer.getToken();

                    if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                        return responseService.generateSuccessResponse("User has been logged in", existingCustomer, HttpStatus.OK);

                    } else {
                        String newToken = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
                        session.setAttribute(tokenKey, newToken);
                        existingCustomer.setToken(newToken);
                        em.persist(existingCustomer);
                        return responseService.generateSuccessResponse("User has been logged in", existingCustomer, HttpStatus.OK);

                    }
                } else {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);
                }
            } else if (roleService.findRoleName(role).equals(Constant.rolevendor)) {
                return serviceProviderService.verifyOtp(loginDetails, session, request);
            } else if (roleService.findRoleName(role).equals(Constant.ADMIN) || roleService.findRoleName(role).equals(Constant.SUPER_ADMIN) || roleService.findRoleName(role).equals(Constant.SUPPORT)) {
                return adminService.verifyOtpForAdmin(loginDetails, session, request);
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_ROLE, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Otp verification error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, Object> loginDetails, HttpSession session, HttpServletRequest request) {
        String mobileNumber = (String) loginDetails.get("mobileNumber");
        Integer role = (Integer) loginDetails.get("role");
        String countryCode = (String) loginDetails.get("countryCode");
        String fcm_token = (String) loginDetails.get("fcm_token");

        if (countryCode == null) {
            countryCode = Constant.COUNTRY_CODE;
        }


        String ipAddress = request.getRemoteAddr();

        String userAgent = request.getHeader("User-Agent");

        if (roleService.findRoleName(role).equals(Constant.roleUser)) {
            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

            if (existingCustomer == null) {
                return responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);
            }


            String newToken = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
            if(fcm_token!= null){
                existingCustomer.setFcmToken(fcm_token);
            }
            existingCustomer.setToken(newToken);
            em.persist(existingCustomer);
            return responseService.generateSuccessResponse("New token has been generated", existingCustomer.getToken(), HttpStatus.OK);
        } else if (roleService.findRoleName(role).equals(Constant.rolevendor)) {

            VendorEntity vendorEntity = serviceProviderService.findServiceProviderByPhone(mobileNumber, countryCode);
            if (vendorEntity == null) {
                return responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);
            }

            String newToken = jwtUtil.generateToken(vendorEntity.getService_provider_id(), role, ipAddress, userAgent);

            if(fcm_token!= null){
                vendorEntity.setFcmToken(fcm_token);
            }

            vendorEntity.setToken(newToken);
            em.persist(vendorEntity);
            return responseService.generateSuccessResponse("New token has been generated", vendorEntity.getToken(), HttpStatus.OK);

    }  else if (roleService.findRoleName(role).equals(Constant.ADMIN) || roleService.findRoleName(role).equals(Constant.SUPER_ADMIN) || roleService.findRoleName(role).equals(Constant.SUPPORT)) {
            CustomAdmin customAdmin = adminService.findAdminByPhone(mobileNumber, countryCode);
            if (customAdmin == null) {
                return responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);
            }

            String newToken = jwtUtil.generateToken(customAdmin.getAdmin_id(), role, ipAddress, userAgent);
            customAdmin.setToken(newToken);
            em.persist(customAdmin);
            return responseService.generateSuccessResponse("New token has been generated", customAdmin.getToken(), HttpStatus.OK);


    } else {
        return responseService.generateErrorResponse(ApiConstants.INVALID_ROLE, HttpStatus.BAD_REQUEST);
    }

    }


    @Transactional
    @PostMapping("/vendor-signup")
    public ResponseEntity<?> sendOtpToMobile(@RequestBody Map<String, Object> signupDetails) {
        try {
            String mobileNumber = (String) signupDetails.get("mobileNumber");
            String countryCode = (String) signupDetails.get("countryCode");


            mobileNumber = mobileNumber.startsWith("0") ? mobileNumber.substring(1) : mobileNumber;
            if (!CommonData.isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
            }
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }


            String otp = twilioService.generateOTP();


            VendorEntity existingServiceProvider = serviceProviderService.findActiveServiceProviderByPhone(mobileNumber, countryCode);
            if(existingServiceProvider!=null){
                return responseService.generateErrorResponse(ApiConstants.MOBILE_NUMBER_REGISTERED, HttpStatus.BAD_REQUEST);
            }
            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/otp/vendor-signup");
            if (bucket.tryConsume(1)) {

                otpservice.sendOtp(countryCode,mobileNumber,otp);
                VendorEntity existingServiceProviderwithoutsigneup = serviceProviderService.findActiveServiceProviderByPhone(mobileNumber, countryCode);
                if(existingServiceProviderwithoutsigneup==null){
                    VendorEntity vendorEntity = new VendorEntity();
                    vendorEntity.setCountry_code(countryCode);
                    vendorEntity.setMobileNumber(mobileNumber);
                    vendorEntity.setOtp(otp);
                    vendorEntity.setRole(4);
                    em.persist(vendorEntity);
                }else{
                    existingServiceProviderwithoutsigneup.setOtp(otp);
                    em.merge(existingServiceProviderwithoutsigneup);
                }

                Map<String, Object> details = new HashMap<>();
                String maskedNumber = twilioService.genereateMaskednumber(mobileNumber);
                details.put("otp", otp);
                return responseService.generateSuccessResponse(ApiConstants.OTP_SENT_SUCCESSFULLY + " on " + maskedNumber, otp, HttpStatus.OK);
            } else {
                return responseService.generateErrorResponse(ApiConstants.RATE_LIMIT_EXCEEDED, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
            }



        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse(ApiConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
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

            CustomAdmin existingcustomAdmin = adminService.findAdminByPhone(mobileNumber, countryCode);

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
            return responseService.generateSuccessResponse(ApiConstants.OTP_SENT_SUCCESSFULLY + " on " + maskedNumber, otp, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse(ApiConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
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

        public class Data {
            private CustomCustomer userDetails;

            public Data(CustomCustomer customerDetails) {
                this.userDetails = customerDetails;
            }

            public CustomCustomer getUserDetails() {
                return userDetails;
            }
        }
    }

    @GetMapping("/customerdevice/details")
    public ResponseEntity<?> getDeviceDetails(@RequestParam Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return responseService.generateErrorResponse("Invalid user ID provided.", HttpStatus.BAD_REQUEST);
            }
            CustomCustomer user = customCustomerService.findCustomCustomerById(userId);
            if (user == null) {
                return responseService.generateErrorResponse("User not found.", HttpStatus.NOT_FOUND);
            }
            List<CustomerDeviceDTO> deviceDetails = deviceMange.getCustomerDeviceLoginDetails(userId);
            if (deviceDetails == null || deviceDetails.isEmpty()) {
                return responseService.generateErrorResponse("No device details found for the user.", HttpStatus.NOT_FOUND);
            }
            return responseService.generateSuccessResponse("Customer login device details fetched.", deviceDetails, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching device details: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/vendordevice/details")
    public ResponseEntity<?> getVendorDeviceDetails(@RequestParam Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return responseService.generateErrorResponse("Invalid user ID provided.", HttpStatus.BAD_REQUEST);
            }
            VendorEntity user = serviceProviderService.getServiceProviderById(userId);
            if (user == null) {
                return responseService.generateErrorResponse("User not found.", HttpStatus.NOT_FOUND);
            }
            List<VendorDeviceDTO> deviceDetails = deviceMange.getVendorDeviceLoginDetails(userId);
            if (deviceDetails == null || deviceDetails.isEmpty()) {
                return responseService.generateErrorResponse("No device details found for the user.", HttpStatus.NOT_FOUND);
            }
            return responseService.generateSuccessResponse("vendor login device details fetched.", deviceDetails, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching device details: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}