package aagapp_backend.dto.admin.league;

import aagapp_backend.annotation.ValidAdminLeagueUpdate;
import aagapp_backend.enums.LeagueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ValidAdminLeagueUpdate
public class AdminLeagueUpdateRequest {

    @NotNull(message = "League id can not be null")
    private Long leagueId;

    private Integer prizePool;

    @NotNull(message = "Status can not be null")
    private LeagueStatus status; // APPROVED or REJECTED

    private String message;
}

