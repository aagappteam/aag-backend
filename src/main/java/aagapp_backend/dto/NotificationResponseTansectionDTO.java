package aagapp_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseTansectionDTO {

    private Long id;
    private Long vendorId;
    private Long customerId;
    private String role;
    private String name;
    private String description;
    private Double amount;
    private String details;
    private ZonedDateTime createdDate;

    // extra fields
    private String userNameOrVendorName;
    private String email;
    private String mobileNumber;
}
