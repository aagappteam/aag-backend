package aagapp_backend.repository.withdrawrequest;

import aagapp_backend.entity.withdrawrequest.CustomerWithdrawalRequest;
import aagapp_backend.enums.WithdrawalStatus;
import aagapp_backend.enums.WithdrawalType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerWithdrawalRequestRepository extends JpaRepository<CustomerWithdrawalRequest, Long>, JpaSpecificationExecutor<CustomerWithdrawalRequest> {

    @Query("SELECT w FROM CustomerWithdrawalRequest w " +
            "WHERE w.customer.id = :customerId " +
            "AND (:withdrawalType IS NULL OR w.withdrawalType = :withdrawalType) " +
            "AND (:status IS NULL OR w.status = :status)")
    Page<CustomerWithdrawalRequest> findByCustomerIdAndFilters(@Param("customerId") Long customerId,
                                                               @Param("withdrawalType") WithdrawalType withdrawalType,
                                                               @Param("status") WithdrawalStatus status,
                                                               Pageable pageable);



}