package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
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

    @Query("SELECT t FROM Tournament t WHERE t.status = :status AND t.scheduledAt >= :currentTime")
    List<Tournament> findByStatusAndScheduledAtGreaterThanEqual(
            @Param("status") TournamentStatus status,
            @Param("currentTime") ZonedDateTime currentTime
    );

}
