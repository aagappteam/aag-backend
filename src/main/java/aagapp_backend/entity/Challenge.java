package aagapp_backend.entity;

import aagapp_backend.enums.ChallengeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;


@Entity
@Getter
@Setter
@Table(
        indexes = {
                @Index(name = "idx_vendorId", columnList = "vendorId"),
                @Index(name = "idx_challengeStatus", columnList = "challengeStatus"),
                @Index(name = "idx_scheduledAt", columnList = "scheduledAt"),
                @Index(name = "idx_opponentVendorId", columnList = "opponentVendorId"),
                @Index(name = "idx_existinggameId", columnList = "existinggameId"),
                @Index(name = "idx_vendor_status", columnList = "vendorId,challengeStatus"),
                @Index(name = "idx_createdAt", columnList = "createdAt"),
                @Index(name = "idx_endDate", columnList = "endDate")
        }
)
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vendorId;

    private  String vendorName;

    private String vendorProfilePic;

    private String name;

    private String gameImage;

    private Long opponentVendorId;

    private String opponentVendorName;

    private String opponentVendorProfilePic;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus challengeStatus;

    private Long existinggameId;

    private Long themeId;

    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;

    @NotNull
    private Double fee;

    private Integer move;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;

    private ZonedDateTime endDate;

    @Column(updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        // Set createdAt to the current timestamp before the entity is persisted
        this.createdAt = ZonedDateTime.now();
    }

    public enum ChallengeStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
