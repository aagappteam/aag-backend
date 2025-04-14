package aagapp_backend.repository.league;


import aagapp_backend.entity.game.GameRoomWinner;
import aagapp_backend.entity.league.LeagueRoomWinner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueRoomWinnerRepository extends JpaRepository<LeagueRoomWinner, Long> {
    List<GameRoomWinner> findByGame_Id(Long gameId);
}
