package aagapp_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_role_table")
@NoArgsConstructor
@Getter
@Setter
public class Role
{
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY) // This tells Hibernate to auto-generate the value

private int roleId; // Use camel case for consistency
    private String roleName;
    private LocalDateTime createdAt; // Use LocalDateTime for timestamps
    private LocalDateTime updatedAt; // Use LocalDateTime for timestamps
    private String createdBy;

    // Constructor with parameters for role creation
    public Role(int roleId, String roleName, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }
}
