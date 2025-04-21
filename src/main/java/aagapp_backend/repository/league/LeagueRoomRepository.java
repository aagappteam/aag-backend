package aagapp_backend.repository.league;


import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.enums.GameRoomStatus;
import aagapp_backend.enums.LeagueRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface LeagueRoomRepository extends JpaRepository<LeagueRoom, Long>  {

    LeagueRoom findByRoomCode(String roomCode);
    List<LeagueRoom> findByLeagueAndStatus(League league, LeagueRoomStatus status);


    List<LeagueRoom> findByStatus(LeagueRoomStatus gameRoomStatus);

    @Query("SELECT SUM(lr.maxPlayers) FROM LeagueRoom lr WHERE lr.league.id = :leagueId")
    Long sumMaxPlayersByLeagueId(@Param("leagueId") Long leagueId);


}
