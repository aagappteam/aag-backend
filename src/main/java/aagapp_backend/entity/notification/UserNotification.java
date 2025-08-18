package aagapp_backend.entity.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name="usernotification",
        indexes = {
                @Index(name = "idx_vendor_id_notificationuser", columnList = "vendorId"),
                @Index(name = "idx_customer_id_notificationuser", columnList = "customerId"),
                @Index(name = "idx_created_date_notificationuser", columnList = "created_date"),
                @Index(name = "idx_vendor_customer_created_date_notificationuser", columnList = "vendorId, customerId, created_date"),
                @Index(name = "idx_amount_notificationuser", columnList = "amount")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendorId")
    private Long vendorId;
    private String role;

    @Column(name = "customerId")
    private Long customerId;

    private String name;

/*    @Enumerated(EnumType.STRING)
    private NotificationType type;*/

    private String description;

    private Double amount;  // If applicable, for wallet credit/debit

    private String details;  // Additional details like game name, tournament name

    @Column(name = "created_date", updatable = false)
    private ZonedDateTime createdDate;

    @PrePersist
    public void setCreatedDate() {
        this.createdDate = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
    }



}