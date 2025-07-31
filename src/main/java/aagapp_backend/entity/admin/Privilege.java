package aagapp_backend.entity.admin;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import jakarta.persistence.Entity;

@Entity
@Table(
        name = "Privilege",
        indexes = {
                @Index(name = "idx_name_privilege", columnList = "name"),
                @Index(name = "idx_type_privilege", columnList = "type"),
                @Index(name = "idx_parentMenu_privileges", columnList = "parentMenu")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Privilege {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // Example: VIEW_WITHDRAW
    private String type;          // MENU, SUBMENU, API
    private String parentMenu;

    private String createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;




}


