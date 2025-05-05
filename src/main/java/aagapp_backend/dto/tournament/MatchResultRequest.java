package aagapp_backend.dto.tournament;

import lombok.Data;

@Data
public class MatchResultRequest {
    private Long tournamentRoomId;
    private Long winnerPlayerId;
}

