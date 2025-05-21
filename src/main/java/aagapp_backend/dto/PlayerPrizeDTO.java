package aagapp_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PlayerPrizeDTO {
    private String playerName;
    private int score;
    private double contributionPercentage;
    private BigDecimal prizeAmount;

    public PlayerPrizeDTO(String playerName, int score, double contributionPercentage, BigDecimal prizeAmount) {
        this.playerName = playerName;
        this.score = score;
        this.contributionPercentage = contributionPercentage;
        this.prizeAmount = prizeAmount;
    }

    // Getters
}
