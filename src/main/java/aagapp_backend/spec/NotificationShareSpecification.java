package aagapp_backend.spec;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.notification.NotificationShare;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class NotificationShareSpecification {

    public static Specification<NotificationShare> vendorIdEquals(Long vendorId) {
        return (root, query, cb) -> vendorId != null ? cb.equal(root.get("vendorId"), vendorId) : null;
    }

    public static Specification<NotificationShare> amountEquals(Double amount) {
        return (root, query, cb) -> amount != null ? cb.equal(root.get("amount"), amount) : null;
    }

    /*public static Specification<NotificationShare> detailsContains(String keyword) {
        return (root, query, cb) ->
                keyword != null && !keyword.isBlank()
                        ? cb.like(cb.lower(root.get("details")), "%" + keyword.toLowerCase() + "%")
                        : null;
    }*/

    public static Specification<NotificationShare> createdAfter(ZonedDateTime from) {
        return (root, query, cb) ->
                from != null ? cb.greaterThanOrEqualTo(root.get("createdDate"), from) : null;
    }

    public static Specification<NotificationShare> createdBefore(ZonedDateTime to) {
        return (root, query, cb) ->
                to != null ? cb.lessThanOrEqualTo(root.get("createdDate"), to) : null;
    }


    public static Specification<NotificationShare> hasAmount(Double amount) {
        return (root, query, cb) ->
                amount != null ? cb.equal(root.get("amount"), amount) : null;
    }

    public static Specification<NotificationShare> detailsContains(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) return null;
            return cb.like(cb.lower(root.get("details")), "%" + term.toLowerCase() + "%");
        };
    }

    public static Specification<NotificationShare> createdBetween(ZonedDateTime from, ZonedDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null)
                return cb.between(root.get("createdDate"), from, to);
            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("createdDate"), from);
            if (to != null)
                return cb.lessThanOrEqualTo(root.get("createdDate"), to);
            return null;
        };
    }


    public static Specification<NotificationShare> vendorNameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            Join<NotificationShare, VendorEntity> vendor = root.join("vendor", JoinType.LEFT);
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(vendor.get("first_name")), pattern),
                    cb.like(cb.lower(vendor.get("last_name")), pattern)
            );
        };
    }

}
