package aagapp_backend.repository.payment;
import aagapp_backend.entity.payment.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, Long> {
    List<PlanEntity> findByPlanVariant(String planVariant);
}
