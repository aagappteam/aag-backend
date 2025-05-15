package aagapp_backend.dto;

import aagapp_backend.entity.notification.Notification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long vendorId;
    private String role;
    private Long customerId;
    private String description;
    private String details;
    private String createdAt; // formatted date string
    private Double amount;

    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.vendorId = notification.getVendorId();
        this.role = notification.getRole();
        this.customerId = notification.getCustomerId();
        this.description = notification.getDescription();
        this.details = notification.getDetails();
        this.amount = notification.getAmount();

        ZonedDateTime createdDate = notification.getCreatedDate();
        if (createdDate != null) {
            // Convert to specific time zone if needed, e.g., Asia/Kolkata
            ZonedDateTime indiaTime = createdDate.withZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata"));
            this.createdAt = indiaTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            this.createdAt = null;
        }
    }

    // Getters and setters (or use Lombok)
}
