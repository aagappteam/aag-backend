package aagapp_backend.dto.game;


import aagapp_backend.dto.LeaderboardResponseDTO;
import aagapp_backend.dto.tournament.LeaderboardResponseDTOTournament;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponseDTOTournamentAdmin {

    private String gameName;
    private Double gameFee;
    private String gameIcon;
    private String themeName;
    private Integer totalPlayers;
    private String totalprizepool;
    private String roundprize;

    private List<GameLeaderboardResponseDTOTornamentAdmin> players;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}

