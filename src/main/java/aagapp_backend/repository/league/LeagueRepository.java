package aagapp_backend.repository.league;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.LeagueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long>, JpaSpecificationExecutor<League> {


//    @Query("SELECT v FROM VendorEntity v WHERE v.service_provider_id = :vendorId")
//    Optional<VendorEntity> findVendorById(@Param("vendorId") Long vendorId);

//    Page<League> findByStatus(String status, Pageable pageable);
//

    @Query(value = "SELECT COUNT(*) FROM aag_league WHERE vendor_id = :vendorId", nativeQuery = true)
    Long countByVendorId(Long vendorId);
//    Page<League> findByStatusAndVendorId(String status, Long vendorId, Pageable pageable);
//    Page<League> findLeaguesByVendorIdAndStatus(Long vendorId, LeagueStatus status, Pageable pageable);
//
//    Page<League> findByVendorIdAndId(Long vendorId, Long leagueId, Pageable pageable);

//    Page<League> findAll(Pageable pageable);

    List<League> findByStatus(LeagueStatus leagueStatus);

    @Query("SELECT l FROM League l WHERE l.status = :status AND (" +
            "l.vendorEntity.service_provider_id = :vendorId OR l.opponentVendorId = :vendorId)")
    Page<League> findLeaguesByStatusAndVendorId(@Param("status") LeagueStatus status,
                                                @Param("vendorId") Long vendorId,
                                                Pageable pageable);


    // Find leagues by status with pagination
    Page<League> findByStatus(LeagueStatus status, Pageable pageable);

    // Find leagues by vendorId with pagination
    @Query("SELECT l FROM League l WHERE l.vendorEntity.service_provider_id = :serviceProviderId")
    Page<League> findByVendorServiceProviderId(@Param("serviceProviderId") Long serviceProviderId, Pageable pageable);

    Page<League> findAll(Pageable pageable);
    @Query("SELECT l FROM League l " +
            "WHERE (:status IS NULL OR l.status = :status) " +
            "AND (:vendorId IS NULL OR l.vendorEntity.service_provider_id = :vendorId OR l.opponentVendorId = :vendorId) " +
            "AND (:gameName IS NULL OR LOWER(l.gameName) LIKE LOWER(CONCAT('%', :gameName, '%')))")
    Page<League> findLeaguesByFilters(
            @Param("status") LeagueStatus status,
            @Param("vendorId") Long vendorId,
            @Param("gameName") String gameName,
            Pageable pageable
    );

//    List<League> findByVendorEntityAndScheduledAtBetween(VendorEntity vendorEntity, ZonedDateTime startTimeUTC, ZonedDateTime endTimeUTC);

    @Query("SELECT g FROM League g WHERE g.vendorEntity = :vendorEntity AND g.scheduledAt BETWEEN :startTime AND :endTime")
    List<League> findByVendorEntityAndScheduledAtBetween(
            @Param("vendorEntity") VendorEntity vendorEntity,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime
    );

    @Query("SELECT l FROM League l WHERE l.status = :status AND l.vendorEntity.service_provider_id = :vendorId Order By l.createdDate DESC")
    List<League> findActiveLeagues(
            @Param("vendorId") Long vendorId,
            @Param("status") LeagueStatus status
    );

    @Query("""
    SELECT l.theme.id FROM League l
    WHERE l.vendorEntity.service_provider_id = :vendorId
      AND l.aagGameId = :gameId
      AND l.theme.id = :themeId
      AND l.status = :status
      AND l.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    Optional<Long> findThemeIdIfPublishedToday(
            @Param("vendorId") Long vendorId,
            @Param("gameId") Long gameId,
            @Param("themeId") Long themeId,
            @Param("status") LeagueStatus status,
            @Param("startOfDay") ZonedDateTime startOfDay,
            @Param("endOfDay") ZonedDateTime endOfDay
    );

//    Long countByVendorEntity_Service_provider_id(Long vendorId);

    @Query("SELECT COUNT(l) FROM League l WHERE l.vendorEntity.service_provider_id = :vendorId AND l.createdDate BETWEEN :start AND :end")
    Long countLeaguesBetweenDates(@Param("vendorId") Long vendorId,
                                  @Param("start") ZonedDateTime start,
                                  @Param("end") ZonedDateTime end);

    @Query("SELECT l FROM League l WHERE l.vendorEntity.service_provider_id = :vendorId AND l.createdDate BETWEEN :start AND :end")
    List<League> findLeaguesBetweenDates(@Param("vendorId") Long vendorId,
                                         @Param("start") ZonedDateTime start,
                                         @Param("end") ZonedDateTime end);

    @Query("SELECT g FROM League g WHERE g.vendorEntity.id = :vendorId")
    List<League> findByVendorId(@Param("vendorId") Long vendorId);
}
