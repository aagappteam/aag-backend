package aagapp_backend.entity;

import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.enums.LeagueStatus;
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
@Table(name = "vendor_table", indexes = {
        @Index(name = "idx_vendor_id", columnList = "service_provider_id"),
        @Index(name = "idx_vendor_email", columnList = "primary_email"),
        @Index(name = "idx_vendor_referral_code", columnList = "referral_code"),
        @Index(name = "idx_vendor_is_active", columnList = "is_active"),
        @Index(name = "idx_vendor_is_paid", columnList = "is_paid")
})
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
    @Column(name = "profile_picture")
    private String profilePic="https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg";

    @Nullable
    @Column(name = "banner_picture")
    private String bannerPicture;

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

    @OneToMany(mappedBy = "vendorEntity", fetch = FetchType.LAZY)
    private List<PaymentEntity> payments;  // One vendor can have many payments (subscriptions)

    @Nullable
    private String password;

    private int signedUp = 0;

    private int isVerified = 0;


    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;


    @Nullable
    @Column(length = 512)
    private String token;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_private")
    private Boolean isPrivate = false;

    @Column(name = "is_paused")
    private Boolean isPaused = false;

    @Nullable
    @Column(name = "pause_reason")
    private String pauseReason;

    @Nullable
    @Column(name = "referral_code", unique = true)
    private String referralCode;

    @Nullable
    @Column(name = "referred_count")
    private int referralCount;

    @Enumerated(EnumType.STRING)
    private VendorLevelPlan vendorLevelPlan = VendorLevelPlan.getDefaultLevel();

    @JsonBackReference("bankDetails-vendor")
    @OneToMany(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorBankDetails> bankDetails = new ArrayList<>();

    @Column(name = "wallet_balance", nullable = false)
    private double walletBalance = 0.0;

    @Column(name = "league_status", nullable = false)
    @Enumerated(EnumType.STRING)

    private LeagueStatus leagueStatus;

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


    @PrePersist
    public void prePersist() {
        this.setLeagueStatus(LeagueStatus.NOT_PAID);
    }

    @JsonManagedReference("submissionentity-vendor")
    @OneToOne(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private VendorSubmissionEntity submissionEntity;

}


