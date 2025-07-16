package aagapp_backend.repository;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorSubmissionEntityRepository extends JpaRepository<VendorSubmissionEntity, Long> {

    VendorSubmissionEntity findByVendorEntity(VendorEntity vendorEntity);

}