package aagapp_backend.dto;

import aagapp_backend.entity.ticket.Ticket;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class TicketDTO {
    private Long ticketId;
    private String subject;
    private String description;
    private String status;
    private String remark;
    private String priority;
    private String type;
    private String email;
    private String role;
    private Date createdDate;
    private Date updatedDate;

    // Constructor, Getters and Setters
    public TicketDTO(Ticket ticket) {
        this.ticketId = ticket.getTicketId();
        this.subject = ticket.getSubject();
        this.description = ticket.getDescription();
        this.status = ticket.getStatus();
        this.remark = ticket.getRemark();
        this.priority = ticket.getPriority();
        this.type = ticket.getType();
        this.email = ticket.getEmail();
        this.role = ticket.getRole();
        this.createdDate = ticket.getCreatedDate();
        this.updatedDate = ticket.getUpdatedDate();
    }

    // Getters and Setters
}

