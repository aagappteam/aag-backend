package aagapp_backend.repository.game;

import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.game.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    GameSession findByGameRoom(GameRoom gameRoom);
}
