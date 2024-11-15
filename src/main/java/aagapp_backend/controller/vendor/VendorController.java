package aagapp_backend.controller.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/vendor")

public class VendorController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private VenderService vendorService;

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

    @GetMapping("/get-service-provider")
    public ResponseEntity<?> getServiceProviderById(@RequestParam Long userId) {
        try {
            VendorEntity serviceProviderEntity = vendorService.getServiceProviderById(userId);
            if (serviceProviderEntity == null) {
                return responseService.generateErrorResponse("Service provider not found " + userId, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> details = new HashMap<>();
            details.put("status", ApiConstants.STATUS_SUCCESS);
            details.put("status_code", HttpStatus.OK);
            details.put("data", serviceProviderEntity);
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
            VendorEntity serviceProviderToBeDeleted = entityManager.find(VendorEntity.class, userId);
            if (serviceProviderToBeDeleted == null)
                return responseService.generateErrorResponse("No record found", HttpStatus.NOT_FOUND);
            else
                entityManager.remove(serviceProviderToBeDeleted);
            return responseService.generateSuccessResponse("Service Provider Deleted", null, HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting: " + e.getMessage());
        }
    }
}
