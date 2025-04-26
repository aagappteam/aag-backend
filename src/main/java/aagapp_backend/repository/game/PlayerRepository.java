package aagapp_backend.repository.game;

import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.players.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findAllByGameRoom(GameRoom gameRoom);
    List<Player>findAllByLeagueRoom(LeagueRoom leagueRoom);

    @Query("SELECT COUNT(p) FROM Player p WHERE p.team.id = :teamId")
    Long countByTeamId(@Param("teamId") Long teamId);

}
