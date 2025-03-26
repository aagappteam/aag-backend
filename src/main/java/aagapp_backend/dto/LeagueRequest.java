package aagapp_backend.dto;

import aagapp_backend.entity.VendorEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class LeagueRequest {

    @NotNull(message = "League name cannot be null")
    private String name;

    private String description;

    private String challengingVendorTeamName;

    @NotNull(message = "Fee cannot be null")
    private Double fee;

    private Integer move;

    private Boolean isChallenge;


    @NotNull(message = "Theme id cannot be null")
    private Long themeId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;

    @NotNull(message = "League type must be specified")
    private String leagueType; // e.g., "Knockout", "Round Robin"

    private Integer maxPlayersPerTeam;

    private Integer minPlayersPerTeam;

    private Long opponentVendorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime registrationDeadline;

    private List<Long> teamIds;
}
