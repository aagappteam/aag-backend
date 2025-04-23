package aagapp_backend.repository.league;


import aagapp_backend.entity.league.League;
import aagapp_backend.entity.team.LeagueTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, Long> {
    List<LeagueTeam> findByLeague(League savedLeague);

    List<LeagueTeam> findByLeagueId(Long leagueId);
}
