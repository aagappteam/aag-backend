package aagapp_backend.services.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.repository.admin.VendorSubmissionRepository;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.exception.VendorSubmissionException;
import aagapp_backend.services.url.UrlVerificationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class VendorSubmissionService {

    @Autowired
    private UrlVerificationService urlVerificationService;

    @Autowired
    private VendorSubmissionRepository submissionRepository;


    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    private EntityManager entityManager;


    public VendorSubmissionEntity submitDetails(VendorSubmissionEntity submissionEntity, VendorEntity vendorEntity) {
        // Check if vendor submission already exists
        VendorSubmissionEntity existingSubmission = this.getVendorSubmissionByServiceProviderId(vendorEntity.getService_provider_id());

        if (existingSubmission != null) {
            throw new VendorSubmissionException("Data already exists for the given vendor.");
        }

        for (Map.Entry<String, String> entry : submissionEntity.getSocialMediaUrls().entrySet()) {
            String platform = entry.getKey();
            String url = entry.getValue();
            if (!urlVerificationService.isUrlValid(url, platform)) {
                throw new VendorSubmissionException("Invalid URL for platform: " + platform + " - URL: " + url);
            }
        }

        VendorSubmissionEntity newEntity = new VendorSubmissionEntity();
        newEntity.setName(submissionEntity.getName());
        newEntity.setVendorEntity(vendorEntity);
        newEntity.setEmail(submissionEntity.getEmail());
        newEntity.setPlanName(submissionEntity.getPlanName());
        newEntity.setSocialMediaUrls(submissionEntity.getSocialMediaUrls());
        newEntity.setApproved(false);

        submissionRepository.save(newEntity);
        return newEntity;
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


    public VendorSubmissionEntity getVendorSubmissionByServiceProviderId(Long serviceProviderId) {
        try {
            String jpql = "SELECT v FROM VendorSubmissionEntity v WHERE v.vendorEntity.service_provider_id = :serviceProviderId";

            TypedQuery<VendorSubmissionEntity> query = entityManager.createQuery(jpql, VendorSubmissionEntity.class);
            query.setParameter("serviceProviderId", serviceProviderId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            exceptionHandlingImplement.handleException(e);
            return null;
        } catch (NonUniqueResultException e) {

            throw new IllegalStateException("Multiple submissions found for the same serviceProviderId, which should not happen.");
        }catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return null;
        }
    }



    public VendorSubmissionEntity updateVendorSubmission(VendorSubmissionEntity vendorSubmissionEntity, VendorSubmissionEntity existingSubmission) throws IllegalAccessException {
        Field[] fields = VendorSubmissionEntity.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            Object newValue = field.get(vendorSubmissionEntity);

            if (newValue != null && (newValue instanceof String && !((String) newValue).isEmpty() ||
                    newValue instanceof Map && !((Map<?, ?>) newValue).isEmpty() || !(newValue instanceof String || newValue instanceof Map))) {
                field.set(existingSubmission, newValue);
            }
        }

        if (existingSubmission.getSocialMediaUrls() != null) {
            for (Map.Entry<String, String> entry : existingSubmission.getSocialMediaUrls().entrySet()) {
                String platform = entry.getKey();
                String url = entry.getValue();
                if (!urlVerificationService.isUrlValid(url, platform)) {
                    throw new IllegalArgumentException("Invalid URL for platform: " + platform + " - URL: " + url);
                }
            }
        }

        return submissionRepository.save(existingSubmission);
    }


    public VendorSubmissionEntity getSubmissionById(Long id) {
        return submissionRepository.findById(id).orElse(null);
    }
}

