package aagapp_backend.dto;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {

    private Long id;
    private String subject;
    private String description;
    private String status;
    private String remark;
    private Long customerOrVendorId;
    private String role;
    private String createdDate;
    private String updatedDate;
}
