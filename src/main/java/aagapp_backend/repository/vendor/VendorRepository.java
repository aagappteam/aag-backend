package aagapp_backend.repository.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    List<VendorEntity> findByLeagueStatus(LeagueStatus leagueStatus);

    @EntityGraph(attributePaths = {"profilePic", "firstName", "lastName"})
    List<VendorEntity> findTop3ByOrderByWalletBalanceDesc();


//    List<League> findByChallengedVendors_Id(Long vendorId);

}
