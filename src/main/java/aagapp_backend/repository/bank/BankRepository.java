package aagapp_backend.repository.bank;

import aagapp_backend.entity.Bank.BankEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BankRepository extends JpaRepository<BankEntity, Long>, JpaSpecificationExecutor<BankEntity> {

    Page<BankEntity> findByBankNameContainingIgnoreCase(String bankName, Pageable pageable);

    Page<BankEntity> findByBankId(Long bankId, Pageable pageable);

    Page<BankEntity> findByBankIdAndBankNameContainingIgnoreCase(Long bankId, String bankName, Pageable pageable);
}


