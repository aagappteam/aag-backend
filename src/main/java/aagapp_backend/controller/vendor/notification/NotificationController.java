package aagapp_backend.controller.vendor.notification;

import aagapp_backend.dto.CreateNotificationRequest;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.services.NotificationService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;

    // Create a new notification
    @PostMapping("/create")
    public ResponseEntity<?> createNotification(@RequestBody CreateNotificationRequest request, @RequestHeader("Authorization") String token) {
        try {
            ResponseEntity<?> createdNotificationResponse = notificationService.createNotification(request, token);

            return createdNotificationResponse;  // Return the ResponseEntity from the service

        } catch (Exception e) {
            exceptionHandling.handleException(e);  // Custom exception handling
            return responseService.generateErrorResponse("Error creating notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{role}/{id}")
    public ResponseEntity<?> getNotifications(
            @PathVariable Long id,
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,  // Default to page 0
            @RequestParam(defaultValue = "10") int size, // Default to size 10
            @RequestParam(required = false) String transaction, // New filter: transaction (non-null amount)
            @RequestParam(required = false) String activity) { // New filter: activity

        try {
            // Fetch notifications from the service layer with pagination and filters
            List<Notification> notifications = notificationService.getNotifications(id, role, page, size, transaction, activity);

            if (notifications.isEmpty()) {
                return responseService.generateErrorResponse("No notifications found", HttpStatus.OK);
            }

            // Return the notifications wrapped in ResponseEntity
            return responseService.generateSuccessResponse("Notifications fetched successfully", notifications, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching notifications: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}