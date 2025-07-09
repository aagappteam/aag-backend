package aagapp_backend.repository.payment;

import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.payment.PlanUpgradeRequest;
import aagapp_backend.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentPlanUpgradeRepository extends JpaRepository<PlanUpgradeRequest, Long> ,
        JpaSpecificationExecutor<PlanUpgradeRequest> {
    boolean existsByVendorIdAndStatus(Long vendorId, RequestStatus requestStatus);

    long countByStatus(RequestStatus requestStatus);

    Optional<PlanUpgradeRequest> findTopByVendorIdAndStatusOrderByApprovedDateDesc(Long vendorId, RequestStatus requestStatus);
}

