package aagapp_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "method_privileges")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MethodPrivilege {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "uri_pattern", nullable = false, length = 255)
    private String uriPattern;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;  // GET, POST, PUT, DELETE

    @Column(name = "privilege_name", nullable = false, length = 50)
    private String privilegeName;

    @Column(name = "description", length = 255)
    private String description;

    // Automatically sets the current timestamp when the record is created
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    // Automatically updates to the current timestamp when the record is updated
    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // Override toString method
    @Override
    public String toString() {
        return "MethodPrivilege{" +
                "id=" + id +
                ", uriPattern='" + uriPattern + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", privilegeName='" + privilegeName + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
