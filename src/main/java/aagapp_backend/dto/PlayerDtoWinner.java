package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class PlayerDtoWinner {
    private Long playerId;
    private int score;
    private String teamName;

}
