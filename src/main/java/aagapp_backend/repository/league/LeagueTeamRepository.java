package aagapp_backend.repository.league;


import aagapp_backend.entity.league.League;
import aagapp_backend.entity.team.LeagueTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, Long> {
    List<LeagueTeam> findByLeague(League savedLeague);

    List<LeagueTeam> findByLeagueId(Long leagueId);

    @Query("SELECT t FROM LeagueTeam t WHERE t.teamName = :teamName AND t.league.id = :leagueId")
    Optional<LeagueTeam> findByTeamNameAndLeagueId(@Param("teamName") String teamName, @Param("leagueId") Long leagueId);

}
