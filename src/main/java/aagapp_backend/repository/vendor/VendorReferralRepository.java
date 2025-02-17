package aagapp_backend.repository.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorReferral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorReferralRepository extends JpaRepository<VendorReferral, Long>{
    List<VendorReferral> findByReferrerId(VendorEntity referrerId);
    VendorReferral findByReferredId(VendorEntity referredId);

}
