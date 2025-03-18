package aagapp_backend.services.payment;

import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.payment.PlanEntity;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.repository.payment.PaymentRepository;
import aagapp_backend.repository.payment.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Create a new Plan
    public PlanEntity createPlan(PlanEntity planEntity) {
        return planRepository.save(planEntity);
    }

    public List<PlanEntity> getAllPlans() {
        return planRepository.findAll();
    }

    // Get Plan by ID
    public PlanEntity getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found with id " + id));
    }


    public List<PlanEntity> getPlansByVariant(String planVariant) {
        return planRepository.findByPlanVariant(planVariant);
    }

}
