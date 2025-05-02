package aagapp_backend.entity;

import aagapp_backend.entity.devices.UserDevice;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.KycStatus;
import aagapp_backend.enums.ProfileStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Set;

@Entity
@Table(
        name = "CUSTOM_USER",
        indexes = {
                @Index(name = "idx_mobile_number_CUSTOM_USER", columnList = "mobile_number"),
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_profile_status_CUSTOM_USER", columnList = "profile_status"),
                @Index(name = "idx_kyc_status", columnList = "kyc_status"),
                @Index(name = "idx_created_date_CUSTOM_USER", columnList = "created_date")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CustomCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerId")
    private Long id;

    @OneToMany(mappedBy = "user")
    @JsonBackReference
    private Set<UserDevice> devices;

    @Nullable
    private String name;

    @Nullable
    private String email;

    @Nullable
    private String password;

    @Nullable // TODO-;
    @Column(name = "profile_picture")
    private String profilePic="https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg";

    @Nullable
    @Column(name = "profile_status")
    private ProfileStatus profileStatus;

    @Nullable
    @Column(name = "referral_code", unique = true)
    private String referralCode;


    @Nullable
    @Column(name = "referred_count")
    private int referralCount;

    @Nullable
    @Column(name = "bonus_balance")
    private BigDecimal bonusBalance = BigDecimal.ZERO;


    @OneToOne(mappedBy = "customCustomer" ,cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JsonManagedReference
    private Wallet wallet;


    @NotNull(message = "Mobile number is required")
    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @Nullable
    @Column(name = "country_code")
    private String countryCode;

    @Nullable
    @Column(name = "otp")
    private String otp;

    /*@Nullable
    @Column(name = "father_name")
    private String fathersName;

    @Nullable
    @Column(name = "nationality")
    private String nationality;


    @Nullable
    @Column(name = "date_of_birth")
    private String dob;

    @Nullable
    @Column(name = "gender")
    private String gender;

    @Nullable
    @Column(name = "residential_address")
    private String residentialAddress;*/

    @Nullable
    @Column(name = "state")
    private String state;

   /* @Nullable
    @Column(name = "district")
    private String district;

    @Nullable
    @Column(name = "city")
    private String city;*/

    @Column(name = "fcm_token")
    private String fcmToken;

    @Nullable
    @Column(length = 512)
    private String token;

    @JsonBackReference("bankDetails-customer")
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankDetails> bankDetails = new ArrayList<>();

    @Column(name = "kyc_status")
    private KycStatus kycStatus = KycStatus.NOT_SUBMITTED;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Player player;

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


}
