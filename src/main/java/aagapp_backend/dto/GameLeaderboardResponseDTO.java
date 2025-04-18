package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameLeaderboardResponseDTO {
    private String gameName;
    private Double gameFee;
    private String gameIcon;
    private String themeName;
    private Integer totalPlayers;
    private List<LeaderboardResponseDTO> players;
    private int currentPage;
    private int totalPages;
    private long totalItems;

}
