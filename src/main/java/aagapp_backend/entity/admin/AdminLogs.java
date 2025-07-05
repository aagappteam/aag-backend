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
        name = "admin_logs",
        indexes = {
                @Index(name = "idx_activity_admin_logs", columnList = "activity"),
                @Index(name = "idx_role_admin_logs", columnList = "role"),
                @Index(name = "idx_createdFor_admin_logs", columnList = "createdDate")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String activity;

    @Column(length = 10)
    private String role;

    @Column(name = "performed_by", length = 255)
    private String performedBy;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_type", length = 100)
    private String targetType;


    @CreationTimestamp
    @Column(name = "createdDate", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime createdDate;


    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private ZonedDateTime updatedDate;


    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }

}
