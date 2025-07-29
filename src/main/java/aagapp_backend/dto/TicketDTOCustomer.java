package aagapp_backend.dto;



import aagapp_backend.entity.ticket.TicketMessage;
import lombok.*;


import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTOCustomer {

    private Long id;
    private String subject;
    private String description;
    private String status;
    private String remark;
    private Long customerOrVendorId;
    private List<TicketMessage> messages;
    private String role;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}

