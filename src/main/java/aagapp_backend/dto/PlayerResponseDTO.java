package aagapp_backend.dto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class PlayerResponseDTO {
    private Long playerId;
    private String playerName;
    private String profileUrl;
    private int score;  // Add the score field

    // Constructor, Getters, Setters
    public PlayerResponseDTO(Long playerId, String playerName, String profileUrl, int score) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.profileUrl = profileUrl;
        this.score = score;
    }
}

