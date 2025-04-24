package aagapp_backend.repository.league;

import aagapp_backend.entity.game.GameResultRecord;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueResultRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueResultRecordRepository extends JpaRepository<LeagueResultRecord, Long> {

    List<LeagueResultRecord> findByLeague_IdAndRoomId(Long leagueId, Long roomId);

    List<LeagueResultRecord> findByLeague_Id(Long leagueId);

    // Goal 1: Player's total score + team in a league
    @Query("SELECT rr.leagueTeam.teamName, SUM(rr.totalScore) " +
            "FROM LeagueResultRecord rr " +
            "WHERE rr.league.id = :leagueId AND rr.player.id = :playerId " +
            "GROUP BY rr.leagueTeam.teamName")
    Object[] getPlayerTeamAndScoreInLeague(@Param("leagueId") Long leagueId, @Param("playerId") Long playerId);

    // Goal 2: All teams’ total score in a league
    @Query("SELECT rr.leagueTeam.teamName, SUM(rr.totalScore) " +
            "FROM LeagueResultRecord rr " +
            "WHERE rr.league.id = :leagueId AND rr.leagueTeam IS NOT NULL " +
            "GROUP BY rr.leagueTeam.teamName")
    List<Object[]> getTeamTotalScoresByLeague(@Param("leagueId") Long leagueId);
}
