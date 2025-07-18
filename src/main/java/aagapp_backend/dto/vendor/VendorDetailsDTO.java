package aagapp_backend.dto.vendor;

import aagapp_backend.dto.PaymentDTO;
import aagapp_backend.entity.VendorSubmissionEntity;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class VendorDetailsDTO {
    private Long service_provider_id;
    private String user_name;
    private String first_name;
    private String last_name;
    private String profilePic;
    private String bannerPicture;
    private String country_code;
    private String state;
    private String mobileNumber;
    private String otp;
    private Integer role;
    private String primary_email;
    private List<PaymentDTO> payments;
    private String password;
    private Integer signedUp;
    private Integer followercount;
    private Integer isVerified;
    private Boolean isPaid;
    private String token;
    private String status;
    private Boolean isPrivate;
    private Boolean isPaused;
    private String pauseReason;
    private Boolean smsPermission;
    private Boolean whatsappPermission;
    private String referralCode;
    private Integer dailyLimit;
    private Integer publishedLimit;
    private Object wallet;
    private Integer referralCount;
    private String vendorLevelPlan;
    private Double refferalbalance;
    private Double totalWalletBalance;
    private Integer themeCount;
    private Integer total_game_published;
    private Integer total_league_published;
    private Integer total_tournament_published;
    private Integer totalParticipatedInGameTournament;
    private String leagueStatus;
    private String kycStatus;
    private String fcmToken;
    private String planName;
    private String createdDate;
    private String updatedDate;
    private String lastActiveAt;
    private VendorSubmissionEntity submissionEntity;
    private String name;
    private Boolean isFollowing;
}
