package aagapp_backend.repository.team;

import aagapp_backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByLeagueId(Long leagueId);

    List<Team> findByNameContainingIgnoreCase(String name);


    @Query("SELECT t FROM Team t WHERE t.id = :id")
    Team getTeamById(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Team t WHERE t.id = :id")
    void deleteTeam(@Param("id") Long id);
}
