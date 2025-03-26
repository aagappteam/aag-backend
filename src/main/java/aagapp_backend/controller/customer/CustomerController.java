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
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    @Transactional
    @GetMapping("/get-all-customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long customerId
    ) {
        try {
            int startPosition = page * limit;

            // If customerId is provided, fetch details for that specific customer
            if (customerId != null) {
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
                if (customCustomer == null) {
                    return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }
                return ResponseService.generateSuccessResponse("Customer details fetched successfully", customCustomer, HttpStatus.OK);
            }

            // Create base query for fetching all customers
            StringBuilder queryString = new StringBuilder(Constant.GET_ALL_CUSTOMERS);

            // Create the query for counting rows
            StringBuilder countQueryString = new StringBuilder("SELECT COUNT(c) FROM CustomCustomer c");

            // Execute the count query to get the total number of matching customers
            Query countQuery = entityManager.createQuery(countQueryString.toString());
            Long totalCount = (Long) countQuery.getSingleResult();

            // Create the query for fetching customers
            Query query = entityManager.createQuery(queryString.toString(), CustomCustomer.class);

            query.setFirstResult(startPosition);
            query.setMaxResults(limit);

            // Execute the query and get the list of customers
            List<CustomCustomer> results = query.getResultList();

            // Return the response including the list of customers and the total count
            return ResponseService.generateSuccessResponseWithCount("List of customers", results, totalCount, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching customers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    @PostMapping("create-or-update-password")
    @CacheEvict(value = "customerDetailsCache", key = "#userId")
    public ResponseEntity<?> createOrUpdatePassword(@RequestBody Map<String, Object> passwordDetails, @RequestParam long userId) {
        try {

            String password = (String) passwordDetails.get("password");
            String newPassword = (String) passwordDetails.get("newPassword");
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, userId);
            if (customCustomer == null)
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);
            if (customCustomer.getPassword() == null) {
                customCustomer.setPassword(passwordEncoder.encode(password));
                entityManager.merge(customCustomer);
                return responseService.generateSuccessResponse("Password created", customCustomer, HttpStatus.OK);
            } else {
                if (password == null || newPassword == null)
                    return responseService.generateErrorResponse("Empty password entered", HttpStatus.BAD_REQUEST);
                if (passwordEncoder.matches(password, customCustomer.getPassword())) {
                    customCustomer.setPassword(passwordEncoder.encode(newPassword));
                    if (!passwordEncoder.matches(password, customCustomer.getPassword())) {
                        customCustomer.setPassword(passwordEncoder.encode(password));
                        entityManager.merge(customCustomer);
                        return responseService.generateSuccessResponse("New Password Set", customCustomer, HttpStatus.OK);
                    }
                    return responseService.generateErrorResponse("Old Password and new Password cannot be same", HttpStatus.BAD_REQUEST);
                } else
                    return new ResponseEntity<>("Password do not match", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error changing/updating password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CacheEvict(value = "customerDetailsCache", key = "#userId")
    @Transactional
    @PatchMapping("update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> userdetails) throws Exception {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, userId);
            if (customCustomer == null)
                return ResponseService.generateErrorResponse("User with provided Id not found", HttpStatus.NOT_FOUND);

            customCustomerService.updateCustomer(userId, userdetails);
            return responseService.generateSuccessResponse("User Details Updated", customCustomer, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Cacheable(value = "customerDetailsCache", key = "#userId")
    @Transactional
    @GetMapping("/get/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            if(userId!=null) {
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, userId);

                return ResponseService.generateSuccessResponse("Customer details : ", customCustomer, HttpStatus.OK);

            }
            return ResponseService.generateErrorResponse("User with provided Id not found", HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.INTERNAL_SERVER_ERROR + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @CacheEvict(value = "customerDetailsCache", key = "#userId")
    @Transactional
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long userId) {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, userId);
            if (customCustomer == null)
                return responseService.generateErrorResponse("No record found", HttpStatus.NOT_FOUND);
            else
                entityManager.remove(customCustomer);
            return responseService.generateSuccessResponse("Customer Deleted", null, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
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
