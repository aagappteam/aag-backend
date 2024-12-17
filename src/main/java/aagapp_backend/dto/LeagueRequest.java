package aagapp_backend.dto;

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

    @NotNull(message = "Entry fee cannot be null")
    private Double entryFee;

    private Long themeId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;

    @NotNull(message = "League type must be specified")
    private String leagueType; // e.g., "Knockout", "Round Robin"

    private Integer maxPlayersPerTeam;

    private Integer minPlayersPerTeam;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime registrationDeadline;

    private List<Long> teamIds;
}
