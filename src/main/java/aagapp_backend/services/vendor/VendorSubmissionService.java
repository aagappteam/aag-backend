package aagapp_backend.services.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.repository.admin.VendorSubmissionRepository;
import aagapp_backend.services.url.UrlVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class VendorSubmissionService {

    @Autowired
    private UrlVerificationService urlVerificationService;

    @Autowired
    private VendorSubmissionRepository submissionRepository;


    public VendorSubmissionEntity submitDetails(VendorSubmissionEntity submissionEntity ,VendorEntity vendorEntity) {
        // Validating all social media URLs
        for (Map.Entry<String, String> entry : submissionEntity.getSocialMediaUrls().entrySet()) {
            String platform = entry.getKey();
            String url = entry.getValue();
            if (!urlVerificationService.isUrlValid(url, platform)) {
                throw new IllegalArgumentException("Invalid URL for platform: " + platform + " - URL: " + url);
            }
        }

        VendorSubmissionEntity entity = new VendorSubmissionEntity();
        entity.setName(submissionEntity.getName());
        entity.setVendorEntity(vendorEntity);
        entity.setEmail(submissionEntity.getEmail());
        entity.setPlanName(submissionEntity.getPlanName());
        entity.setSocialMediaUrls(submissionEntity.getSocialMediaUrls());
        entity.setApproved(false);

        submissionRepository.save(entity);

        return entity;
    }
    public List<VendorSubmissionEntity> getSubmissionsByStatus(String status) {
        System.out.println("Fetching submissions with status: " + status);
        List<VendorSubmissionEntity> result;
        if ("pending".equalsIgnoreCase(status)) {
            result = submissionRepository.findByApprovedFalse();
        } else if ("approved".equalsIgnoreCase(status)) {
            result = submissionRepository.findByApprovedTrue();
        } else {
            result = submissionRepository.findAll();
        }
        System.out.println("Found " + result.size() + " submissions");
        return result;
    }


}

