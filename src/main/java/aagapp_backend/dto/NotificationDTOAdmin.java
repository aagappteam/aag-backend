package aagapp_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Timestamp;
import java.time.ZonedDateTime;


@Getter
@Setter
@NoArgsConstructor
public class NotificationDTOAdmin {

    private Long id;
    private Long vendorId;
    private String role;
    private Long customerId;
    private String description;
    private String details;
    private String createdDate;

    private Double amount;
    private String vendorName;
    private String vendorEmail;

    public NotificationDTOAdmin(Long id, Long vendorId, String role, Long customerId,
                                String description, String details, String createdDate,
                                Double amount, String vendorName, String vendorEmail) {
        this.id = id;
        this.vendorId = vendorId;
        this.role = role;
        this.customerId = customerId;
        this.description = description;
        this.details = details;
        this.createdDate = createdDate;
        this.amount = amount;
        this.vendorName = vendorName;
        this.vendorEmail = vendorEmail;
    }


}

