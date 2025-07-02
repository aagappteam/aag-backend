package aagapp_backend.dto.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameLeaderboardResponseDTOTornamentAdmin {

    private Long playerId;
    private String mobileNumber;
    private String state;

    private String playerName;
    private String profilePicture;
    private Integer score;
    private Integer round;
    private Double winningammount;

}
