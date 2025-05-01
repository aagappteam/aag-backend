package aagapp_backend.dto.tournament;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TournamentJoinRequest {

    @NotNull(message="Player id can not be null")
    private Long playerId;

    @NotNull(message="tournamentId  can not be null")
    private Long gameid;

}
