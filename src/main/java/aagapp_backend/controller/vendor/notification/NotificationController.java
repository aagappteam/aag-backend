package aagapp_backend.controller.vendor.notification;

import aagapp_backend.dto.CreateNotificationRequest;
import aagapp_backend.dto.NotificationDTO;
import aagapp_backend.dto.UserNotificationDTO;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.notification.UserNotification;
import aagapp_backend.services.NotificationService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String transaction,
            @RequestParam(required = false) String activity) {

        try {


            if ("vendor".equalsIgnoreCase(role)) {
                List<NotificationDTO> notificationDTOs;
                long totalElements;
                Page<Notification> notificationPage =
                        notificationService.getNotifications(id, role, page, size, transaction, activity);

                if (notificationPage.isEmpty()) {
                    return responseService.generateErrorResponse("No notifications found", HttpStatus.OK);
                }

                notificationDTOs = notificationPage.getContent()
                        .stream()
                        .map(NotificationDTO::new)
                        .collect(Collectors.toList());
                totalElements = notificationPage.getTotalElements();

                return responseService.generateSuccessResponseWithCount(
                        "Notifications fetched successfully",
                        notificationDTOs,
                        totalElements,
                        HttpStatus.OK
                );

            } else {
                List<UserNotificationDTO> notificationDTOs;
                long totalElements;
                Page<UserNotification> notificationPage =
                        notificationService.getNotificationsOfuser(id, role, page, size, transaction, activity);

                if (notificationPage.isEmpty()) {
                    return responseService.generateErrorResponse("No notifications found", HttpStatus.OK);
                }

                notificationDTOs = notificationPage.getContent()
                        .stream()
                        .map(UserNotificationDTO::new)  // assuming you have a constructor for UserNotification too
                        .collect(Collectors.toList());
                totalElements = notificationPage.getTotalElements();

                return responseService.generateSuccessResponseWithCount(
                        "Notifications fetched successfully",
                        notificationDTOs,
                        totalElements,
                        HttpStatus.OK
                );
            }



        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    "Error fetching notifications: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


/*    @GetMapping("/{role}/{id}")
    public ResponseEntity<?> getNotifications(
            @PathVariable Long id,
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String transaction,
            @RequestParam(required = false) String activity) {

        try {


            if(role.equals("vendor")) {
                Page<Notification> notificationPage = notificationService.getNotifications(id, role, page, size, transaction, activity);

            }else{
                Page<UserNotification> notificationPage = notificationService.getNotificationsOfuser(id, role, page, size, transaction, activity);

            }
            if (notificationPage.isEmpty()) {
                return responseService.generateErrorResponse("No notifications found", HttpStatus.OK);
            }

            List<NotificationDTO> notificationDTOs = notificationPage.getContent()
                    .stream()
                    .map(NotificationDTO::new)
                    .collect(Collectors.toList());

            return responseService.generateSuccessResponseWithCount(
                    "Notifications fetched successfully",
                    notificationDTOs,
                    notificationPage.getTotalElements(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching notifications: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/



}