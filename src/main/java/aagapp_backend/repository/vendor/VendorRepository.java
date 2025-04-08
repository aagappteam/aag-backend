package aagapp_backend.repository.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.LeagueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    List<VendorEntity> findByLeagueStatus(LeagueStatus leagueStatus);

    List<VendorEntity> findTop3ByOrderByRefferalbalanceDesc();

    List<VendorEntity> findTop3ByOrderByTotalWalletBalanceDesc();
    List<VendorEntity> findTop3ByOrderByTotalParticipatedInGameTournamentDesc();

    // By Referral Count (new method to fetch vendors based on referral count)
    List<VendorEntity> findTop3ByOrderByReferralCountDesc();



}
