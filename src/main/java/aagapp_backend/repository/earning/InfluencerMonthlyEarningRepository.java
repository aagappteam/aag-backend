package aagapp_backend.repository.earning;

import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InfluencerMonthlyEarningRepository extends JpaRepository<InfluencerMonthlyEarning, Long> {

    // Now paginated
    Page<InfluencerMonthlyEarning> findByMonthYear(String monthYear, Pageable pageable);

    // This one remains non-paginated because it returns one item
    InfluencerMonthlyEarning findByInfluencerIdAndMonthYear(Long influencerId, String monthYear);

    // Now paginated
    Page<InfluencerMonthlyEarning> findByInfluencerId(Long influencerId, Pageable pageable);
}
