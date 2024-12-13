package aagapp_backend.repository.payment;

import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Query("SELECT p FROM PaymentEntity p WHERE p.vendorEntity.id = :vendorId " +
            "AND p.expiryAt > :currentDate AND p.status = :status " +
            "ORDER BY p.createdAt DESC")
    Optional<PaymentEntity> findActivePlanByVendorId(@Param("vendorId") Long vendorId,
                                                     @Param("currentDate") LocalDateTime currentDate,
                                                     @Param("status") PaymentStatus status);



}
