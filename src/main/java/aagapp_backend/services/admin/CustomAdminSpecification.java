package aagapp_backend.services.admin;

import aagapp_backend.entity.CustomAdmin;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CustomAdminSpecification {
    public static Specification<CustomAdmin> hasRole(Integer roleId) {
        return (root, query, cb) -> roleId == null ? null : cb.equal(root.get("role"), roleId);
    }

    public static Specification<CustomAdmin> hasMobileNumber(String mobileNumber) {
        return (root, query, cb) ->
                (mobileNumber == null || mobileNumber.isEmpty())
                        ? null
                        : cb.like(cb.lower(root.get("mobileNumber")), "%" + mobileNumber.toLowerCase() + "%");
    }

    public static Specification<CustomAdmin> hasUserName(String userName) {
        return (root, query, cb) ->
                (userName == null || userName.isEmpty())
                        ? null
                        : cb.like(cb.lower(root.get("user_name")), "%" + userName.toLowerCase() + "%");
    }
}

