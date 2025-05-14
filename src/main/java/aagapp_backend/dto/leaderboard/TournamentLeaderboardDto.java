package aagapp_backend.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class TournamentLeaderboardDto {
    private Long playerId;
    private String name;
    private String profilePic;
    private int totalScore;
    private Boolean isWinner;
    private BigDecimal winAmount;
    private int round;
    private Long roomId;
}
