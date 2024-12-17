package aagapp_backend.repository.league;

import aagapp_backend.entity.league.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeagueRepository extends JpaRepository<League, Long> {


    Page<League> findByVendorId(Long vendorId, Pageable pageable);
//    Page<League> findByStatusAndVendorId(String status, Long vendorId, Pageable pageable);
//    Page<League> findByStatusAndVendorEntity_Id(String status, Long vendorId, Pageable pageable);

}
