package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeagueTeamDetailsResponse {

    private String leagueName;
    private String winningTeamName;
    private List<TeamDetailsDTO> teams;
}
