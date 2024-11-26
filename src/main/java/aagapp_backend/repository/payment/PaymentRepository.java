package aagapp_backend.repository.payment;

import aagapp_backend.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.vendorEntity.id = :vendorId " +
            "AND p.expiryAt > :currentDate AND p.status = 'SUCCESSFUL' " +
            "ORDER BY p.createdAt DESC")
    Optional<Payment> findActivePlanByVendorId(@Param("vendorId") Long vendorId,
                                               @Param("currentDate") LocalDateTime currentDate);
}
