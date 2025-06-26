package aagapp_backend.spec;

import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InfluencerMonthlyEarningSpecification {

    public static Specification<InfluencerMonthlyEarning> filter(
            Long influencerId,
            String monthYear,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minEarning,
            BigDecimal maxEarning,
            BigDecimal minRecharge,
            BigDecimal maxRecharge,
            Integer multiplier
    ) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();

            if (influencerId != null) {
                p = cb.and(p, cb.equal(root.get("influencerId"), influencerId));
            }
            if (monthYear != null) {
                p = cb.and(p, cb.equal(root.get("monthYear"), monthYear));
            }

            // If you have createdAt or similar column...
            if (startDate != null) {
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("monthYearDate"), startDate));
            }
            if (endDate != null) {
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("monthYearDate"), endDate));
            }

            if (minEarning != null) {
                p = cb.and(p, cb.ge(root.get("earnedAmount"), minEarning));
            }
            if (maxEarning != null) {
                p = cb.and(p, cb.le(root.get("earnedAmount"), maxEarning));
            }
            if (minRecharge != null) {
                p = cb.and(p, cb.ge(root.get("rechargeAmount"), minRecharge));
            }
            if (maxRecharge != null) {
                p = cb.and(p, cb.le(root.get("rechargeAmount"), maxRecharge));
            }
            if (multiplier != null) {
                p = cb.and(p, cb.equal(root.get("multiplier"), multiplier));
            }


            // Similar logic could be added for influencerName if influencer entity exists

            return p;
        };
    }
}
