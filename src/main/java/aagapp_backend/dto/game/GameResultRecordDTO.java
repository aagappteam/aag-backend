package aagapp_backend.dto.game;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameResultRecordDTO {
    private Long id;
    private Long roomId;
    private String gameName;
    private String playerName;
    private String playerProfilePic;
    private BigDecimal winningAmount;
    private Integer score;
    private String isWinner;
    private LocalDateTime playedAt;
}
