package aagapp_backend.services.payment;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.repository.payment.PaymentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;


    public List<PaymentEntity> findActivePlansByVendorId(Long vendorId) {
        String query = "SELECT p FROM PaymentEntity p WHERE p.vendorEntity.id = :vendorId AND p.status = 'ACTIVE'";

        TypedQuery<PaymentEntity> typedQuery = entityManager.createQuery(query, PaymentEntity.class);
        typedQuery.setParameter("vendorId", vendorId);

        List<PaymentEntity> result = typedQuery.getResultList();
        logger.info("Found {} active payments for vendorId: {}", result.size(), vendorId);
        return result;
    }


    @Transactional
    public PaymentEntity createPayment(PaymentEntity paymentRequest, Long vendorId) {
        VendorEntity existingVendor = entityManager.find(VendorEntity.class, vendorId);

        if (existingVendor == null) {
            throw new RuntimeException("Vendor not found with ID: " + vendorId);
        }

        // Associate the vendor with the payment
        paymentRequest.setVendorEntity(existingVendor);

        // Generate a unique transaction ID
        paymentRequest.setTransactionId(UUID.randomUUID().toString());

        // Set default daily limit if not provided
        if (paymentRequest.getDailyLimit() == null) {
            paymentRequest.setDailyLimit(5);  // Default to 5 if not set
        }

        // Set status to ACTIVE by default
        paymentRequest.setStatus(PaymentStatus.ACTIVE);

        // Set expiry date based on the plan duration
        setPlanExpiry(paymentRequest);

        // Set the payment type
        paymentRequest.setPaymentType(paymentRequest.getPaymentType());

        // Expire any existing active payments for the vendor
        expireExistingActivePayments(vendorId);

        // Save and return the newly created payment
        return paymentRepository.save(paymentRequest);
    }


    private void setPlanExpiry(PaymentEntity paymentRequest) {
        String planDuration = paymentRequest.getPlanDuration();
        if (planDuration != null) {
            switch (planDuration.toLowerCase()) {
                case "monthly":
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusMonths(1));
                    break;
                case "yearly":
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusYears(1));
                    break;
                default:
                    paymentRequest.setExpiryAt(LocalDateTime.now().plusDays(1));
                    break;
            }
        } else {
            // Default to 1-day expiry if no plan duration is provided
            paymentRequest.setExpiryAt(LocalDateTime.now().plusDays(1));
        }
    }


    private void expireExistingActivePayments(Long vendorId) {
        List<PaymentEntity> existingActivePayments = this.findActivePlansByVendorId(vendorId);
        if (existingActivePayments != null && !existingActivePayments.isEmpty()) {
            for (PaymentEntity existingPayment : existingActivePayments) {
                existingPayment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(existingPayment);  // Mark them as expired
            }
        }
    }

    // Updated to include transactionReference as an additional parameter
    public List<PaymentEntity> getTransactionsByVendorId(Long vendorId, int page, int size, String transactionReference) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findAllTransactionsByVendorId(vendorId, transactionReference, pageable);
    }

    // Updated to include transactionReference as an additional parameter
    public List<PaymentEntity> getAllTransactionsByVendorName(String vendorName, int page, int size, String transactionReference) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findAllTransactionsByVendorName(vendorName, transactionReference, pageable);
    }
}
