package aagapp_backend.services.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotoficationFirebase {


    public String sendNotification(String title, String body, String token) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            return FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending notification: " + e.getMessage();
        }
    }


    private Map<String, String> getSamplePayloadData() {
        Map<String, String> pushData = new HashMap<>();
        pushData.put("messageId", "123");
        pushData.put("text", " Hello. This is payload content "+ " " + LocalDateTime.now());
        return pushData;
    }


   /* private PushNotificationRequest getSamplePushNotificationRequest() {
        PushNotificationRequest request = new PushNotificationRequest(defaults.get("title"),
                defaults.get("message"),
                defaults.get("topic"));
        return request;
    }*/


}
