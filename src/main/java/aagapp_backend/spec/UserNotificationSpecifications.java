package aagapp_backend.spec;

import aagapp_backend.entity.notification.UserNotification;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;

public class UserNotificationSpecifications {

    public static Specification<UserNotification> hasRole(String role) {
        return (root, query, cb) -> {
            if (role == null || role.isBlank()) return null;
            return cb.equal(cb.lower(root.get("role")), role.toLowerCase());
        };
    }

    public static Specification<UserNotification> hasVendorId(Long vendorId) {
        return (root, query, cb) -> {
            if (vendorId == null) return null;
            return cb.equal(root.get("vendorId"), vendorId);
        };
    }

    public static Specification<UserNotification> hasCustomerId(Long customerId) {
        return (root, query, cb) -> {
            if (customerId == null) return null;
            return cb.equal(root.get("customerId"), customerId);
        };
    }

    public static Specification<UserNotification> hasAmount(Double amount) {
        return (root, query, cb) -> {
            if (amount == null) return null;
            return cb.equal(root.get("amount"), amount);
        };
    }

    public static Specification<UserNotification> hasMinAmount(Double minAmount) {
        return (root, query, cb) -> {
            if (minAmount == null) return null;
            return cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
        };
    }

    public static Specification<UserNotification> hasMaxAmount(Double maxAmount) {
        return (root, query, cb) -> {
            if (maxAmount == null) return null;
            return cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
        };
    }

    public static Specification<UserNotification> createdBetween(ZonedDateTime start, ZonedDateTime end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start != null && end != null)
                return cb.between(root.get("createdDate"), start, end);
            if (start != null)
                return cb.greaterThanOrEqualTo(root.get("createdDate"), start);
            return cb.lessThanOrEqualTo(root.get("createdDate"), end);
        };
    }

    public static Specification<UserNotification> descriptionContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<UserNotification> detailsContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(cb.lower(root.get("details")), "%" + keyword.toLowerCase() + "%");
        };
    }
}
