package aagapp_backend.dto;

import aagapp_backend.entity.ticket.TicketMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {

    private Long ticketId;
    private String subject;
    private String description;
    private String status;
    private String role;
    private Long customerOrVendorId;
    private String priority;
    private String assignedTeam;
    private List<TicketMessage> messages;
    private Date createdDate;
    private Date updatedDate;
    private String name;
    private String email;
    private String mobile;
    private String profilePic;
}
