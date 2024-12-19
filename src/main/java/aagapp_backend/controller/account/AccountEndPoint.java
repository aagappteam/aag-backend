package aagapp_backend.controller.account;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.controller.otp.OtpEndpoint;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.game.Game;
import aagapp_backend.services.*;
import aagapp_backend.services.GameService.GameService;
import aagapp_backend.services.admin.AdminService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping(value = "/account",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public class AccountEndPoint {

    private ExceptionHandlingImplement exceptionHandling;
    private TwilioService twilioService;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private EntityManager em;
    private CustomCustomerService customCustomerService;
    private RoleService roleService;
    private OtpEndpoint otpEndpoint;
    private VenderService vendorService;
    private ResponseService responseService;
    private AdminService adminService;
    private GameService gameService;

    @Autowired
    @Lazy
    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setTwilioService(TwilioService twilioService) {
        this.twilioService = twilioService;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setOtpEndpoint(OtpEndpoint otpEndpoint) {
        this.otpEndpoint = otpEndpoint;
    }

    @Autowired
    public void setVendorService(VenderService vendorService) {
        this.vendorService = vendorService;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/login-with-otp")
    public ResponseEntity<?> verifyAndLogin(@RequestBody Map<String, Object> loginDetails, HttpSession session) {
        try {

            String roleName = roleService.findRoleName((Integer) loginDetails.get("role"));
            if (roleName.equals("EMPTY"))
                return ResponseService.generateErrorResponse("Role not found", HttpStatus.NOT_FOUND);

            String mobileNumber = (String) loginDetails.get("mobileNumber");

            if (mobileNumber != null) {

                int i = 0;
                for (; i < mobileNumber.length(); i++) {
                    if (mobileNumber.charAt(i) != '0')
                        break;
                }
                mobileNumber = mobileNumber.substring(i);
                loginDetails.put("mobileNumber", mobileNumber);
                Integer role = (Integer) loginDetails.get("role");

                if (customCustomerService.isValidMobileNumber(mobileNumber)) {

                    return loginWithPhoneOtp(loginDetails, session);
                } else {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    @Transactional

    @PostMapping("/login-with-password")
    @ResponseBody
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, Object> loginDetails, HttpSession session, HttpServletRequest request) {
        try {
            String roleName = roleService.findRoleName((Integer) loginDetails.get("role"));
            if (roleName.equals("EMPTY"))
                return ResponseService.generateErrorResponse("Role not found", HttpStatus.NOT_FOUND);

            String mobileNumber = (String) loginDetails.get("mobileNumber");
            if (mobileNumber != null) {
                if (mobileNumber.startsWith("0"))
                    mobileNumber = mobileNumber.substring(1);
                if (customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return loginWithCustomerPassword(loginDetails, session, request);
                } else {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.INTERNAL_SERVER_ERROR);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @RequestMapping(value = "phone-otp", method = RequestMethod.POST)
    private ResponseEntity<?> loginWithPhoneOtp(Map<String, Object> loginDetails, HttpSession session) throws UnsupportedEncodingException, UnsupportedEncodingException {
        try {

            if (loginDetails == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);

            }

            String mobileNumber = (String) loginDetails.get("mobileNumber");

            String countryCode = (String) loginDetails.get("countryCode");
            Integer role = (Integer) loginDetails.get("role");
            if (mobileNumber == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);

            } else if (role == null) {
                return responseService.generateErrorResponse(ApiConstants.ROLE_EMPTY, HttpStatus.BAD_REQUEST);

            }
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }
            String updated_mobile = mobileNumber;
            if (mobileNumber.startsWith("0")) {
                updated_mobile = mobileNumber.substring(1);
            }
            if (roleService.findRoleName(role).equals(Constant.roleUser)) {
                CustomCustomer customerRecords = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);
                if (customerRecords == null) {
                    return responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);

                }
                if (customCustomerService == null) {
                    return responseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

                }
                CustomCustomer customer = customCustomerService.readCustomerById(customerRecords.getId());
                if (customer != null) {

                    ResponseEntity<Map<String, Object>> otpResponse = twilioService.sendOtpToMobile(updated_mobile, countryCode);

                    Map<String, Object> responseBody = otpResponse.getBody();


                    if (responseBody.get("otp") != null) {
                        return responseService.generateSuccessResponse((String) responseBody.get("message"), (String) responseBody.get("otp"), HttpStatus.OK);
                    } else {
                        return responseService.generateErrorResponse((String) responseBody.get("message"), HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                }
            } else if (roleService.findRoleName(role).equals(Constant.rolevendor)) {
                if (vendorService.findServiceProviderByPhone(mobileNumber, countryCode) != null) {
                    if (vendorService.findServiceProviderByPhone(mobileNumber, countryCode).getOtp() != null) {
                        responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);

                    }
                    return vendorService.sendOtp(mobileNumber, countryCode, session);

                } else {
                    return responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                }

            } else if (roleService.findRoleName(role).equals(Constant.SUPPORT) || roleService.findRoleName(role).equals(Constant.ADMIN)) {
                CustomAdmin customAdmin = adminService.findAdminByPhone(mobileNumber, countryCode);
                if (customAdmin != null) {
                    if (customAdmin.getRole() == 1 || customAdmin.getRole() == 2) {
                        if (adminService.findAdminByPhone(mobileNumber, countryCode).getOtp() != null) {
                            responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                        }
                        return adminService.sendOtpForAdmin(mobileNumber, countryCode, session);
                    } else {
                        return responseService.generateErrorResponse("Custom Admin with mobileNumber " + mobileNumber + " does not have " + roleService.findRoleName(role) + " role", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return responseService.generateErrorResponse(ApiConstants.NO_RECORDS_FOUND, HttpStatus.NOT_FOUND);
                }
            } else {
                responseService.generateErrorResponse(ApiConstants.ROLE_EMPTY, HttpStatus.BAD_REQUEST);
            }
            return responseService.generateErrorResponse("Role not specified", HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);

        }

    }

    @RequestMapping(value = "customer-login-with-password", method = RequestMethod.POST)
    public ResponseEntity<?> loginWithCustomerPassword(@RequestBody Map<String, Object> loginDetails, HttpSession session,
                                                       HttpServletRequest request) {
        try {

            if (loginDetails == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);

            }
            String mobileNumber = (String) loginDetails.get("mobileNumber");
            String password = (String) loginDetails.get("password");
            String countryCode = (String) loginDetails.get("countryCode");
            Integer role = (Integer) loginDetails.get("role");

            if (mobileNumber == null || password == null || role == null) {
                return responseService.generateErrorResponse("number/password number cannot be empty", HttpStatus.UNAUTHORIZED);

            }
            if (countryCode == null) {
                countryCode = Constant.COUNTRY_CODE;
            }
            if (customCustomerService == null) {
                return responseService.generateErrorResponse(ApiConstants.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (roleService.findRoleName(role).equals(Constant.roleUser)) {
                CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);
                if (existingCustomer != null) {
                    CustomCustomer customer = customCustomerService.readCustomerById(existingCustomer.getId());
                    if (passwordEncoder.matches(password, existingCustomer.getPassword())) {
                        String tokenKey = "authToken_" + mobileNumber;
                        String existingToken = existingCustomer.getToken();
                        String ipAddress = request.getRemoteAddr();
                        String userAgent = request.getHeader("User-Agent");

                        if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                            return ResponseEntity.ok(new OtpEndpoint.ApiResponse(existingToken, customer, HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in"));

                        } else {

                            String token = jwtUtil.generateToken(existingCustomer.getId(), role, ipAddress, userAgent);
                            existingCustomer.setToken(token);
                            em.persist(existingCustomer);
                            session.setAttribute(tokenKey, token);
                            return ResponseEntity.ok(new OtpEndpoint.ApiResponse(token, customer, HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in"));
                        }

                    } else {
                        return responseService.generateErrorResponse("Incorrect Password", HttpStatus.UNAUTHORIZED);

                    }
                } else {
                    return responseService.generateErrorResponse(ApiConstants.NO_EXISTING_RECORDS_FOUND, HttpStatus.NOT_FOUND);

                }
            } else if (roleService.findRoleName(role).equals(Constant.rolevendor)) {
                return vendorService.loginWithPassword(loginDetails, request, session);
            } else return responseService.generateErrorResponse(ApiConstants.INVALID_ROLE, HttpStatus.BAD_REQUEST);


        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin-login-with-otp")
    public ResponseEntity<?> verifyAndLoginAdmin(@RequestBody Map<String, Object> loginDetails, HttpSession session) {
        try {

            String roleName = roleService.findRoleName((Integer) loginDetails.get("role"));
            if (roleName.equals("EMPTY"))
                return ResponseService.generateErrorResponse("Role not found", HttpStatus.NOT_FOUND);

            String mobile_number = (String) loginDetails.get("mobileNumber");

            if (mobile_number != null) {

                int i = 0;
                for (; i < mobile_number.length(); i++) {
                    if (mobile_number.charAt(i) != '0')
                        break;
                }

                mobile_number = mobile_number.substring(i);
                loginDetails.put("mobileNumber", mobile_number);
                if (customCustomerService.isValidMobileNumber(mobile_number)) {
                    return loginWithPhoneOtp(loginDetails, session);

                } else {
                    return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);

        }
        return responseService.generateErrorResponse(ApiConstants.INVALID_MOBILE_NUMBER, HttpStatus.BAD_REQUEST);
    }

    @jakarta.transaction.Transactional
    @RequestMapping(value = "admin-login-with-password", method = RequestMethod.POST)
    public ResponseEntity<?> adminLoginWithPassword(@RequestBody Map<String, Object> loginDetails, HttpSession session, HttpServletRequest request) {
        try {

            if (loginDetails == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

            String username = (String) loginDetails.get("username");
            String mobilenumber = (String) loginDetails.get("mobileNumber");
            String countryCode = (String) loginDetails.get("countryCode");

            if (countryCode == null) {
                countryCode = Constant.COUNTRY_CODE;
            }

            String password = (String) loginDetails.get("password");
            Integer role = (Integer) loginDetails.get("role");

            CustomAdmin customAdmin = adminService.findAdminByPhone(mobilenumber, countryCode);
            if (customAdmin == null) {
                return responseService.generateErrorResponse("Custom Admin with username " + username + " not found", HttpStatus.NOT_FOUND);
            }
            if (roleService.findRoleName(role).equals(Constant.ADMIN)) {

                if (customAdmin.getRole() == 2) {
                    return adminService.loginWithPasswordForAdmin(loginDetails, request, session);
                } else {
                    return responseService.generateErrorResponse("Custom Admin with username " + username + " does not have " + roleService.findRoleName(role) + " role", HttpStatus.BAD_REQUEST);
                }
            } else if (roleService.findRoleName(role).equals(Constant.SUPPORT)) {

                if (customAdmin.getRole() == 1) {
                    return adminService.loginWithPasswordForAdmin(loginDetails, request, session);
                } else {
                    return responseService.generateErrorResponse("Custom Admin with username " + username + " does not have " + roleService.findRoleName(role) + " role", HttpStatus.BAD_REQUEST);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_ROLE, HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }


}
