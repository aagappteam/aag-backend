package aagapp_backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "service_vendor")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VendorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long service_provider_id;

    @Nullable
    private String user_name;

    @Nullable
    private String first_name;
    @Nullable

    private String last_name;
    @Nullable

    private String country_code;

/*
    @Pattern(regexp = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$", message = "Date of birth must be in the format DD-MM-YYYY")
    private String date_of_birth;*/


    private String mobileNumber;
    private String otp;

    @Nullable
    private int role;

    @Nullable
    @Email(message = "invalid email format")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = "Please enter a valid email address.")
    private String primary_email;

    @Nullable
    private String password;

    @Nullable
    private String business_location;


    private int signedUp=0;

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

/*    @JsonIgnore
    @JsonBackReference
    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerReferrer> myReferrals = new ArrayList<>();
    */
/*    @Column(name="is_active")
    private Boolean isActive;*/

    @JsonBackReference("bankDetails-vendor")
    @OneToMany(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorBankDetails> bankDetails = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;


    @Nullable
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedDate;


    @JsonManagedReference("submissionentity-vendor")
    @OneToOne(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private VendorSubmissionEntity submissionEntity;

}


