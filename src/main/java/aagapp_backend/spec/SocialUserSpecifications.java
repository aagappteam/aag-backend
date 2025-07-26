package aagapp_backend.spec;

import aagapp_backend.entity.social.SocialUser;
import aagapp_backend.enums.SocialStatus;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Date;

public class SocialUserSpecifications {

    public static Specification<SocialUser> hasStatus(SocialStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<SocialUser> hasNameLike(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("customer").get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<SocialUser> hasEmailLike(String email) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("customer").get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<SocialUser> hasMobileLike(String mobile) {
        return (root, query, cb) ->
                cb.like(root.get("customer").get("mobileNumber"), "%" + mobile + "%");
    }

    public static Specification<SocialUser> createdBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            Path<Date> createdAt = root.get("createdAt");
            return cb.between(createdAt,
                    java.sql.Date.valueOf(startDate),
                    java.sql.Date.valueOf(endDate));
        };
    }


    public static Specification<SocialUser> searchAcrossFields(String search) {
        return (root, query, cb) -> {
            Predicate name = cb.like(cb.lower(root.get("customer").get("name")), "%" + search.toLowerCase() + "%");
            Predicate email = cb.like(cb.lower(root.get("customer").get("email")), "%" + search.toLowerCase() + "%");
            Predicate mobile = cb.like(root.get("customer").get("mobileNumber"), "%" + search + "%");
            return cb.or(name, email, mobile);
        };
    }
}