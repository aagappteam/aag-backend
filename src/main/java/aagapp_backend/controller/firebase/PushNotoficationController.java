package aagapp_backend.controller.firebase;

import aagapp_backend.services.firebase.NotoficationFirebase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam String body,
            @RequestParam String token) {
       try{
           notificationService.sendNotification(title, body, token);

           return new ResponseEntity<>("Notification sent successfully", HttpStatus.OK);
       }catch (Exception e){
           return new ResponseEntity<>("Failed to send notification", HttpStatus.INTERNAL_SERVER_ERROR);
       }

    }
}
