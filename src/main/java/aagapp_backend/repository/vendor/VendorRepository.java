package aagapp_backend.repository.vendor;

import aagapp_backend.entity.VendorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<VendorEntity, Long> {
    VendorEntity findServiceProviderByReferralCode(String referralCode);
}
