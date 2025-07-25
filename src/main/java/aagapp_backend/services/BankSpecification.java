package aagapp_backend.services;

import aagapp_backend.entity.Bank.BankEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

public class BankSpecification {

    public static Specification<BankEntity> hasBankId(Long bankId) {
        return (root, query, cb) ->
                bankId == null ? null : cb.equal(root.get("bankId"), bankId);
    }

    public static Specification<BankEntity> hasBankNameLike(String bankName) {
        return (root, query, cb) ->
                bankName == null ? null : cb.like(cb.lower(root.get("bankName")), "%" + bankName.toLowerCase() + "%");
    }
}

