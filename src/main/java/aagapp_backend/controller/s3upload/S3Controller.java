package aagapp_backend.controller.s3upload;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.s3services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private JwtUtil jwtService;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CustomCustomerRepository customerRepository;


    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("profilePicture") MultipartFile file,
                                        @RequestHeader("Authorization") String token) {
        if (file.isEmpty()) {
            return ResponseService.generateErrorResponse("Please select a file to upload", HttpStatus.BAD_REQUEST);
        }

        try {
            // Extract User ID and Role from JWT Token
            String jwtToken = token.replace("Bearer ", "");
            Long userId = jwtService.extractId(jwtToken);
            int role = jwtService.extractRoleId(jwtToken);

            // Generate unique filename using timestamp
            String fileExtension = "";
            String originalFilename = file.getOriginalFilename();

            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String key = "profilePicture/" + System.currentTimeMillis() + fileExtension;

            // Upload file to S3
            s3Service.uploadPhoto(key, file);
            String fileUrl = s3Service.getFileUrl(key);

            // **Update Profile URL based on Role**
            if (role == Constant.VENDOR_ROLE) { // Role 4 -> VendorEntity
                Optional<VendorEntity> vendorOptional = vendorRepository.findById(userId);
                if (vendorOptional.isPresent()) {
                    VendorEntity vendor = vendorOptional.get();
                    vendor.setProfilePic(fileUrl);
                    vendorRepository.save(vendor);
                } else {
                    return ResponseService.generateErrorResponse("Vendor not found", HttpStatus.NOT_FOUND);
                }
            } else if (role == Constant.CUSTOMER_ROLE) { // Role 5 -> CustomCustomer
                Optional<CustomCustomer> customerOptional = customerRepository.findById(userId);
                if (customerOptional.isPresent()) {
                    CustomCustomer customer = customerOptional.get();
                    customer.setProfilePic(fileUrl);
                    customerRepository.save(customer);
                } else {
                    return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }
            } else {
                return ResponseService.generateErrorResponse("Unauthorized role", HttpStatus.UNAUTHORIZED);
            }

            return ResponseService.generateSuccessResponse("Your profile image uploaded successfully", fileUrl, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseService.generateErrorResponse("File upload failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete-profile-pic")
    public ResponseEntity<?> deleteProfilePicture(@RequestHeader("Authorization") String token) {
        try {

            String jwtToken = token.replace("Bearer ", "");
            Long userId = jwtService.extractId(jwtToken);
            int role = jwtService.extractRoleId(jwtToken);

            String fileUrl = null;


            if (role == Constant.VENDOR_ROLE) {
                Optional<VendorEntity> vendorOptional = vendorRepository.findById(userId);
                if (vendorOptional.isPresent()) {
                    VendorEntity vendor = vendorOptional.get();
                    fileUrl = vendor.getProfilePic();
                    vendor.setProfilePic(null);
                    vendorRepository.save(vendor);
                } else {
                    return ResponseService.generateErrorResponse("Vendor not found", HttpStatus.NOT_FOUND);
                }
            } else if (role == Constant.CUSTOMER_ROLE) {
                Optional<CustomCustomer> customerOptional = customerRepository.findById(userId);
                if (customerOptional.isPresent()) {
                    CustomCustomer customer = customerOptional.get();
                    fileUrl = customer.getProfilePic();
                    customer.setProfilePic(null);
                    customerRepository.save(customer);
                } else {
                    return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }
            } else {
                return ResponseService.generateErrorResponse("Unauthorized role", HttpStatus.UNAUTHORIZED);
            }

            if (fileUrl != null && !fileUrl.isEmpty()) {
                String key = fileUrl.substring(fileUrl.indexOf("profilePicture/"));
                s3Service.deleteFile(key);
            }

            return ResponseService.generateSuccessResponse("Profile picture deleted successfully", null, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Profile picture deletion failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/upload-banner")
    public ResponseEntity<?> uploadFileWithoutAuth(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseService.generateErrorResponse("Please select a file to upload", HttpStatus.BAD_REQUEST);
        }
        try {
            // Generate unique filename using timestamp
            String fileExtension = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String key = "banner/" + System.currentTimeMillis() + fileExtension;

            // Upload file to S3
            s3Service.uploadPhoto(key, file);
            String fileUrl = s3Service.getFileUrl(key);

            return ResponseService.generateSuccessResponse("File uploaded successfully", fileUrl, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("File upload failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete file from S3 without authorization
     */
    @DeleteMapping("/delete-banner")
    public ResponseEntity<?> deleteFileWithoutAuth(@RequestBody Map<String, Object> formDetails) {
        try {
            String fileUrl = (String) formDetails.get("fileUrl");
            if (fileUrl == null || fileUrl.isEmpty()) {
                return ResponseService.generateErrorResponse("File URL is required", HttpStatus.BAD_REQUEST);
            }
            String key = fileUrl.substring(fileUrl.indexOf("banner/"));
            s3Service.deleteFile(key);

            return ResponseService.generateSuccessResponse("File deleted successfully", null, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("File deletion failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
