package aagapp_backend.repository.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.LeagueStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    List<VendorEntity> findByLeagueStatus(LeagueStatus leagueStatus);

    List<VendorEntity> findTop3ByOrderByRefferalbalanceDesc();

    List<VendorEntity> findTop3ByOrderByTotalWalletBalanceDesc();
    List<VendorEntity> findTop3ByOrderByTotalParticipatedInGameTournamentDesc();

    // By Referral Count (new method to fetch vendors based on referral count)
    List<VendorEntity> findTop3ByOrderByReferralCountDesc();

/*    @Modifying
    @Transactional
    @Query("UPDATE VendorEntity v SET v.dailyLimit = 0, v.publishedLimit = 0 WHERE v.service_provider_id IN :vendorIds")
    void updateDailyLimitForVendors(@Param("vendorIds") List<Long> vendorIds);*/


    @Modifying
    @Transactional
    @Query("UPDATE VendorEntity v SET  v.publishedLimit = 0 WHERE v.service_provider_id IN :vendorIds")
    void updateDailyLimitForVendors(@Param("vendorIds") List<Long> vendorIds);




}
