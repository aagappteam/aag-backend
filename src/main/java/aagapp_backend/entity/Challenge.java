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
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vendorId;

    private Long opponentVendorId;

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

    public enum ChallengeStatus {
        PENDING, ACCEPTED, REJECTED
    }
}
