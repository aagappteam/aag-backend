package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    Page<Tournament> findTournamentByStatusAndVendorId(TournamentStatus status, Long vendorId, Pageable pageable);

    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);

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

}
