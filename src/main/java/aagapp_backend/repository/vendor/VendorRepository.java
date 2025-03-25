package aagapp_backend.repository.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    List<VendorEntity> findByLeagueStatus(LeagueStatus leagueStatus);

/*
    @EntityGraph(attributePaths = {"profilePic", "first_name", "last_name"})
*/
    List<VendorEntity> findTop3ByOrderByWalletBalanceDesc();

/*    @Query("SELECT v FROM VendorEntity v WHERE v.id = :vendorId OR v.id IN "
            + "(SELECT t.vendorId FROM TopVendorCache t ORDER BY t.rank ASC LIMIT 3)")
    List<VendorEntity> findVendorAndTopInvites(@Param("vendorId") Long vendorId);*/



//    List<League> findByChallengedVendors_Id(Long vendorId);

}
