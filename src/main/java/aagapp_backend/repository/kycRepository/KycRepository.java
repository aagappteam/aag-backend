package aagapp_backend.repository.kycRepository;

import aagapp_backend.entity.kyc.KycEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KycRepository extends JpaRepository<KycEntity, Long> {
    Page<KycEntity> findByRole(String role, Pageable pageable);

    Page<KycEntity> findByMobileNumber(String mobileNumber, Pageable pageable);

    Page<KycEntity> findByRoleAndMobileNumber(String role, String mobileNumber, Pageable pageable);

    @Query("SELECT k FROM KycEntity k WHERE k.userOrVendorId = :userOrVendorId AND k.role = :role")
    KycEntity findByUserOrVendorIdAndRole(@Param("userOrVendorId") Long userOrVendorId,
                                          @Param("role") String role);


    @Query("SELECT CASE WHEN COUNT(k) > 0 THEN true ELSE false END FROM KycEntity k WHERE k.userOrVendorId = :userOrVendorId AND k.role = :role")
    boolean existsByUserOrVendorIdAndRole(@Param("userOrVendorId") Long userOrVendorId, @Param("role") String role);
}
/*
public interface KycRepository extends JpaRepository<KycEntity, Long> {

    @Query("SELECT k FROM KycEntity k WHERE LOWER(k.role) = LOWER(:role)")
    Page<KycEntity> findByRole(@Param("role") String role, Pageable pageable);

    @Query("SELECT k FROM KycEntity k WHERE LOWER(k.mobileNumber) = LOWER(:mobileNumber)")
    Page<KycEntity> findByMobileNumber(@Param("mobileNumber") String mobileNumber, Pageable pageable);

    @Query("SELECT k FROM KycEntity k WHERE LOWER(k.role) = LOWER(:role) AND LOWER(k.mobileNumber) = LOWER(:mobileNumber)")
    Page<KycEntity> findByRoleAndMobileNumber(@Param("role") String role, @Param("mobileNumber") String mobileNumber, Pageable pageable);
}*/
