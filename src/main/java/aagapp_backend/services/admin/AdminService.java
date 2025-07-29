package aagapp_backend.services.admin;

import aagapp_backend.components.CommonData;
import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.Role;
import aagapp_backend.entity.admin.Privilege;
import aagapp_backend.repository.admin.CustomAdminRepository;
import aagapp_backend.repository.admin.PrivilegeRepository;
import aagapp_backend.repository.admin.RoleRepository;
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
import java.util.*;

import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

@Service
public class AdminService
{

    @Autowired
    private PrivilegeRepository privilegeRepo;

    @Autowired
    private CustomAdminRepository customAdminRepository;

    private RoleRepository roleRepo;
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
    public void setRoleRepo(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
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
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<CustomAdmin> findAdminsByRole(int role) {
        return entityManager.createQuery(
                        "SELECT a FROM CustomAdmin a WHERE a.role = :role AND a.active = 1", CustomAdmin.class)
                .setParameter("role", role)
                .getResultList();
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

/*   @Transactional
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
                    int targetRoleId = 2;

                    boolean hasRole = customAdmin.getRole().stream()
                            .anyMatch(r -> r.getRoleId() == targetRoleId);

                    if (!hasRole) {
                        return responseService.generateErrorResponse(
                                "Custom Admin with username " + mobileNumber + " does not have role " + roleService.findRoleName(targetRoleId),
                                HttpStatus.BAD_REQUEST
                        );
                    }

                }
                else if(roleService.findRoleName(role).equals(Constant.SUPPORT))
                {
                    int supportRoleId = 1;

                    boolean hasSupportRole = customAdmin.getRoles().stream()
                            .anyMatch(r -> r.getRoleId() == supportRoleId);

                    if (!hasSupportRole) {
                        return responseService.generateErrorResponse(
                                "Support with username " + mobileNumber + " does not have role " + roleService.findRoleName(role),
                                HttpStatus.BAD_REQUEST
                        );
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
                    String newToken = jwtUtil.generateToken(customAdmin.getAdminId(), role, ipAddress, userAgent);

                    customAdmin.setToken(newToken);
                    entityManager.persist(customAdmin);
                    Map<String, Object> responseBody = createAuthResponseForAdmin(newToken, customAdmin).getBody();
*//*                    if(customAdmin.getSignedUp()==0) {
                        customAdmin.setSignedUp(1);
                        entityManager.merge(customAdmin);
                        responseBody.put("message", "User has been signed up");
                    }*//*
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
    }*/

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

    public ResponseEntity<?> loginWithPasswordForAdmin(@RequestBody Map<String, Object> customAdminDetails,
                                                       HttpServletRequest request,
                                                       HttpSession session) {
        try {
            String mobileNumber = (String) customAdminDetails.get("mobileNumber");
            if (mobileNumber != null && mobileNumber.startsWith("0")) {
                mobileNumber = mobileNumber.substring(1);
            }

            String password = (String) customAdminDetails.get("password");
            String countryCode = (String) customAdminDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);

            if (password == null || password.isEmpty()) {
                return responseService.generateErrorResponse("Password cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("Empty Phone Number", HttpStatus.BAD_REQUEST);
            }

            // 🔐 Authenticate Admin
            CustomAdmin customAdmin = findAdminByPhone(mobileNumber, countryCode);
            if (customAdmin == null) {
                return responseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
            }

            if (!passwordEncoder.matches(password, customAdmin.getPassword())) {
                return responseService.generateErrorResponse("Invalid Password", HttpStatus.BAD_REQUEST);
            }

            String token = jwtUtil.generateToken(
                    customAdmin.getAdminId(),
                    customAdmin.getRole(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );

            // 📦 Base Response
            Map<String, Object> data = new HashMap<>();
            data.put("customAdminDetails", customAdmin);

            Map<String, Object> response = new HashMap<>();
            response.put("status_code", 200);
            response.put("status", "OK");
            response.put("message", "Admin has been logged in");
            response.put("token", token);
            response.put("data", data);

            // ✅ Skip menus/privileges for super admin (roleId = 2)
            if (customAdmin.getRole() != 2) {
                Role role = roleRepo.findById((int) customAdmin.getRole())
                        .orElseThrow(() -> new RuntimeException("Role not found"));

                Set<Privilege> rolePrivileges = role.getPrivileges();

                // Find all unique parentMenu names from SUBMENU privileges
                Set<String> parentMenuNames = rolePrivileges.stream()
                        .filter(p -> "SUBMENU".equalsIgnoreCase(p.getType()) && p.getParentMenu() != null)
                        .map(Privilege::getParentMenu)
                        .collect(Collectors.toSet());

                Set<String> assignedMenuNames = rolePrivileges.stream()
                        .filter(p -> "MENU".equalsIgnoreCase(p.getType()))
                        .map(Privilege::getName)
                        .collect(Collectors.toSet());

        // Merge both sets (parent menus + directly assigned menus)
                        Set<String> allMenuNames = new HashSet<>();
                        allMenuNames.addAll(parentMenuNames);
                        allMenuNames.addAll(assignedMenuNames);

        // Fetch all related MENU privilege objects
                        List<Privilege> menuPrivileges = privilegeRepo.findAllByNameIn(allMenuNames);


                List<Map<String, Object>> menusList = new ArrayList<>();

                for (Privilege menu : menuPrivileges) {
                    Map<String, Object> menuMap = new HashMap<>();
                    menuMap.put("menuId", menu.getId());
                    menuMap.put("menuName", menu.getName());

                    List<Map<String, Object>> submenus = rolePrivileges.stream()
                            .filter(p -> "SUBMENU".equalsIgnoreCase(p.getType()) &&
                                    menu.getName().equalsIgnoreCase(p.getParentMenu()))
                            .map(sub -> {
                                Map<String, Object> map = new HashMap<>();
                                map.put("submenuId", sub.getId());
                                map.put("submenuName", sub.getName());
                                return map;
                            })
                            .collect(Collectors.toList());

                    menuMap.put("submenus", submenus);
                    menusList.add(menuMap);
                }

                response.put("menus", menusList);
                response.put("privileges", rolePrivileges.stream().map(Privilege::getName).toList());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Utility method to clean and format menu/submenu names.
     * Example:*



/*    public ResponseEntity<?> loginWithPasswordForAdmin(@RequestBody Map<String, Object> customAdminDetails,
                                                       HttpServletRequest request,
                                                       HttpSession session) {
        try {
            String mobileNumber = (String) customAdminDetails.get("mobileNumber");
            if (mobileNumber != null && mobileNumber.startsWith("0")) {
                mobileNumber = mobileNumber.substring(1);
            }

            String password = (String) customAdminDetails.get("password");
            String countryCode = (String) customAdminDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);

            if (password == null || password.isEmpty()) {
                return responseService.generateErrorResponse("Password cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("Empty Phone Number", HttpStatus.BAD_REQUEST);
            }

            // Authenticate Admin
            CustomAdmin customAdmin = findAdminByPhone(mobileNumber, countryCode);
            if (customAdmin == null) {
                return responseService.generateErrorResponse("User not found", HttpStatus.NOT_FOUND);
            }

            if (!passwordEncoder.matches(password, customAdmin.getPassword())) {
                return responseService.generateErrorResponse("Invalid Password", HttpStatus.BAD_REQUEST);
            }

            // Generate Token
            String token = jwtUtil.generateToken(
                    customAdmin.getAdmin_id(),
                    customAdmin.getRole(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );

            Map<String, Object> data = new HashMap<>();
            data.put("customAdminDetails", customAdmin);

            Map<String, Object> response = new HashMap<>();
            response.put("status_code", 200);
            response.put("status", "OK");
            response.put("message", "Admin has been logged in");
            response.put("token", token);
            response.put("data", data);

            // ✅ Skip menus and privileges for admin role (e.g., roleId = 2)
            if (customAdmin.getRole() != 2) {
                Role role = roleRepo.findById((long) customAdmin.getRole())
                        .orElseThrow(() -> new RuntimeException("Role not found"));

                // Build Menus & Submenus
                Map<String, List<String>> menus = new HashMap<>();
                role.getPrivileges().forEach(priv -> {
                    if ("MENU".equalsIgnoreCase(priv.getType())) {
                        menus.put(priv.getName().replace("ACCESS_", "").replace("_", " "), new ArrayList<>());
                    }
                });

                role.getPrivileges().forEach(priv -> {
                    if ("SUBMENU".equalsIgnoreCase(priv.getType()) && priv.getParentMenu() != null) {
                        String parentKey = priv.getParentMenu().replace("ACCESS_", "").replace("_", " ");
                        menus.computeIfAbsent(parentKey, k -> new ArrayList<>())
                                .add(priv.getName().replace("ACCESS_" + priv.getParentMenu() + "_", "").replace("_", " "));
                    }
                });

                response.put("menus", menus);
                response.put("privileges", role.getPrivileges().stream().map(Privilege::getName).toList());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/


/*
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
*/



    public CustomAdmin findAdminByUsername(String username) {

        return entityManager.createQuery(Constant.USERNAME_QUERY_CUSTOM_ADMIN, CustomAdmin.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
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

    public boolean setAdminActiveStatus(Long adminId, int activeStatus, String modifiedBy) {
        Optional<CustomAdmin> optionalAdmin = customAdminRepository.findByAdminId(adminId);

        if (optionalAdmin.isPresent()) {
            CustomAdmin admin = optionalAdmin.get();

            // If already in desired status, return false
            if (admin.getActive() == activeStatus) {
                return false;
            }

            admin.setActive(activeStatus); // 1 = active, 0 = terminated
            admin.setUpdated_at(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            admin.setCreatedBy(modifiedBy); // Can be used to track who did the action

            customAdminRepository.save(admin);
            return true;
        }

        return false;
    }




}