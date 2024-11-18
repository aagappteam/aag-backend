package aagapp_backend.repository.admin;

import aagapp_backend.entity.VendorSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorSubmissionRepository extends JpaRepository<VendorSubmissionEntity, Long> {
    List<VendorSubmissionEntity> findByApprovedFalse();
    List<VendorSubmissionEntity> findByApprovedTrue();


}

