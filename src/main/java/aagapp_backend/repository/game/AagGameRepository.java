package aagapp_backend.repository.game;
import aagapp_backend.entity.game.AagAvailableGames;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AagGameRepository extends JpaRepository<AagAvailableGames,Long>{
    Optional<AagAvailableGames> findByGameName(String gameName);

}
