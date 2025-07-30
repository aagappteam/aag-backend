package aagapp_backend.repository.vendor;

import aagapp_backend.dto.TopHostWeekDto;
import aagapp_backend.dto.TopVendorDto;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.enums.VendorStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    //    List<VendorEntity> findByLeagueStatus(LeagueStatus leagueStatus);
    List<VendorEntity> findByLeagueStatusAndStatus(LeagueStatus leagueStatus, VendorStatus status);


    @Query("SELECT v FROM VendorEntity v WHERE v.leagueStatus = :leagueStatus AND v.status = :status AND v.lastActiveAt >= :lastActiveCutoff")
    List<VendorEntity> findActiveAvailableVendorsWithRecentActivity(
            @Param("leagueStatus") LeagueStatus leagueStatus,
            @Param("status") VendorStatus status,
            @Param("lastActiveCutoff") Date lastActiveCutoff
    );


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

    @Query("SELECT COUNT(v) FROM VendorEntity v WHERE v.lastActiveAt >= :activeSince")
    Long countActiveVendors(@Param("activeSince") Date activeSince);

    @Query("SELECT COUNT(v) FROM VendorEntity v WHERE v.lastActiveAt < :activeSince OR v.lastActiveAt IS NULL")
    Long countInactiveVendors(@Param("activeSince") Date activeSince);

    @Query(value = """
    SELECT new aagapp_backend.dto.TopHostWeekDto(
        v.id,
        CONCAT(v.first_name, ' ', v.last_name),
        (COALESCE(g.gameCount, 0) + COALESCE(l.leagueCount, 0) + COALESCE(t.tournamentCount, 0)),
        v.primary_email,
        v.profilePic,
        v.user_name
    )
    FROM VendorEntity v
    LEFT JOIN (
        SELECT g.vendorEntity.id AS vendorId, COUNT(g) AS gameCount
        FROM Game g
        WHERE g.createdDate BETWEEN :startOfWeek AND :endOfWeek
        GROUP BY g.vendorEntity.id
    ) g ON v.id = g.vendorId
    LEFT JOIN (
        SELECT l.vendorEntity.id AS vendorId, COUNT(l) AS leagueCount
        FROM League l
        WHERE l.createdDate BETWEEN :startOfWeek AND :endOfWeek
        GROUP BY l.vendorEntity.id
    ) l ON v.id = l.vendorId
    LEFT JOIN (
        SELECT t.vendorEntity.id AS vendorId, COUNT(t) AS tournamentCount
        FROM Tournament t
        WHERE t.createdDate BETWEEN :startOfWeek AND :endOfWeek
        GROUP BY t.vendorEntity.id
    ) t ON v.id = t.vendorId
    WHERE (COALESCE(g.gameCount, 0) + COALESCE(l.leagueCount, 0) + COALESCE(t.tournamentCount, 0)) > 0
    ORDER BY (COALESCE(g.gameCount, 0) + COALESCE(l.leagueCount, 0) + COALESCE(t.tournamentCount, 0)) DESC
    """)
    List<TopHostWeekDto> findTopHostsThisWeek(@Param("startOfWeek") ZonedDateTime startOfWeek,
                                              @Param("endOfWeek") ZonedDateTime endOfWeek,
                                              Pageable pageable);

/*    @Query(value = """
        SELECT new aagapp_backend.dto.TopHostWeekDto(
            v.id,
            CONCAT(v.first_name, ' ', v.last_name),
            (COALESCE(g.gameCount, 0) + COALESCE(l.leagueCount, 0) + COALESCE(t.tournamentCount, 0)),
            v.primary_email,
            v.profilePic,
            v.user_name
        )
        FROM VendorEntity v
        LEFT JOIN (
            SELECT g.vendorEntity.id AS vendorId, COUNT(g) AS gameCount
            FROM Game g
            WHERE g.createdDate BETWEEN :startOfWeek AND :endOfWeek
            GROUP BY g.vendorEntity.id
        ) g ON v.id = g.vendorId
        LEFT JOIN (
            SELECT l.vendorEntity.id AS vendorId, COUNT(l) AS leagueCount
            FROM League l
            WHERE l.createdDate BETWEEN :startOfWeek AND :endOfWeek
            GROUP BY l.vendorEntity.id
        ) l ON v.id = l.vendorId
        LEFT JOIN (
            SELECT t.vendorEntity.id AS vendorId, COUNT(t) AS tournamentCount
            FROM Tournament t
            WHERE t.createdDate BETWEEN :startOfWeek AND :endOfWeek
            GROUP BY t.vendorEntity.id
        ) t ON v.id = t.vendorId
        WHERE (COALESCE(g.gameCount, 0) + COALESCE(l.leagueCount, 0) + COALESCE(t.tournamentCount, 0)) > 0
        ORDER BY (COALESCE(g.gameCount, 0) + COALESCE(l.leagueCount, 0) + COALESCE(t.tournamentCount, 0)) DESC
        """)
    List<TopHostWeekDto> findTopHostsThisWeek(@Param("startOfWeek") ZonedDateTime startOfWeek,
                                              @Param("endOfWeek") ZonedDateTime endOfWeek);*/

    Optional<Object> findByReferralCode(String referralCode);

    Optional<VendorEntity> findByMobileNumber(String mobileNumber);

    Page<VendorEntity> findByIsPaid(boolean b, Pageable pageable);
}