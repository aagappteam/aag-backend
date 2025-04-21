package aagapp_backend.repository.game;

import aagapp_backend.entity.game.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MoveRepository extends JpaRepository<Move, Long> {
    List<Move> findByGameId(Long gameId);
}
