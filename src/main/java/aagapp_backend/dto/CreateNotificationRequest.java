package aagapp_backend.dto;

import aagapp_backend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationRequest {

    private Long userId; // This would come from the logged-in user in a real app
/*
    private NotificationType type;
*/
    private String description;
    private Double amount;
    private String details;
}