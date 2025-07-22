package aagapp_backend.dto.admin.tournament;

import aagapp_backend.enums.TournamentStatus;
import lombok.Data;

@Data
public class AdminTournamentUpdateRequest {
    private Long tournamentId;
    private TournamentStatus status; // APPROVED or REJECTED
    private String message; // Optional feedback
}

