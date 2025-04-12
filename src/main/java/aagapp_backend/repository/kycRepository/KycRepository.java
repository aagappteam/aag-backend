package aagapp_backend.repository.kycRepository;

import aagapp_backend.entity.kyc.KycEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycRepository extends JpaRepository<KycEntity, Long> {
    Page<KycEntity> findByRole(String role, Pageable pageable);

    Page<KycEntity> findByMobileNumber(String mobileNumber, Pageable pageable);

    Page<KycEntity> findByRoleAndMobileNumber(String role, String mobileNumber, Pageable pageable);

}
