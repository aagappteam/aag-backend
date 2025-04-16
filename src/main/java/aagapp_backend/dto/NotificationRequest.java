package aagapp_backend.dto;
import aagapp_backend.entity.Challenge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String title;
    private String body;
    private String topic;
    private String token;
}
