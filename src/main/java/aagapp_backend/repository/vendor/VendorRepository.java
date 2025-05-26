package aagapp_backend.repository.vendor;

import aagapp_backend.dto.TopVendorDto;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.LeagueStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


    @Query("SELECT new aagapp_backend.dto.TopVendorDto(v.service_provider_id, v.first_name, COUNT(f.id)) " +
            "FROM VendorEntity v LEFT JOIN UserVendorFollow f ON f.vendor.service_provider_id = v.service_provider_id " +
            "GROUP BY v.service_provider_id, v.first_name " +
            "ORDER BY COUNT(f.id) DESC")
    List<TopVendorDto> findTopVendorsWithFollowerCount(Pageable pageable);


    @Query("SELECT v FROM VendorEntity v WHERE v.service_provider_id = :influencerId")

    VendorEntity findByServiceProviderId(@Param("influencerId") Long influencerId);
}
