package aagapp_backend.dto.game;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaderboardResponseDTOAdmin {

    private Long playerId;
    private String playerName;
    private String mobileNumber;
    private String state;
    private String profilePicture;
    private Integer score;
    private Double winningammount;

}

