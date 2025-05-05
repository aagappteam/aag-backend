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
public class TeamDetailsDTO {

    private String teamName;
    private String profilePic;
    private Integer totalScore;
    private List<PlayerLeagueScoreDTO> players;
}
