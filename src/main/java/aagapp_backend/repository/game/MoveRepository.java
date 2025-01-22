package aagapp_backend.repository.game;

import aagapp_backend.entity.game.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Long> {
    List<Move> findByGameId(Long gameId);
}
