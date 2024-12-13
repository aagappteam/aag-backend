package aagapp_backend.services.payment;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.repository.payment.PaymentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;


    public List<PaymentEntity> findActivePlansByVendorId(Long vendorId) {
        String query = "SELECT p FROM PaymentEntity p WHERE p.vendorEntity.id = :vendorId AND p.status = 'ACTIVE'";

        TypedQuery<PaymentEntity> typedQuery = entityManager.createQuery(query, PaymentEntity.class);
        typedQuery.setParameter("vendorId", vendorId);

        List<PaymentEntity> result = typedQuery.getResultList();
        System.out.println("Found " + result.size() + " active payments for vendorId: " + vendorId);
        return result;
    }

    public PaymentEntity createPayment(PaymentEntity paymentRequest, Long vendorId) {
        VendorEntity existingVendor = entityManager.find(VendorEntity.class, vendorId);

        if (existingVendor == null) {
            throw new RuntimeException("Vendor not found with ID: " + vendorId);
        }

        paymentRequest.setVendorEntity(existingVendor);

        paymentRequest.setTransactionId(UUID.randomUUID().toString());

        if (paymentRequest.getDailyLimit() == null) {
            paymentRequest.setDailyLimit(5); // Default to 5 if not set
        }


        paymentRequest.setStatus(PaymentStatus.ACTIVE);

        String planDuration = paymentRequest.getPlanDuration();
        if (planDuration != null) {
            switch (planDuration.toLowerCase()) {
                case "monthly":
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusDays(30));
                    break;
                case "yearly":
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusDays(365));
                    break;
                default:
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusDays(1));
                    break;
            }
        } else {
            paymentRequest.setExpiryAt(LocalDateTime.now().plusDays(1));

        }

        paymentRequest.setPaymentType(paymentRequest.getPaymentType()); // e.g., "RECURRING" or "ONETIME"
        // Checking if there is an active payment plan for the vendor and expire it
        List<PaymentEntity> existingActivePayments = this.findActivePlansByVendorId(vendorId);
        if (existingActivePayments != null && !existingActivePayments.isEmpty()) {
            for (PaymentEntity existingPayment : existingActivePayments) {
                existingPayment.setStatus(PaymentStatus.EXPIRED);
//                existingPayment.setExpiryAt(LocalDateTime.now());
                paymentRepository.save(existingPayment);
            }
        }


        return paymentRepository.save(paymentRequest);
    }



}

