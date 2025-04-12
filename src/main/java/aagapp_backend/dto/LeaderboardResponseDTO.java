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
}
