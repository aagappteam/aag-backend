package aagapp_backend.entity;
import aagapp_backend.enums.VendorLevelPlan;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "vendor_table")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VendorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_provider_id")
    private Long service_provider_id;

    @Nullable
    private String user_name;

    @Nullable
    private String first_name;
    @Nullable

    private String last_name;
    @Nullable

    private String country_code;


    private String mobileNumber;
    private String otp;

    @Nullable
    private int role;

    @Nullable
    @Email(message = "invalid email format")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Please enter a valid email address.")
    private String primary_email;

    @Nullable
    private String password;

    private int signedUp = 0;

    private int isVerified = 0;


    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;


/*    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "status_id", referencedColumnName = "status_id")
    private ServiceProviderStatus status;*/


/*    @ManyToMany
    @JoinTable(
            name = "service_provider_privileges", // The name of the join table
            joinColumns = @JoinColumn(name = "service_provider_id"), // Foreign key for ServiceProvider
            inverseJoinColumns = @JoinColumn(name = "privilege_id")) // Foreign key for Privilege
    private List<Privileges> privileges;*/

    @Nullable
    @Column(length = 512)
    private String token;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_private")
    private Boolean isPrivate=false;

    @Column(name = "is_paused")
    private Boolean isPaused=false;

    @Nullable
    @Column(name = "pause_reason")
    private String pauseReason;

    @Nullable
    @Column(name = "referral_code", unique = true)
    private String referralCode;


    @Nullable
    @Column(name = "referred_count")
    private int referralCount;

    /*@JsonManagedReference("vendor-referred")
    @OneToMany(mappedBy = "referrerId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorReferral> vendorReferralList = new ArrayList<>();*/

    @JsonManagedReference("vendor-referrer")  //for the referrer relationship
    @OneToMany(mappedBy = "referrerId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorReferral> vendorReferralList = new ArrayList<>();

    @JsonManagedReference("vendor-referred")  // Unique name for the referred relationship
    @OneToMany(mappedBy = "referredId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorReferral> referredVendorReferralList = new ArrayList<>();


    private VendorLevelPlan vendorLevelPlan;

    @JsonBackReference("bankDetails-vendor")
    @OneToMany(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorBankDetails> bankDetails = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;


    @CurrentTimestamp
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedDate;

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = new Date();
    }


    @JsonManagedReference("submissionentity-vendor")
    @OneToOne(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private VendorSubmissionEntity submissionEntity;

}


