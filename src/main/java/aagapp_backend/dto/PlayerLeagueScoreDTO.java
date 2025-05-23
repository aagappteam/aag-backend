package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLeagueScoreDTO {
    private String name;
    private String playerProfilePic;
    private boolean isCurrentUser;
    private Integer score;
    private Integer retries;
    private boolean isWinner;
}
