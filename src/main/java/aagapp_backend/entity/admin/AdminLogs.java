package aagapp_backend.entity.admin;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "admin_logs"
        /*indexes = {
                @Index(name = "idx_activity_admin_logs", columnList = "activity"),
//                @Index(name = "idx_role_admin_logs", columnList = "role"),
                @Index(name = "idx_createdFor_admin_logs", columnList = "createdDate")
        }*/
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderid;
    private String senderrole;

    @Column(length = 1000)
    private String message;

    private String targetType; // e.g., "KYC", "PLAN", "TICKET"
    private Long targetId;

    private String targetRole; // who this originally came for
    private Long receiverId;   // direct user receiver (optional)

    private String assignedRole;     // assigned to role
    private Long assignedUserId;     // assigned to a specific user
    private Long assignedBy;         // admin who assigned

    private boolean read = false;


    @Column(name = "performed_by", length = 255)
    private String performedBy;


    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_date", updatable = false)
    private ZonedDateTime createdDate;



    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime updatedDate;


    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }

}
