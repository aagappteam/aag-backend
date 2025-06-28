package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResultRecordDTO {

    private Long id;
    private Long roomId;
    private String tournamentName;
    private String playerName;
    private String playerProfilePic;
    private Integer score;
    private BigDecimal amount;
    private String isWinner;
    private Integer round;
    private LocalDateTime playedAt;
}
