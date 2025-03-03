package aagapp_backend.repository.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    VendorEntity findServiceProviderByReferralCode(String referralCode);
    List<VendorEntity> findByLeagueStatus(LeagueStatus leagueStatus);
//    List<League> findByChallengedVendors_Id(Long vendorId);

}
