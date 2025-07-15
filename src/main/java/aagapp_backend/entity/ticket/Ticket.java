package aagapp_backend.entity.ticket;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.AssignedTeam;
import aagapp_backend.enums.TicketEnum;
import aagapp_backend.enums.TicketPriority;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_id", columnList = "id"),
        @Index(name = "idx_role_ticket", columnList = "role"),
        @Index(name = "idx_status_ticket", columnList = "status"),
        @Index(name = "idx_created_date_ticket", columnList = "created_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String description;

    private TicketEnum status;

//    private String remark;

    private Long customerOrVendorId;

    private String role;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketMessage> messages;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private TicketPriority priority=TicketPriority.NOT_SET;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_team")
    private AssignedTeam assignedTeam=AssignedTeam.NOT_ASSIGNED;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    @CurrentTimestamp
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedDate;



}