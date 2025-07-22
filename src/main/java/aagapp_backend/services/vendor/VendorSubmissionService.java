package aagapp_backend.services.vendor;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.VendorSubmissionEntity;
import aagapp_backend.enums.ProfileStatus;
import aagapp_backend.repository.admin.VendorSubmissionRepository;
import aagapp_backend.services.EmailService;
import aagapp_backend.services.admin.AdminReviewService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.exception.VendorSubmissionException;
import aagapp_backend.services.url.UrlVerificationService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.Query;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
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
    private EmailService emailService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    private AdminReviewService adminReviewService;

    @Transactional
    public VendorSubmissionEntity submitDetails(VendorSubmissionEntity submissionEntity, VendorEntity vendorEntity) throws IOException {
        try {
            // Check if vendor submission already exists
            VendorSubmissionEntity existingSubmission = this.getVendorSubmissionByServiceProviderId(vendorEntity.getService_provider_id());

            if (existingSubmission != null) {
                throw new VendorSubmissionException("Data already exists for the given vendor.");
            }
            System.out.println("efdfdc");

            // Check for invalid URLs
            for (Map.Entry<String, String> entry : submissionEntity.getSocialMediaUrls().entrySet()) {
                String platform = entry.getKey();
                String url = entry.getValue();
                if (!urlVerificationService.isUrlValid(url, platform)) {
                    throw new VendorSubmissionException("Url is not valid for " + platform);
                }
            }



            // Data persistence only after all validation checks pass
            VendorSubmissionEntity newEntity = new VendorSubmissionEntity();
            newEntity.setFirstName(submissionEntity.getFirstName());
            newEntity.setLastName(submissionEntity.getLastName());
            newEntity.setVendorEntity(vendorEntity);
            newEntity.setEmail(submissionEntity.getEmail());
            newEntity.setPlanName(submissionEntity.getPlanName());
            newEntity.setSocialMediaUrls(submissionEntity.getSocialMediaUrls());
            newEntity.setApproved(false);
            entityManager.persist(newEntity);  // Persist vendor submission entity

            vendorEntity.setPrimary_email(submissionEntity.getEmail());
            vendorEntity.setFirst_name(submissionEntity.getFirstName());
            vendorEntity.setLast_name(submissionEntity.getLastName());
            String mobile = submissionEntity.getMobileNumber();
            String lastFourDigits = mobile != null && mobile.length() >= 4
                    ? mobile.substring(mobile.length() - 4)
                    : "0000";

            String firstNamePart = submissionEntity.getFirstName() != null
                    ? submissionEntity.getFirstName().replaceAll("\\s+", "").toLowerCase()
                    : "AAGVEER";

            vendorEntity.setUser_name(firstNamePart + lastFourDigits);
            entityManager.merge(vendorEntity);  // Update vendor entity

            sendonboardingmail(vendorEntity);  // Send onboarding email

            return newEntity;
        } catch (VendorSubmissionException ex) {
            // Re-throw the VendorSubmissionException so it can be handled in the controller
            throw ex;
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            throw new VendorSubmissionException("Failed to submit vendor details.");
        }
    }


    public void sendonboardingmail(VendorEntity vendorEntity) throws IllegalAccessException, IOException {
        System.out.println(vendorEntity.getIsVerified() + " is verified" + vendorEntity.getPrimary_email() + " " + vendorEntity.getFirst_name() + " " + vendorEntity.getLast_name());

        // Try to send the email, but don't block the other actions if it fails
        emailService.sendOnboardingEmail(vendorEntity.getPrimary_email(), vendorEntity.getFirst_name(), vendorEntity.getLast_name());

//            emailService.sendOnboardingEmail(vendorEntity.getPrimary_email(), vendorEntity.getFirst_name(), vendorEntity.getLast_name());
    }

