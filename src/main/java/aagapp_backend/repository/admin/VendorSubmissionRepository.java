package aagapp_backend.repository.admin;

import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.enums.ProfileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorSubmissionRepository extends JpaRepository<VendorSubmissionEntity, Long> {
    // Get submissions where approved is false
/*
    Page<VendorSubmissionEntity> findByApprovedFalse(Pageable pageable);
*/
    Page<VendorSubmissionEntity> findByApprovedFalseAndProfileStatusNot(Pageable pageable, ProfileStatus profileStatus);

    // Get submissions where approved is true
    Page<VendorSubmissionEntity> findByApprovedTrue(Pageable pageable);

    Page<VendorSubmissionEntity> findAll(Pageable pageable);


}