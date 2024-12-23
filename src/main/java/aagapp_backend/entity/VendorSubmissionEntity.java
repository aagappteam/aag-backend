package aagapp_backend.entity;
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
    private String name;

    @OneToOne
    @JoinColumn(name = "service_provider_id")
    @JsonBackReference("submissionentity-vendor")
    private VendorEntity vendorEntity;


    @Email(message = "invalid email format")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = "Please enter a valid email address.")
    private String email;

    private String planName;

    private Boolean approved = false;

    @ElementCollection
    private Map<String, String> socialMediaUrls;

}
