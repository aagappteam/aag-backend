package aagapp_backend.repository.aagavailblegames;

import aagapp_backend.entity.game.AagAvailableGames;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AagAvailbleGamesRepository extends JpaRepository<AagAvailableGames, Long> {
}
