package aagapp_backend.repository.league;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.LeagueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {


//    @Query("SELECT v FROM VendorEntity v WHERE v.service_provider_id = :vendorId")
//    Optional<VendorEntity> findVendorById(@Param("vendorId") Long vendorId);

//    Page<League> findByStatus(String status, Pageable pageable);
//
//    Page<League> findByVendorId(Long vendorId, Pageable pageable);
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


//    List<League> findByVendorEntityAndScheduledAtBetween(VendorEntity vendorEntity, ZonedDateTime startTimeUTC, ZonedDateTime endTimeUTC);

    @Query("SELECT g FROM League g WHERE g.vendorEntity = :vendorEntity AND g.scheduledAt BETWEEN :startTime AND :endTime")
    List<League> findByVendorEntityAndScheduledAtBetween(
            @Param("vendorEntity") VendorEntity vendorEntity,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime
    );

}
