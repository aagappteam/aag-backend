package aagapp_backend.entity;
import aagapp_backend.enums.ProfileStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "vendor_submission_details", indexes = {
        @Index(name = "idx_approved", columnList = "approved"),
        @Index(name = "idx_profile_status", columnList = "profile_status"),
        @Index(name = "idx_email_vendor_submission", columnList = "email")


})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VendorSubmissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull(message = "First name is required")
    private String firstName;
    @NotNull(message = "Last name is required")
    private String lastName;

    @OneToOne
    @JoinColumn(name = "service_provider_id")
    @JsonBackReference("submissionentity-vendor")
    private VendorEntity vendorEntity;


    @NotNull(message = "Email is required")
    @Email(message = "invalid email format")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = "Please enter a valid email address.")
    private String email;

    @NotNull(message = "Plan name is required")
    private String planName;

    private String planType;

    private Boolean approved;

    @Column(name = "profile_status")
    private ProfileStatus profileStatus;

    @ElementCollection
    private Map<String, String> socialMediaUrls;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void ensureApproved() {
        if (approved == null) {
            approved = false;
        }
    }

    public String getMobileNumber() {
        return vendorEntity != null ? vendorEntity.getMobileNumber() : null;
    }

}
