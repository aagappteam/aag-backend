package aagapp_backend.dto.game;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeagueLeaderboardDtoAdmin {
    private Long playerId;
    private String phone;
    private String name;
    private String pictureUrl;
    private Integer score;
    private Boolean isWinner;
}
