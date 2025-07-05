package aagapp_backend.dto.game;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlayerSummaryCompactDTO {
    private Long totalMatchesPlayed;
    private BigDecimal totalWinningAmount;
}
