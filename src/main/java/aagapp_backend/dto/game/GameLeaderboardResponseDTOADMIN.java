package aagapp_backend.dto.game;

import aagapp_backend.dto.LeaderboardResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameLeaderboardResponseDTOADMIN {
    private String gameName;
    private Double gameFee;
    private String gameIcon;
    private String themeName;
    private String vendorname;
    private Integer totalPlayers;
    private List<LeaderboardResponseDTOAdmin> players;
    private int currentPage;
    private int totalPages;
    private long totalItems;

}
