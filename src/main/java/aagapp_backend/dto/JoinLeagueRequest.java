package aagapp_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JoinLeagueRequest {

    @NotNull(message="Player id can not be null")
    private Long playerId;

    @NotNull(message="League id can not be null")
    private Long leagueId;

    @NotNull(message="Team id can not be null")
    private Long teamId;


}