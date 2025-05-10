package aagapp_backend.dto.tournament;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LeaderboardResponseDTOTournament {

    private Long playerId;
    private String playerName;
    private String profilePicture;
    private Integer score;
    private Integer round;
    private Double winningammount;
}
