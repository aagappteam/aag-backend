package aagapp_backend.repository.game;

import aagapp_backend.entity.game.GameResultRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GameResultRecordRepository extends JpaRepository<GameResultRecord, Long> {

    List<GameResultRecord> findByGame_IdAndRoomId(Long gameId, Long roomId);
    List<GameResultRecord> findByGame_Id(Long gameId);

    Page<GameResultRecord> findByGame_IdAndIsWinnerTrue(Long gameId, Pageable pageable);

    @Query("SELECT g FROM GameResultRecord g WHERE g.player.id = :playerId")
    Page<GameResultRecord> findByPlayerId(@Param("playerId") Long playerId, Pageable pageable);


    @Query("""
        SELECT g FROM GameResultRecord g 
        WHERE g.player.id = :playerId 
        AND (:gameName IS NULL OR LOWER(g.game.name) LIKE LOWER(CONCAT('%', :gameName, '%')))
        AND (:winner IS NULL OR g.isWinner = :winner)
    """)
    Page<GameResultRecord> findByPlayerIdWithFilters(
            @Param("playerId") Long playerId,
            @Param("gameName") String gameName,
            @Param("winner") Boolean winner,
            Pageable pageable
    );

    Long countByPlayer_PlayerId(Long playerId);

    @Query("SELECT COALESCE(SUM(grr.winningammount), 0) FROM GameResultRecord grr WHERE grr.player.id = :playerId AND grr.isWinner = true")
    BigDecimal getTotalWinningAmountByPlayer(Long playerId);
}
