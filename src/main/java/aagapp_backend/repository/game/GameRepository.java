package aagapp_backend.repository.game;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.game.Game;
import aagapp_backend.enums.GameStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
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

    @Query("SELECT g FROM Game g WHERE g.vendorEntity = :vendorEntity AND  g.status = :status  ORDER BY g.createdDate DESC")
    List<Game> findActiveGames(
            @Param("vendorEntity") VendorEntity vendorEntity,
            @Param("status") GameStatus status
    );


 /*   @Query("SELECT g FROM Game g WHERE g.vendorEntity.service_provider_id = :vendorId")
    List<Game> findByVendorId(@Param("vendorId") Long vendorId, Pageable pageable);*/

    // Find games by ACTIVE status and endDate after the current date
/*    @Query("SELECT g FROM Game g WHERE g.status = :status AND g.endDate > :endDate")
    Page<Game> findAllByStatusAndEndDateAfter(
            @Param("status") GameStatus status,
            @Param("endDate") ZonedDateTime endDate,
            Pageable pageable
    );*/

    @Query("SELECT g FROM Game g WHERE g.status = :status AND g.endDate > :endDate ORDER BY g.createdDate DESC")
    Page<Game> findAllByStatusAndEndDateAfter(
            @Param("status") GameStatus status,
            @Param("endDate") ZonedDateTime endDate,
            Pageable pageable
    );



    @Query("SELECT g FROM Game g WHERE g.vendorEntity.id = :vendorId AND g.status = :status AND g.endDate > :endDate")
    List<Game> findByVendorIdAndStatusAndEndDateAfter(
            @Param("vendorId") Long vendorId,
            @Param("status") GameStatus status,
            @Param("endDate") ZonedDateTime endDate,
            Pageable pageable
    );

/*    @Query("SELECT g FROM Game g WHERE g.vendorEntity = :vendorEntity AND g.scheduledAt >= :startTime AND g.scheduledAt <= :endTime")
    List<Game> findByVendorEntityAndScheduledAtWithin24Hours(VendorEntity vendorEntity, ZonedDateTime startTime, ZonedDateTime endTime);*/


}

