package aagapp_backend.repository.game;
import aagapp_backend.enums.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.GameRoomStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {

    GameRoom findByRoomCode(String roomCode);

    List<GameRoom> findByStatus(GameRoomStatus status);

    List findByGameAndStatus(Game game, GameRoomStatus gameRoomStatus);
    Optional<GameRoom> findByCurrentPlayersContains(Player player);
    List<GameRoom> findByGame_Id(Long gameId, Pageable pageable);

    @Query("SELECT SUM(g.maxPlayers) FROM GameRoom g WHERE g.game.id = :gameId")
    Long sumMaxPlayersByGameId(@Param("gameId") Long gameId);

/*    @Query("SELECT gr.game, COUNT(gr.id) FROM GameRoom gr WHERE gr.status IN ('ONGOING', 'INITIALIZED') GROUP BY gr.game ORDER BY COUNT(gr.id) DESC")
    List<Object[]> findPopularGames(Pageable pageable);*/
@Query("""
    SELECT gr.game, COUNT(p.id)
    FROM Player p
    JOIN p.gameRoom gr
    WHERE gr.status IN (:statuses)
      AND gr.game.status = :gameStatus
      AND gr.game.endDate > :currentTime
    GROUP BY gr.game
    ORDER BY COUNT(p.id) DESC
""")
List<Object[]> findPopularGames(
        @Param("statuses") List<GameRoomStatus> statuses,
        @Param("gameStatus") GameStatus gameStatus,
        @Param("currentTime") ZonedDateTime currentTime,
        Pageable pageable
);


}

