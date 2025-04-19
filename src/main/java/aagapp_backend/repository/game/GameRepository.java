package aagapp_backend.repository.game;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.game.Game;
import aagapp_backend.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Page<Game> findAll(Pageable pageable);
    @Query("SELECT g FROM Game g WHERE g.vendorEntity = :vendorEntity AND g.scheduledAt BETWEEN :startTime AND :endTime")
    List<Game> findByVendorEntityAndScheduledAtBetween(
            @Param("vendorEntity") VendorEntity vendorEntity,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime
    );
    Page<Game> findByFeeBetween(int min, int max, Pageable pageable);
    Page<Game> findByFeeGreaterThanEqual(int fee, Pageable pageable);
    Page<Game> findByVendorEntityIsNotNull(Pageable pageable);

/*    @Query("SELECT g FROM Game g WHERE g.vendorEntity = :vendorEntity AND g.scheduledAt >= :startTime AND g.scheduledAt <= :endTime")
    List<Game> findByVendorEntityAndScheduledAtWithin24Hours(VendorEntity vendorEntity, ZonedDateTime startTime, ZonedDateTime endTime);*/


}

