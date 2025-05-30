package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseAdmin {
    private long usersInActiveTournaments;
    private long usersInActiveGames;
    private long usersInActiveLeagues;
    private long usersInCompletedTournaments;
    private long usersInCompletedGames;
    private long usersInCompletedLeagues;
}
