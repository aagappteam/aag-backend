package aagapp_backend.spec;

import aagapp_backend.entity.kyc.KycEntity;
import aagapp_backend.enums.KycStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class KycSpecification {

    public static Specification<KycEntity> hasRole(String role) {
        return (root, query, builder) ->
                (role == null || role.isEmpty()) ? null : builder.equal(root.get("role"), role);
    }

    public static Specification<KycEntity> hasMobileNumber(String mobileNumber) {
        return (root, query, builder) ->
                (mobileNumber == null || mobileNumber.isEmpty()) ? null : builder.equal(root.get("mobileNumber"), mobileNumber);
    }

    public static Specification<KycEntity> hasUserOrVendorId(Long id) {
        return (root, query, builder) ->
                (id == null) ? null : builder.equal(root.get("userOrVendorId"), id);
    }

    public static Specification<KycEntity> hasAadharNo(String aadharNo) {
        return (root, query, builder) ->
                (aadharNo == null || aadharNo.isEmpty()) ? null : builder.equal(root.get("aadharNo"), aadharNo);
    }

    public static Specification<KycEntity> hasPanNo(String panNo) {
        return (root, query, builder) ->
                (panNo == null || panNo.isEmpty()) ? null : builder.equal(root.get("panNo"), panNo);
    }

    public static Specification<KycEntity> createdAfter(Date date) {
        return (root, query, builder) ->
                (date == null) ? null : builder.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<KycEntity> createdBefore(Date date) {
        return (root, query, builder) ->
                (date == null) ? null : builder.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    public static Specification<KycEntity> hasStatus(KycStatus status) {
        return (root, query, builder) ->
                (status == null) ? null : builder.equal(root.get("kycStatus"), status);
    }

    public static Specification<KycEntity> hasName(String name) {
        return (root, query, builder) ->
                (name == null || name.isEmpty()) ? null : builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<KycEntity> hasEmail(String email) {
        return (root, query, builder) ->
                (email == null || email.isEmpty()) ? null : builder.equal(root.get("email"), email);
    }

    public static Specification<KycEntity> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) return null;

            String keyword = "%" + search.trim().toLowerCase() + "%";

            Predicate nameMatch = cb.like(cb.lower(root.get("name")), keyword);
            Predicate emailMatch = cb.like(cb.lower(root.get("email")), keyword);
            Predicate mobileMatch = cb.like(cb.lower(root.get("mobileNumber")), keyword);

            return cb.or(nameMatch, emailMatch, mobileMatch);
        };
    }


}