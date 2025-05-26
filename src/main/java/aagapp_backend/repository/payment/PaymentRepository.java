package aagapp_backend.repository.payment;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
/*    @Query("SELECT p FROM PaymentEntity p WHERE p.vendorEntity.id = :vendorId " +
            "AND p.expiryAt > :currentDate AND p.status = :status " +
            "ORDER BY p.createdAt DESC")
    Optional<PaymentEntity> findActivePlanByVendorId(@Param("vendorId") Long vendorId,
                                                     @Param("currentDate") LocalDateTime currentDate,
                                                     @Param("status") PaymentStatus status);*/

    @Query("SELECT p FROM PaymentEntity p " +
            "WHERE p.vendorEntity.id = :vendorId " +
            "AND :currentDate BETWEEN p.createdAt AND p.expiryAt " +
            "AND p.status = :status " +
            "ORDER BY p.createdAt DESC")
    Optional<PaymentEntity> findActivePlanByVendorId(@Param("vendorId") Long vendorId,
                                                     @Param("currentDate") LocalDateTime currentDate,
                                                     @Param("status") PaymentStatus status);




    @Query("SELECT p FROM PaymentEntity p WHERE p.vendorEntity.id = :vendorId " +
            "AND (:transactionReference IS NULL OR p.transactionId = :transactionReference) " +
            "ORDER BY p.transactionDate DESC")
    List<PaymentEntity> findAllTransactionsByVendorId(@Param("vendorId") Long vendorId,
                                                      @Param("transactionReference") String transactionReference,
                                                      Pageable pageable);

    @Query("SELECT p FROM PaymentEntity p WHERE (p.vendorEntity.first_name = :vendorName OR p.vendorEntity.last_name = :vendorName) " +
            "AND (:transactionReference IS NULL OR p.transactionId = :transactionReference) " +
            "ORDER BY p.createdAt DESC")
    List<PaymentEntity> findAllTransactionsByVendorName(@Param("vendorName") String vendorName,
                                                        @Param("transactionReference") String transactionReference,
                                                        Pageable pageable);


    // Custom method to count the number of payments for a given vendor
    long countByVendorEntity(VendorEntity vendor);

    PaymentEntity findByTransactionId(String transactionId);




}
