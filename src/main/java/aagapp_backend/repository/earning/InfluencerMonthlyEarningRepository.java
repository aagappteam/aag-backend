package aagapp_backend.repository.earning;

import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InfluencerMonthlyEarningRepository extends JpaRepository<InfluencerMonthlyEarning, Long>, JpaSpecificationExecutor<InfluencerMonthlyEarning> {
    @Query(value = """
    SELECT e FROM InfluencerMonthlyEarning e 
    WHERE e.influencerId = :influencerId 
    AND e.monthYear = (
        SELECT MAX(e2.monthYear) FROM InfluencerMonthlyEarning e2 
        WHERE e2.influencerId = :influencerId
    )
    ORDER BY e.id DESC  LIMIT 1

    """)
    Optional<InfluencerMonthlyEarning> findLatestByInfluencerId(@Param("influencerId") Long influencerId);


    Optional<InfluencerMonthlyEarning> findByPaymentId(Long paymentId);

    @Query("SELECT e FROM InfluencerMonthlyEarning e WHERE e.influencerId = :influencerId "
            + "AND (:monthYear IS NULL OR e.monthYear = :monthYear)")
    Page<InfluencerMonthlyEarning> findByInfluencerId(
            @Param("influencerId") Long influencerId,
            @Param("monthYear") String monthYear,
            Pageable pageable);



    Optional<InfluencerMonthlyEarning> findByInfluencerIdAndPaymentId(Long influencerId, Long paymentId);

    Optional<InfluencerMonthlyEarning> findTopByPaymentIdOrderByIdDesc(Long paymentId);
}
