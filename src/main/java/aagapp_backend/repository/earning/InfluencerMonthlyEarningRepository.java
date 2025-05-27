package aagapp_backend.repository.earning;

import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InfluencerMonthlyEarningRepository extends JpaRepository<InfluencerMonthlyEarning, Long> {
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

    // Now paginated
    Page<InfluencerMonthlyEarning> findByMonthYear(String monthYear, Pageable pageable);

    // This one remains non-paginated because it returns one item
    InfluencerMonthlyEarning findByInfluencerIdAndMonthYear(Long influencerId, String monthYear);

    Optional<InfluencerMonthlyEarning> findByPaymentId(Long paymentId);

    // Now paginated
    Page<InfluencerMonthlyEarning> findByInfluencerId(Long influencerId, Pageable pageable);

    Optional<InfluencerMonthlyEarning> findByInfluencerIdAndPaymentId(Long influencerId, Long paymentId);

    Optional<InfluencerMonthlyEarning> findTopByPaymentIdOrderByIdDesc(Long paymentId);
}
