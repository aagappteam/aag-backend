package aagapp_backend.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LeagueMatchProcess {

    private Long leagueId;
    private Long roomId;
    private List<LeaguePlayerDtoWinner> players;
}
