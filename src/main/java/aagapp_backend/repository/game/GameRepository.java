package aagapp_backend.repository.game;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.game.Game;
import aagapp_backend.enums.GameStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {
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

    @Query("SELECT g.theme.id FROM Game g WHERE g.vendorEntity.service_provider_id = :vendorId AND g.aaggameid = :gameId AND g.theme IS NOT NULL")
    List<Long> findThemeIdsByVendorAndGame(@Param("vendorId") Long vendorId, @Param("gameId") Long gameId);

    @Query("SELECT MAX(g.theme.id) FROM Game g WHERE g.vendorEntity.service_provider_id = :vendorId AND g.aaggameid = :gameId")
    Long findLastThemeIdByVendorAndGame(@Param("vendorId") Long vendorId, @Param("gameId") Long gameId);

    @Query("SELECT COUNT(DISTINCT g.theme.id) FROM Game g WHERE g.vendorEntity.service_provider_id = :vendorId")
    Long countDistinctThemeIdByVendor(@Param("vendorId") Long vendorId);

    @Query("SELECT g.theme.id FROM Game g WHERE g.vendorEntity.service_provider_id = :vendorId AND g.aaggameid = :gameId AND g.status = :status")
    List<Long> findThemeIdsByVendorAndGameAndStatus(Long vendorId, Long gameId, GameStatus status);

    @Query("SELECT g.theme.id FROM Game g " +
            "WHERE g.vendorEntity.service_provider_id = :vendorId " +
            "AND g.aaggameid = :gameId " +
            "AND g.theme.id = :themeId " +
            "AND g.status = :status " +
            "AND g.createdDate BETWEEN :startOfDay AND :endOfDay")
    Optional<Long> findThemeIdIfPublishedToday(
            @Param("vendorId") Long vendorId,
            @Param("gameId") Long gameId,
            @Param("themeId") Long themeId,
            @Param("status") GameStatus status,
            @Param("startOfDay") ZonedDateTime startOfDay,
            @Param("endOfDay") ZonedDateTime endOfDay
    );



    @Query("SELECT DISTINCT g.theme.id FROM Game g " +
            "WHERE g.vendorEntity.service_provider_id = :vendorId " +
            "AND g.aaggameid = :gameId " +
            "AND g.status IN :statuses")
    List<Long> findThemeIdsByVendorAndGameAndStatuses(
            @Param("vendorId") Long vendorId,
            @Param("gameId") Long gameId,
            @Param("statuses") List<GameStatus> statuses);



/*    @Query("SELECT g FROM Game g WHERE g.vendorEntity = :vendorEntity AND g.scheduledAt >= :startTime AND g.scheduledAt <= :endTime")
    List<Game> findByVendorEntityAndScheduledAtWithin24Hours(VendorEntity vendorEntity, ZonedDateTime startTime, ZonedDateTime endTime);*/

    @Query("SELECT COUNT(g) FROM Game g WHERE g.vendorEntity.service_provider_id = :vendorId AND g.createdDate >= :startOfWeek")
    int countGamesThisWeek(@Param("vendorId") Long vendorId, @Param("startOfWeek") ZonedDateTime startOfWeek);

    @Query(value = "SELECT COUNT(*) FROM aag_ludo_game WHERE vendor_id = :vendorId", nativeQuery = true)
    Long countByVendorId(Long vendorId);


    @Query("SELECT g FROM Game g WHERE g.vendorEntity.id = :vendorId")
    List<Game> findByVendorId(@Param("vendorId") Long vendorId);

}

