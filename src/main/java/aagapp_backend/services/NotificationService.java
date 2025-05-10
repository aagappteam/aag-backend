package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.dto.CreateNotificationRequest;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.enums.NotificationType;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.services.vendor.VenderService;
import com.amazonaws.services.ec2.model.CreateNatGatewayRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VenderService vendorService;
    private ResponseService responseService;
    private CustomCustomerService customCustomerService;

    // Create a new notification

    public ResponseEntity<?> createNotification(CreateNotificationRequest notificationRequest , String token) {

        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtil.extractId(jwtToken);
        int role = jwtUtil.extractRoleId(jwtToken);

        if (role == Constant.VENDOR_ROLE) {
            VendorEntity vendor = vendorService.getServiceProviderById(userId);
            if (vendor == null) {
                return responseService.generateErrorResponse("Vendor not found", HttpStatus.NOT_FOUND);
            }
        } else if (role == Constant.CUSTOMER_ROLE) {
            CustomCustomer customer = customCustomerService.getCustomerById(userId);
            if (customer == null) {
                return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
        } else {
            return responseService.generateErrorResponse("Unauthorized role", HttpStatus.UNAUTHORIZED);
        }

        Notification notification = new Notification();
        notification.setVendorId(notificationRequest.getUserId());
/*
        notification.setType(notificationRequest.getType());
*/
        notification.setDescription(notificationRequest.getDescription());
        notification.setAmount(notificationRequest.getAmount());
        notification.setDetails(notificationRequest.getDetails());



        notification.setRole(role == Constant.VENDOR_ROLE ? "Vendor" : "Customer");

        if (role == Constant.VENDOR_ROLE) {
            VendorEntity vendor = vendorService.getServiceProviderById(userId);
            notification.setVendorId(vendor.getService_provider_id());
        } else if (role == Constant.CUSTOMER_ROLE) {
            CustomCustomer customer = customCustomerService.getCustomerById(userId);
            notification.setCustomerId(customer.getId());
        }
        notification = notificationRepository.save(notification);


        return responseService.generateSuccessResponse("Notification created successfully", notification, HttpStatus.CREATED);

    }

    // Retrieve notifications for a specific vendor

    public List<Notification> getNotifications(Long id, String role, int page, int size, String transaction, String activity) {
        try {
            Pageable pageable = PageRequest.of(page, size); // Create a Pageable object using the page and size
            Page<Notification> notificationsPage;

            if ("vendor".equalsIgnoreCase(role)) {
                if (transaction != null && transaction.equalsIgnoreCase("transaction")) {
                    // Filter by non-null amount
                    notificationsPage = notificationRepository.findByVendorIdAndAmountIsNotNullOrderByCreatedDateDesc(id, pageable);
                } else if (activity != null && !activity.isEmpty()) {
                    // Filter by activity (description or details)
                    notificationsPage = notificationRepository.findByVendorIdAndAmountIsNullOrderByCreatedDateDesc(id, pageable);
                } else {
                    // Regular fetch without additional filters
                    notificationsPage = notificationRepository.findByVendorIdOrderByCreatedDateDesc(id, pageable);
                }
            } else if ("customer".equalsIgnoreCase(role)) {
                if (transaction != null && transaction.equalsIgnoreCase("transaction")) {
                    // Filter by non-null amount
                    notificationsPage = notificationRepository.findByCustomerIdAndAmountIsNotNullOrderByCreatedDateDesc(id, pageable);
                } else if (activity != null && !activity.isEmpty()) {
                    // Filter by activity (description or details)
                    notificationsPage = notificationRepository.findByCustomerIdAndAmountIsNullOrderByCreatedDateDesc(id, pageable);
                } else {
                    // Regular fetch without additional filters
                    notificationsPage = notificationRepository.findByCustomerIdOrderByCreatedDateDesc(id, pageable);
                }
            } else {
                throw new IllegalArgumentException("Invalid role specified");
            }

            return notificationsPage.getContent(); // Return the content (list) from the page

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve notifications", e);

        } }


}