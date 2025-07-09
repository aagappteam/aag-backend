package aagapp_backend.spec;

import aagapp_backend.entity.game.Game;
import aagapp_backend.enums.GameStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameSpecification {

    public static Specification<Game> filterGames(String status, String gamename, String vendorName, Long vendorId, String vendorEmail, String vendorMobile,
                                                  ZonedDateTime startDate, ZonedDateTime endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), GameStatus.valueOf(status)));
            }

            if (gamename != null && !gamename.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + gamename.toLowerCase() + "%"));
            }

            if (vendorId != null) {
                predicates.add(cb.equal(root.get("vendorEntity").get("id"), vendorId));
            }

            if (vendorName != null && !vendorName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("vendorEntity").get("first_name")), "%" + vendorName.toLowerCase() + "%"));
            }

            if (vendorEmail != null) {
                predicates.add(cb.like(cb.lower(root.get("vendorEntity").get("primary_email")), "%" + vendorEmail.toLowerCase() + "%"));
            }

            if (vendorMobile != null) {
                predicates.add(cb.like(cb.lower(root.get("vendorEntity").get("mobileNumber")), "%" + vendorMobile.toLowerCase() + "%"));
            }

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("createdDate"), startDate, endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
