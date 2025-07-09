package aagapp_backend.spec;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.withdrawrequest.CustomerWithdrawalRequest;
import aagapp_backend.enums.WithdrawalStatus;
import aagapp_backend.enums.WithdrawalType;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class WithdrawalRequestSpecification {

    public static Specification<CustomerWithdrawalRequest> hasStatus(WithdrawalStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<CustomerWithdrawalRequest> hasWithdrawalType(WithdrawalType withdrawalType) {
        return (root, query, cb) ->
                withdrawalType == null ? null : cb.equal(root.get("withdrawalType"), withdrawalType);
    }

    public static Specification<CustomerWithdrawalRequest> hasCustomerId(Long customerId) {
        return (root, query, cb) ->
                customerId == null ? null : cb.equal(root.get("customer").get("id"), customerId);
    }

    public static Specification<CustomerWithdrawalRequest> requestDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return null;
            if (startDate != null && endDate != null) {
                return cb.between(root.get("requestDate"), startDate, endDate);
            }
            if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("requestDate"), startDate);
            }
            return cb.lessThanOrEqualTo(root.get("requestDate"), endDate);
        };
    }

    public static Specification<CustomerWithdrawalRequest> customerNameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty()) return null;
            Join<CustomerWithdrawalRequest, CustomCustomer> customerJoin = root.join("customer");
            return cb.like(cb.lower(customerJoin.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<CustomerWithdrawalRequest> customerEmailContains(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isEmpty()) return null;
            Join<CustomerWithdrawalRequest, CustomCustomer> customerJoin = root.join("customer");
            return cb.like(cb.lower(customerJoin.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<CustomerWithdrawalRequest> customerMobileNumberContains(String mobileNumber) {
        return (root, query, cb) -> {
            if (mobileNumber == null || mobileNumber.isEmpty()) return null;
            Join<CustomerWithdrawalRequest, CustomCustomer> customerJoin = root.join("customer");
            return cb.like(customerJoin.get("mobileNumber"), "%" + mobileNumber + "%");
        };
    }

    public static Specification<CustomerWithdrawalRequest> customerStateEquals(String state) {
        return (root, query, cb) -> {
            if (state == null || state.isEmpty()) return null;
            Join<CustomerWithdrawalRequest, CustomCustomer> customerJoin = root.join("customer");
            return cb.equal(cb.lower(customerJoin.get("state")), state.toLowerCase());
        };
    }
    //common search filter name, email, mobile
    public static Specification<CustomerWithdrawalRequest> commonSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isEmpty()) return null;
            Join<CustomerWithdrawalRequest, CustomCustomer> customerJoin = root.join("customer");
            return cb.or(
                    cb.like(cb.lower(customerJoin.get("name")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(customerJoin.get("email")), "%" + search.toLowerCase() + "%"),
                    cb.like(customerJoin.get("mobileNumber"), "%" + search + "%")
            );
        };
    }
}

