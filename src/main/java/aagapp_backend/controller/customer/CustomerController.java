package aagapp_backend.controller.customer;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer")

public class CustomerController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;


    @Autowired
    private ResponseService responseService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private CustomCustomerService customCustomerService;

    @GetMapping("/get-all-customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        try {

            int startPosition = offset * limit;
            TypedQuery<CustomCustomer> query = entityManager.createQuery(Constant.GET_ALL_CUSTOMERS, CustomCustomer.class);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<CustomCustomer> results = new ArrayList<>();
            for (CustomCustomer customer : query.getResultList()) {
                CustomCustomer customerToadd = customCustomerService.readCustomerById(customer.getId());
                results.add(customerToadd);
            }
            return ResponseService.generateSuccessResponse("List of customers : ", results, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in customers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PostMapping("create-or-update-password")
    public ResponseEntity<?> deleteServiceProvider(@RequestBody Map<String, Object> passwordDetails, @RequestParam long userId) {
        try {

            String password = (String) passwordDetails.get("password");
             String newPassword = (String) passwordDetails.get("newPassword");
            VendorEntity serviceProvider = entityManager.find(VendorEntity.class, userId);
            if (serviceProvider == null)
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);
            if (serviceProvider.getPassword() == null) {
                serviceProvider.setPassword(passwordEncoder.encode(password));
                entityManager.merge(serviceProvider);
                return responseService.generateSuccessResponse("Password created", serviceProvider, HttpStatus.OK);
            } else {
                if (password == null || newPassword == null)
                    return responseService.generateErrorResponse("Empty password entered", HttpStatus.BAD_REQUEST);
                if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(newPassword));
                if (!passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(password));
                    entityManager.merge(serviceProvider);
                    return responseService.generateSuccessResponse("New Password Set", serviceProvider, HttpStatus.OK);
                }
                return responseService.generateErrorResponse("Old Password and new Password cannot be same", HttpStatus.BAD_REQUEST);
            }else
                    return new ResponseEntity<>("Password do not match", HttpStatus.BAD_REQUEST);}
        }   catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error changing/updating password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-user")
    public ResponseEntity<?> getUserById(@RequestParam Long userId) {
        try {
            CustomCustomer customCustomer = customCustomerService.readCustomerById(userId);
            if (customCustomer == null) {
                return responseService.generateErrorResponse("User not found " + userId, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            details.put("status", ApiConstants.STATUS_SUCCESS);
            details.put("status_code", HttpStatus.OK);
            details.put("data", customCustomer);
            return responseService.generateSuccessResponse("Service provider details are", details, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @DeleteMapping("delete")
    public ResponseEntity<?> deleteServiceProvider(@RequestParam Long userId) {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, userId);
            if (customCustomer == null)
                return responseService.generateErrorResponse("No record found", HttpStatus.NOT_FOUND);
            else
                entityManager.remove(customCustomer);
            return responseService.generateSuccessResponse("Customer Deleted", null, HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(Constant.BEARER_CONST)) {
            return ResponseEntity.badRequest().body("Token is required");
        }

        String token = authorizationHeader.substring(7);
        try {
            jwtUtil.logoutUser(token);
            return responseService.generateSuccessResponse("Logged out successfully", null, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during logout");
        }
    }



}
