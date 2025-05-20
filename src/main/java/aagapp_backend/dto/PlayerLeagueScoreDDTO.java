package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlayerLeagueScoreDDTO {
    private String name;
    private String playerProfilePic;
    private boolean currentUser;
    private int score;
    private int retries;
    private boolean winner;
    private Double prizeAmount;

    // Constructor
    public PlayerLeagueScoreDDTO(String name, String playerProfilePic, boolean currentUser,
                                int score, int retries, boolean winner, Double prizeAmount) {
        this.name = name;
        this.playerProfilePic = playerProfilePic;
        this.currentUser = currentUser;
        this.score = score;
        this.retries = retries;
        this.winner = winner;
        this.prizeAmount = prizeAmount;
    }

    // Getters
    public String getName() { return name; }
    public int getScore() { return score; }
}
