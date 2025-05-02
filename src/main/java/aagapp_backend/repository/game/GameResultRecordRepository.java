package aagapp_backend.repository.game;

import aagapp_backend.entity.game.GameResultRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameResultRecordRepository extends JpaRepository<GameResultRecord, Long> {

    List<GameResultRecord> findByGame_IdAndRoomId(Long gameId, Long roomId);
    List<GameResultRecord> findByGame_Id(Long gameId);

    Page<GameResultRecord> findByGame_IdAndIsWinnerTrue(Long gameId, Pageable pageable);
}
