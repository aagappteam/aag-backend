package aagapp_backend.repository.game;

import aagapp_backend.entity.game.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    GameRoom findByRoomCode(String roomCode);
    List<GameRoom> findByStatus(String status);  // e.g., "waiting" for rooms with space
}

