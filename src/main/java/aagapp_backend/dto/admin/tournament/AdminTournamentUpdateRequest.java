package aagapp_backend.dto.admin.tournament;

import aagapp_backend.enums.TournamentStatus;
import lombok.Data;

@Data
public class AdminTournamentUpdateRequest {
    private Long tournamentId;
    private Long vendorId;
    private TournamentStatus status;
    private String message;
}

