package aagapp_backend.repository.game;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.game.GameRoomWinner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRoomWinnerRepository extends JpaRepository<GameRoomWinner, Long> {

//    Page<GameRoomWinner> findByGame_Id(Long gameId, Pageable pageable);
//List<GameRoomWinner> findByGame_Id(Long gameId);
Page<GameRoomWinner> findByGame_Id(Long gameId, Pageable pageable);

}
