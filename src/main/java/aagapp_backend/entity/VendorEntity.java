package aagapp_backend.entity;

import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.wallet.VendorWallet;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.KycStatus;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.enums.VendorLevelPlan;
import com.fasterxml.jackson.annotation.*;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity

@Table(name = "vendor_table", indexes = {
        @Index(name = "idx_vendor_id", columnList = "service_provider_id"),
        @Index(name = "idx_vendor_email", columnList = "primary_email"),
        @Index(name = "idx_vendor_referral_code", columnList = "referral_code"),
        @Index(name = "idx_vendor_is_active", columnList = "is_active"),
        @Index(name = "idx_vendor_is_paid", columnList = "is_paid"),
        @Index(name = "idx_vendor_mobile_number", columnList = "mobileNumber"),
        @Index(name = "idx_vendor_league_status", columnList = "league_status"),
        @Index(name = "idx_vendor_is_verified", columnList = "isVerified"),
        @Index(name = "idx_vendor_wallet_balance", columnList = "wallet_balance"),
        @Index(name = "idx_vendor_total_wallet_balance", columnList = "total_wallet_balance"),
        @Index(name = "idx_vendor_total_participated_in_game_tournament_league", columnList = "total_participated_in_game_tournament_league"),
        @Index(name = "idx_vendor_wallet_active_league", columnList = "wallet_balance, service_provider_id, profile_picture ,first_name ,last_name")

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

//    @OneToMany(mappedBy = "vendorEntity", fetch = FetchType.LAZY)
    @OneToMany(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaymentEntity> payments;  // One vendor can have many payments (subscriptions)

    @Nullable
    private String password;

    private int signedUp = 0;

    @Column(name = "follower_count",nullable = false)
    private Integer followercount = 0;



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

    private Integer dailyLimit;

    private Integer publishedLimit;

    @OneToOne(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private VendorWallet wallet;


    @Nullable
    @Column(name = "referred_count")
    private int referralCount;

    @Enumerated(EnumType.STRING)
    private VendorLevelPlan vendorLevelPlan = VendorLevelPlan.getDefaultLevel();

    @JsonBackReference("bankDetails-vendor")
    @OneToMany(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendorBankDetails> bankDetails = new ArrayList<>();

//    this is reffal ammount of the vendor
    @Column(name = "wallet_balance", nullable = false)
    private double refferalbalance = 0.0;

    @Column(name = "total_wallet_balance", nullable = false)
    private BigDecimal totalWalletBalance = BigDecimal.ZERO;

    @Nullable
    @Column(name = "theme_count")
    private Integer themeCount = 0;

    @Nullable

    @Column(name = "total_game_published")
    private Integer total_game_published = 0;  // Use Integer instead of int
    @Nullable

    @Column(name = "total_league_published")
    private Integer total_league_published = 0;
    @Nullable

    @Column(name = "total_tournament_published")
    private Integer total_tournament_published = 0;

    // Column for total participants in the game tournament league
    @Column(name = "total_participated_in_game_tournament_league")
    private int totalParticipatedInGameTournament = 0;


    @Column(name = "league_status", nullable = false)
    @Enumerated(EnumType.STRING)

    private LeagueStatus leagueStatus;

    @Column(name = "kyc_status")
    private KycStatus kycStatus = KycStatus.NOT_SUBMITTED;

    @Column(name = "fcm_token")
    private String fcmToken;


    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    @CurrentTimestamp
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedDate;

/*    @Transient
    private Boolean isFollowing;*/

    @JsonProperty("isFollowing")
    @Transient
    private Boolean isFollowing;


    public Boolean getIsFollowing() {
        return isFollowing;
    }

    public void setIsFollowing(Boolean isFollowing) {
        this.isFollowing = isFollowing;
    }


    @PreUpdate
    public void preUpdate() {
        this.updatedDate = new Date();
    }


    @PrePersist
    public void prePersist() {
        if (this.total_game_published == null) {
            this.total_game_published = 0;
        }
        if (this.total_league_published == null) {
            this.total_league_published = 0;
        }
        if (this.total_tournament_published == null) {
            this.total_tournament_published = 0;
        }
        this.setLeagueStatus(LeagueStatus.NOT_PAID);
    }


    @JsonManagedReference("submissionentity-vendor")
    @OneToOne(mappedBy = "vendorEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private VendorSubmissionEntity submissionEntity;

    // Custom getter for first_name
    public String getFirst_name() {
        if (isPrivate != null && isPrivate) {
            return "Anonymous";
        }
        return first_name;
    }

    // Custom getter for last_name
    public String getLast_name() {
        if (isPrivate != null && isPrivate) {
            return "User";
        }
        return last_name;
    }
    @Transient
    public String getName() {
        return this.first_name + " " + this.last_name;
    }



}


