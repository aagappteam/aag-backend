package aagapp_backend.dto.admin;

import aagapp_backend.entity.notification.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private Long id;
    private Long vendorId;
    private Long customerId;
    private String role;
    private String name;
    private String email;
    private String description;
    private Double amount;
    private String details;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime createdDate;

    public Transaction(Notification n, String name, String email) {
        this.id = n.getId();
        this.vendorId = n.getVendorId();
        this.customerId = n.getCustomerId();
        this.role = n.getRole();
        this.name = name;
        this.email = email;
        this.description = n.getDescription();
        this.amount = n.getAmount();
        this.details = n.getDetails();
        this.createdDate = n.getCreatedDate();
    }
}
