package aagapp_backend.repository.game;

import aagapp_backend.entity.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    int countGamesByVendorIdAndDate(Long vendorId, LocalDate date);

    int countGamesByVendorIdAndWeek(Long vendorId);

    List<Game> findActiveGamesByVendorId(Long vendorId);

    List<Game> findScheduledGamesByVendorId(Long vendorId);
}

