package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LeaderboardResponseDTO {

    private Long playerId;
    private String playerName;
    private String profilePicture;
    private Integer score;
    private String gameName;
    private Double gameFee;
    private String gameIcon;
    private String themeName;
    private Long totalPlayers;  // Total players in the game
}

