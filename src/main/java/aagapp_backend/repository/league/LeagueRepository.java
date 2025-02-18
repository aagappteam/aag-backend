package aagapp_backend.repository.league;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {


    @Query("SELECT v FROM VendorEntity v WHERE v.service_provider_id = :vendorId")
    Optional<VendorEntity> findVendorById(@Param("vendorId") Long vendorId);

    Page<League> findByStatus(String status, Pageable pageable);

    Page<League> findByVendorId(Long vendorId, Pageable pageable);
    Page<League> findByStatusAndVendorId(String status, Long vendorId, Pageable pageable);
    Page<League> findLeaguesByVendorIdAndStatus(Long vendorId, LeagueStatus status, Pageable pageable);

    Page<League> findByVendorIdAndId(Long vendorId, Long leagueId, Pageable pageable);

    Page<League> findAll(Pageable pageable);

}
