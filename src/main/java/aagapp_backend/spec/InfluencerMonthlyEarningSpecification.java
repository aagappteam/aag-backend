package aagapp_backend.spec;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import jakarta.persistence.criteria.*;
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
            Integer multiplier,
            String search
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

            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";

                // Perform a subquery join with VendorEntity
                Subquery<Long> vendorSubquery = query.subquery(Long.class);
                Root<VendorEntity> vendorRoot = vendorSubquery.from(VendorEntity.class);

                Predicate vendorPredicate = cb.or(
                        cb.like(cb.lower(vendorRoot.get("first_name")), keyword),
                        cb.like(cb.lower(vendorRoot.get("last_name")), keyword),
                        cb.like(cb.lower(vendorRoot.get("primary_email")), keyword),
                        cb.like(cb.lower(vendorRoot.get("mobileNumber")), keyword)
                );

                vendorSubquery.select(vendorRoot.get("service_provider_id")).where(vendorPredicate);

                p = cb.and(p, root.get("influencerId").in(vendorSubquery));
            }



            // Similar logic could be added for influencerName if influencer entity exists

            return p;
        };
    }
}
