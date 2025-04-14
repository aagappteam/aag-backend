package aagapp_backend.controller.firebase;

import aagapp_backend.dto.NotificationRequest;
import aagapp_backend.dto.PushNotificationResponse;
import aagapp_backend.services.firebase.NotoficationFirebase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/push-notification")
public class PushNotoficationController {

    private final NotoficationFirebase notificationService;

    public PushNotoficationController(NotoficationFirebase notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(
            @RequestParam String title,
            @RequestParam String body) {
        try {
            notificationService.sendNotificationToAll(title, body);
            return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification sent."), HttpStatus.OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to send notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/notification")
    public ResponseEntity sendNotification(@RequestBody NotificationRequest request) throws ExecutionException, InterruptedException {
        notificationService.sendMessageToToken(request);
        return new ResponseEntity<>(new PushNotificationResponse(HttpStatus.OK.value(), "Notification has been sent."), HttpStatus.OK);
    }

}
