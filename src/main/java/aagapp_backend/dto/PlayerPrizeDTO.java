package aagapp_backend.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlayerPrizeDTO {
    private String playerName;
    private int score;
    private double contributionPercentage;
    private double prizeAmount;

    public PlayerPrizeDTO(String playerName, int score, double contributionPercentage, double prizeAmount) {
        this.playerName = playerName;
        this.score = score;
        this.contributionPercentage = contributionPercentage;
        this.prizeAmount = prizeAmount;
    }

    // Getters
}
