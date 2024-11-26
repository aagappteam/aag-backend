package aagapp_backend.services.payment;

import aagapp_backend.entity.payment.Payment;
import aagapp_backend.repository.payment.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    public Payment getActivePlan(Long vendorId) {
        return paymentRepository.findActivePlanByVendorId(vendorId, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("No active plan found for the vendor"));
    }
}

