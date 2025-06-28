package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResultRecordDTO {

    private Long id;
    private Long roomId;
    private String leagueName;
    private String playerName;
    private String playerProfilePic;
    private Integer totalScore;
    private String isWinner;
    private String teamName;
    private LocalDateTime playedAt;
}
