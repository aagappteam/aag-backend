package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardDto {
    private Long playerId;
    private String name;
    private String pictureUrl;
    private Integer score;
    private Boolean isWinner;
    private Double winningAmount;
}

