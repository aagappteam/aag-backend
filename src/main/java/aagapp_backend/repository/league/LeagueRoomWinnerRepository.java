package aagapp_backend.repository.league;

import aagapp_backend.entity.league.LeagueRoomWinner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueRoomWinnerRepository extends JpaRepository<LeagueRoomWinner, Long> {
    @Query("SELECT lrw FROM LeagueRoomWinner lrw WHERE lrw.leagueRoom.id = :leagueRoomId")
    Page<LeagueRoomWinner> findByLeagueRoomId(@Param("leagueRoomId") Long leagueRoomId, Pageable pageable);



}
