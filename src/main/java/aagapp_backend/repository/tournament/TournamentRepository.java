package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.TournamentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long>, JpaSpecificationExecutor<Tournament> {
    Page<Tournament> findTournamentByStatusAndVendorId(TournamentStatus status, Long vendorId, Pageable pageable);

    List<Tournament> findByStatusAndScheduledAtBetween(TournamentStatus status, ZonedDateTime start, ZonedDateTime end);

    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);
    List<Tournament> findByStatus(TournamentStatus status);

    Page<Tournament> findByVendorId(Long vendorId, Pageable pageable);
    Page<Tournament> findByVendorIdAndStatusAndScheduledAtBetween(
            Long vendorId,
            TournamentStatus status,
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            Pageable pageable
    );

    Page<Tournament> findByVendorIdAndStatus(
            Long vendorId,
            TournamentStatus status,
            Pageable pageable
    );

    @Query("SELECT g FROM Tournament g WHERE g.vendorId = :vendorId AND g.scheduledAt BETWEEN :startTime AND :endTime")
    List<Tournament> findByVendorEntityAndScheduledAtBetween(
            @Param("vendorId") Long vendorId,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime
    );

    @Query("SELECT t FROM Tournament t WHERE t.status = :status AND t.scheduledAt <= :currentTime")
    List<Tournament> findByStatusAndScheduledAtGreaterThanEqual(
            @Param("status") TournamentStatus status,
            @Param("currentTime") ZonedDateTime currentTime
    );

/*    @Query("""
    SELECT t FROM Tournament t 
    WHERE t.status = :status AND (
        (t.scheduledAt BETWEEN :start AND :end)
        OR t.scheduledAt <= :currentTime
    )
""")
    List<Tournament> findTournamentsToStart(
            @Param("status") TournamentStatus status,
            @Param("start") ZonedDateTime start,
            @Param("end") ZonedDateTime end,
            @Param("currentTime") ZonedDateTime currentTime
    );*/
/*    @Query("""
        SELECT t FROM Tournament t 
        WHERE t.status = :status AND t.scheduledAt BETWEEN :start AND :end
    """)
    List<Tournament> findTournamentsToStart(
            @Param("status") TournamentStatus status,
            @Param("start") ZonedDateTime start,
            @Param("end") ZonedDateTime end
    );*/

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tournament t WHERE t.id = :id")
    Optional<Tournament> lockTournamentForProcessing(@Param("id") Long id);


/*    @Query("SELECT t FROM Tournament t WHERE t.status = :status " +
            "AND t.scheduledAt BETWEEN :start AND :end")
    List<Tournament> findTournamentsToStart(
            @Param("status") TournamentStatus status,
            @Param("start") ZonedDateTime start,
            @Param("end") ZonedDateTime end
    );*/


    @Query("SELECT t FROM Tournament t WHERE t.status = :status AND t.scheduledAt BETWEEN :windowStart AND :windowEnd")
    List<Tournament> findTournamentsToStart(
            @Param("status") TournamentStatus status,
            @Param("windowStart") ZonedDateTime windowStart,
            @Param("windowEnd") ZonedDateTime windowEnd
    );



    Page<Tournament> findByStatusIn(List<TournamentStatus> statuses, Pageable pageable);

    Page<Tournament> findByStatusInAndVendorId(List<TournamentStatus> statuses, Long vendorId, Pageable pageable);

    @Query("SELECT g FROM Tournament g WHERE g.vendorId = :vendorId AND g.status = :status ORDER BY g.createdDate DESC")
    List<Tournament> findActiveTournaments( @Param("vendorId") Long vendorId,  @Param("status") TournamentStatus status);

    @Query("SELECT t FROM Tournament t " +
            "WHERE (:statuses IS NULL OR t.status IN :statuses) " +
            "AND (:vendorId IS NULL OR t.vendorId = :vendorId) " +
            "AND (:gameName IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :gameName, '%')))")
    Page<Tournament> findByFilters(
            @Param("statuses") List<TournamentStatus> statuses,
            @Param("vendorId") Long vendorId,
            @Param("gameName") String gameName,
            Pageable pageable
    );

    @Query("SELECT t.theme.id FROM Tournament t " +
            "WHERE t.vendorId = :vendorId " +
            "AND t.existinggameId = :gameId " +
            "AND t.theme.id = :themeId " +
            "AND t.status = :status " +
            "AND t.createdDate BETWEEN :startOfDay AND :endOfDay")
    Optional<Long> findThemeIdIfPublishedToday(
            @Param("vendorId") Long vendorId,
            @Param("gameId") Long gameId,
            @Param("themeId") Long themeId,
            @Param("status") TournamentStatus status,
            @Param("startOfDay") ZonedDateTime startOfDay,
            @Param("endOfDay") ZonedDateTime endOfDay
    );

    @Query(value = "SELECT COUNT(*) FROM tournament WHERE vendorentity_service_provider_id = :vendorId", nativeQuery = true)
    Long countByVendorId(@Param("vendorId") Long vendorId);


    @Query("SELECT COUNT(t) FROM Tournament t WHERE t.vendorEntity.service_provider_id = :vendorId AND t.createdDate BETWEEN :start AND :end")
    Long countTournamentsBetweenDates(@Param("vendorId") Long vendorId,
                                      @Param("start") ZonedDateTime start,
                                      @Param("end") ZonedDateTime end);

    @Query("SELECT t FROM Tournament t WHERE t.vendorEntity.service_provider_id = :vendorId AND t.createdDate BETWEEN :start AND :end")
    List<Tournament> findTournamentsBetweenDates(@Param("vendorId") Long vendorId,
                                                 @Param("start") ZonedDateTime start,
                                                 @Param("end") ZonedDateTime end);

    @Query("SELECT g FROM Tournament g WHERE g.vendorEntity.id = :vendorId")
    List<Tournament> findByVendorId(@Param("vendorId") Long vendorId);

    boolean existsByVendorIdAndStatus(Long vendorId, TournamentStatus tournamentStatus);
    @Query("SELECT t FROM Tournament t " +
            "WHERE t.vendorId = :vendorId " +
            "AND t.scheduledAt BETWEEN :startTime AND :endTime " +
            "AND t.status IN :statuses")
    List<Tournament> findByVendorIdAndScheduledAtBetweenAndStatusIn(
            @Param("vendorId") Long vendorId,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime,
            @Param("statuses") List<TournamentStatus> statuses
    );

    @Query("SELECT t FROM Tournament t WHERE t.vendorId = :vendorId AND t.createdDate BETWEEN :startTime AND :endTime AND t.status IN :statuses")
    List<Tournament> findTournamentsByVendorAndCreatedDateAndStatusIn(
            @Param("vendorId") Long vendorId,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime,
            @Param("statuses") List<TournamentStatus> statuses
    );





    List<Tournament> findAllByStatusAndCreatedDateBefore(TournamentStatus tournamentStatus, ZonedDateTime fifteenMinutesAgo);
}