/*    public Page<VendorSubmissionEntity> getSubmissionsByStatus(String status, Pageable pageable) {
        List<VendorSubmissionEntity> result;
        if ("pending".equalsIgnoreCase(status)) {
            result = submissionRepository.findByApprovedFalse();
        } else if ("approved".equalsIgnoreCase(status)) {
            result = submissionRepository.findByApprovedTrue();
        } else {
            result = submissionRepository.findAll();
        }
        return result;
    }*/

    public Page<VendorSubmissionEntity> getSubmissionsByStatus(String status, Pageable pageable) {
        // Sort by id in descending order to get the most recently created submissions first
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("id")));

        switch (status.toLowerCase()) {
            case "approved":
                return submissionRepository.findByApprovedTrue(sortedPageable);  // Only approved submissions, sorted by ID
            case "pending":
                return submissionRepository.findByApprovedFalseAndProfileStatusNot(sortedPageable, ProfileStatus.REJECTED);
            case "all":
            default:
                return submissionRepository.findAll(sortedPageable);  // All submissions, sorted by ID
        }
    }



    public VendorSubmissionEntity getVendorSubmissionByServiceProviderId(Long serviceProviderId) {
        try {
//            String jpql = "SELECT v FROM VendorSubmissionEntity v WHERE v.vendorEntity.service_provider_id = :serviceProviderId";
            String jpql = "SELECT v FROM VendorSubmissionEntity v JOIN FETCH v.vendorEntity WHERE v.vendorEntity.service_provider_id = :serviceProviderId";

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

    public void deleteVendorSubmission(Long id) throws VendorSubmissionException {

        submissionRepository.deleteById(id);
    }

    public Page<VendorSubmissionEntity> getSubmissionsByEmail(String email, Pageable pageable) {
        return submissionRepository.findByEmail(email, pageable);


    }

    public long countByApproved(boolean b) {
        return submissionRepository.countByApproved(b);
    }

    @Transactional
    public Page<VendorSubmissionEntity> getFilteredSubmissionsNative(
            String email,
            String search,
            ProfileStatus profileStatus,
            Boolean approved,
            String planName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        try {
            StringBuilder sql = new StringBuilder(
                    "SELECT v.* FROM vendor_submission_details v " +
                            "LEFT JOIN vendor_table ve ON v.service_provider_id = ve.service_provider_id " +
                            "WHERE 1=1"
            );

            if (email != null && !email.isEmpty()) {
                sql.append(" AND LOWER(v.email) LIKE LOWER(:email)");
            }

            if (search != null && !search.isEmpty()) {
                sql.append(" AND (")
                        .append("LOWER(CONCAT(v.firstname, ' ', v.lastname)) LIKE LOWER(:search) ")
                        .append("OR LOWER(v.email) LIKE LOWER(:search) ")
                        .append("OR LOWER(ve.mobilenumber) LIKE LOWER(:search))");
            }

            if (profileStatus != null) {
                sql.append(" AND v.profile_status = :profileStatus");
            }

            if (approved != null) {
                sql.append(" AND v.approved = :approved");
            }

            if (planName != null && !planName.isEmpty()) {
                sql.append(" AND LOWER(v.plan_name) = LOWER(:planName)");
            }

            if (startDate != null && endDate != null) {
                sql.append(" AND v.created_at BETWEEN :startDate AND :endDate");
            }

            sql.append(" ORDER BY v.id DESC");

            Query query = entityManager.createNativeQuery(sql.toString(), VendorSubmissionEntity.class);

            if (email != null && !email.isEmpty()) {
                query.setParameter("email", "%" + email.toLowerCase() + "%");
            }

            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }

            if (profileStatus != null) {
                query.setParameter("profileStatus", profileStatus.ordinal());
            }

            if (approved != null) {
                query.setParameter("approved", approved);
            }

            if (planName != null && !planName.isEmpty()) {
                query.setParameter("planName", planName.toLowerCase());
            }

            if (startDate != null && endDate != null) {
                query.setParameter("startDate", startDate.atStartOfDay());
                query.setParameter("endDate", endDate.plusDays(1).atStartOfDay());
            }

            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<VendorSubmissionEntity> submissions = query.getResultList();

            // Count Query
            StringBuilder countSql = new StringBuilder(
                    "SELECT COUNT(*) FROM vendor_submission_details v " +
                            "LEFT JOIN vendor_table ve ON v.service_provider_id = ve.service_provider_id " +
                            "WHERE 1=1"
            );

            if (email != null && !email.isEmpty()) {
                countSql.append(" AND LOWER(v.email) LIKE LOWER(:email)");
            }

            if (search != null && !search.isEmpty()) {
                countSql.append(" AND (")
                        .append("LOWER(CONCAT(v.firstname, ' ', v.lastname)) LIKE LOWER(:search) ")
                        .append("OR LOWER(v.email) LIKE LOWER(:search) ")
                        .append("OR LOWER(ve.mobilenumber) LIKE LOWER(:search))");
            }

            if (profileStatus != null) {
                countSql.append(" AND v.profile_status = :profileStatus");
            }

            if (approved != null) {
                countSql.append(" AND v.approved = :approved");
            }

            if (planName != null && !planName.isEmpty()) {
                countSql.append(" AND LOWER(v.plan_name) = LOWER(:planName)");
            }

            if (startDate != null && endDate != null) {
                countSql.append(" AND v.created_at BETWEEN :startDate AND :endDate");
            }

            Query countQuery = entityManager.createNativeQuery(countSql.toString());

            if (email != null && !email.isEmpty()) {
                countQuery.setParameter("email", "%" + email.toLowerCase() + "%");
            }

            if (search != null && !search.isEmpty()) {
                countQuery.setParameter("search", "%" + search.toLowerCase() + "%");
            }

            if (profileStatus != null) {
                countQuery.setParameter("profileStatus", profileStatus.ordinal());
            }

            if (approved != null) {
                countQuery.setParameter("approved", approved);
            }

            if (planName != null && !planName.isEmpty()) {
                countQuery.setParameter("planName", planName.toLowerCase());
            }

            if (startDate != null && endDate != null) {
                countQuery.setParameter("startDate", startDate.atStartOfDay());
                countQuery.setParameter("endDate", endDate.plusDays(1).atStartOfDay());
            }

            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(submissions, pageable, total);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving submissions", e);
        }
    }




    public long countAllSubmissions() {
        return submissionRepository.count();
    }
}

