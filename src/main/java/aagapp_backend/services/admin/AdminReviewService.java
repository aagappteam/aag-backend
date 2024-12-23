package aagapp_backend.services.admin;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.enums.ProfileStatus;
import aagapp_backend.repository.admin.VendorSubmissionRepository;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminReviewService {

    @Autowired
    private VendorSubmissionRepository submissionRepository;

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    private EntityManager entitymanager; // Assuming EntityManager is available to save vendor entity.

    @Autowired
    private VenderService venderService; // Assuming VendorService is available to fetch vendor details.

    public Object reviewSubmission(Long id, boolean isApproved) {
        try {
            Optional<VendorSubmissionEntity> submission = submissionRepository.findById(id);

            if (submission.isPresent()) {
                VendorSubmissionEntity vendorSubmissionEntity = submission.get();

                if (vendorSubmissionEntity.getApproved() != null && vendorSubmissionEntity.getApproved()) {
                    return new SubmissionResponse("The submission has already been approved.", vendorSubmissionEntity);
                } else if (vendorSubmissionEntity.getApproved() != null && !vendorSubmissionEntity.getApproved()) {
                    return new SubmissionResponse("The submission has already been rejected.", vendorSubmissionEntity);
                }

                if(isApproved){
                    vendorSubmissionEntity.setProfileStatus(ProfileStatus.ACTIVE);
                }

                vendorSubmissionEntity.setApproved(isApproved);
                submissionRepository.save(vendorSubmissionEntity);
                VendorEntity vendorEntity = new VendorEntity();

                vendorEntity.setIsVerified(1);
                entitymanager.persist(vendorEntity);

                return new SubmissionResponse(isApproved ? "The submission has been successfully approved." : "The submission has been successfully rejected.", vendorSubmissionEntity);
            }
            return new SubmissionResponse("Submission with ID " + id + " not found.", null); // Submission not found
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return new SubmissionResponse("An error occurred while processing the review request.", null);
        }
    }


    @Getter
    @Setter
    public static class SubmissionResponse {
        private String message;
        private VendorSubmissionEntity data;

        public SubmissionResponse(String message, VendorSubmissionEntity data) {
            this.message = message;
            this.data = data;
        }

    }
}
