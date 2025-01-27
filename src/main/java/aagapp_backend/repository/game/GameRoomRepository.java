package aagapp_backend.repository.game;

import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.GameRoomStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {


    GameRoom findByRoomCode(String roomCode);

    List<GameRoom> findByStatus(GameRoomStatus status);

    List findByGameAndStatus(Game game, GameRoomStatus gameRoomStatus);

    Optional<Object> findByPlayersContains(Player player);
}

