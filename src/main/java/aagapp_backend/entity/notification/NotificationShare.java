package aagapp_backend.entity.notification;

import aagapp_backend.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_vendor_id_notificationshare", columnList = "vendorId"),
                @Index(name = "idx_created_date_notificationshare", columnList = "created_date"),
                @Index(name = "idx_vendor_created_date_notificationshare", columnList = "vendorId, created_date"),
                @Index(name = "idx_details_notificationshare", columnList = "details"),
                @Index(name = "idx_amount_notificationshare", columnList = "amount")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendorId")
    private Long vendorId;

    private String description;

    private Double amount;

    private String details;  // Additional details like game name, tournament name

    @Nullable
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    @Column(name = "created_date", updatable = false)
    private ZonedDateTime createdDate;

    @PrePersist
    public void setCreatedDate() {
        this.createdDate = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

}


