package aagapp_backend.spec;


import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.TournamentStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class TournamentSpecification {

    public static Specification<Tournament> withFilters(
            Long id,
            Long vendorId,
            String name,
            BigDecimal totalPrizePool,
            TournamentStatus status,
            String vendorName,
            String vendorEmail,
            String vendorMobile,
            String search
    ) {
        return (Root<Tournament> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate = cb.conjunction();

            if (id != null) {
                predicate = cb.and(predicate, cb.equal(root.get("id"), id));
            }

            if (vendorId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("vendorId"), vendorId));
            }

            if (name != null && !name.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (totalPrizePool != null) {
                predicate = cb.and(predicate, cb.equal(root.get("totalPrizePool"), totalPrizePool));
            }

            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            if (vendorName != null && !vendorName.isBlank()) {
                Join<Object, Object> vendorJoin = root.join("vendorEntity", JoinType.LEFT);
                Expression<String> fullName = cb.concat(
                        cb.lower(vendorJoin.get("first_name")), " "
                );
                Expression<String> fullVendorName = cb.concat(fullName, cb.lower(vendorJoin.get("last_name")));
                predicate = cb.and(predicate, cb.like(fullVendorName, "%" + vendorName.toLowerCase() + "%"));
            }

            if (vendorEmail != null && !vendorEmail.isBlank()) {
                Join<Object, Object> vendorJoin = root.join("vendorEntity", JoinType.LEFT);
                predicate = cb.and(predicate, cb.like(cb.lower(vendorJoin.get("primary_email")), "%" + vendorEmail.toLowerCase() + "%"));
            }

            if (vendorMobile != null && !vendorMobile.isBlank()) {
                Join<Object, Object> vendorJoin = root.join("vendorEntity", JoinType.LEFT);
                predicate = cb.and(predicate, cb.like(vendorJoin.get("mobileNumber"), "%" + vendorMobile + "%"));
            }

            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(root.get("vendorEntity").get("first_name")), keyword),
                        cb.like(cb.lower(root.get("vendorEntity").get("last_name")), keyword),
                        cb.like(cb.lower(root.get("vendorEntity").get("primary_email")), keyword),
                        cb.like(cb.lower(root.get("vendorEntity").get("mobileNumber")), keyword)
                ));
            }

            return predicate;
        };
    }
}

