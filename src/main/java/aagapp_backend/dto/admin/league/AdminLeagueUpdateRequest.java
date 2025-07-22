package aagapp_backend.dto.admin.league;

import aagapp_backend.enums.LeagueStatus;
import lombok.Data;

@Data
public class AdminLeagueUpdateRequest {
    private Long leagueId;
    private Long vendorId;
    private Long challengerId;
    private LeagueStatus status; // APPROVED or REJECTED
    private String message;
}

