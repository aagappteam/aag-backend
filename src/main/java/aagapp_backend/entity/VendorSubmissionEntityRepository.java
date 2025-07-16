package aagapp_backend.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorSubmissionEntityRepository extends JpaRepository<VendorSubmissionEntity, Long> {

    VendorSubmissionEntity findByVendorEntity(VendorEntity vendorEntity);

}