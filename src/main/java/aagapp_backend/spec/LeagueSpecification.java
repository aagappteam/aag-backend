package aagapp_backend.spec;

import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class LeagueSpecification {

    public static Specification<League> withFilters(
            String name,
            String gameName,
            Long challengingVendorId,
            String challengingVendorName,
            Double fee,
            Integer move,
            LeagueStatus status,
            Long vendorId,
            Long opponentVendorId,
            String opponentVendorName,
            String vendorFirstName,
            String vendorLastName,
            String vendorMobileNumber,
            String vendorPrimaryEmail,
            String search
    ) {
        return (Root<League> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (StringUtils.hasText(name)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(gameName)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("gameName")), "%" + gameName.toLowerCase() + "%"));
            }

            if (challengingVendorId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("challengingVendorId"), challengingVendorId));
            }

            if (StringUtils.hasText(challengingVendorName)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("challengingVendorName")), "%" + challengingVendorName.toLowerCase() + "%"));
            }

            if (fee != null) {
                predicate = cb.and(predicate, cb.equal(root.get("fee"), fee));
            }

            if (move != null) {
                predicate = cb.and(predicate, cb.equal(root.get("move"), move));
            }

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            if (vendorId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("vendorEntity").get("id"), vendorId));
            }

            if (StringUtils.hasText(vendorFirstName)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("vendorEntity").get("first_name")), "%" + vendorFirstName.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(vendorLastName)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("vendorEntity").get("last_name")), "%" + vendorLastName.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(vendorPrimaryEmail)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("vendorEntity").get("primary_email")), "%" + vendorPrimaryEmail.toLowerCase() + "%"));
            }


            if (StringUtils.hasText(vendorMobileNumber)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("vendorEntity").get("mobileNumber")), "%" + vendorMobileNumber.toLowerCase() + "%"));
            }


            if (opponentVendorId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("opponentVendorId"), opponentVendorId));
            }

            if (StringUtils.hasText(opponentVendorName)) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("opponentVendorName")), "%" + opponentVendorName.toLowerCase() + "%"));
            }

            if(search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(root.get("vendorEntity").get("first_name")), keyword),
                        cb.like(cb.lower(root.get("vendorEntity").get("last_name")), keyword),
                        cb.like(cb.lower(root.get("vendorEntity").get("primary_email")), keyword),
                        cb.like(cb.lower(root.get("vendorEntity").get("mobileNumber")), keyword)));
            }
            return predicate;
        };
    }
}
