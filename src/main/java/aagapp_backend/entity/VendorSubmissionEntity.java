package aagapp_backend.entity;
import aagapp_backend.enums.ProfileStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Map;

@Entity
@Table(name = "vendor_submission_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VendorSubmissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @OneToOne
    @JoinColumn(name = "service_provider_id")
    @JsonBackReference("submissionentity-vendor")
    private VendorEntity vendorEntity;

/*
    private String mobileNumber;
*/


    @Email(message = "invalid email format")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = "Please enter a valid email address.")
    private String email;

    private String planName;

    private String planType;

    private Boolean approved;

    private ProfileStatus profileStatus;

    @ElementCollection
    private Map<String, String> socialMediaUrls;

    @PrePersist
    @PreUpdate
    public void ensureApproved() {
        // Ensure approved is never null
        if (approved == null) {
            approved = false;
        }

        // Ensure any other fields that should default to false or empty are handled
        if (firstName == null) {
            firstName = "Unknown";
        }

        if (lastName == null) {
            lastName = "Unknown";
        }

        if (planName == null) {
            planName = "Not Assigned";
        }

        if (planType == null) {
            planType = "Not Assigned";
        }

        if (profileStatus == null) {
            profileStatus = ProfileStatus.PENDING; // assuming you want a default status
        }
    }

    public String getMobileNumber() {
        return vendorEntity != null ? vendorEntity.getMobileNumber() : null;
    }

}
