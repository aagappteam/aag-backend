package aagapp_backend.spec;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.enums.KycStatus;
import aagapp_backend.enums.VendorStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomCustomerSpecification {

    public static Specification<CustomCustomer> filterCustomers(
            String mobileNumber,
            String name,
            String email,
            KycStatus kycStatus,
            VendorStatus vendorStatus,
            Date startDate,
            Date endDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                predicates.add(cb.equal(root.get("mobileNumber"), mobileNumber));
            }

            if (name != null && !name.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (email != null && !email.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }

            if (kycStatus != null) {
                predicates.add(cb.equal(root.get("kycStatus"), kycStatus));
            }

            if (vendorStatus != null) {
                predicates.add(cb.equal(root.get("status"), vendorStatus));
            }

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("createdDate"), startDate, endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}