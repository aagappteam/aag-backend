package aagapp_backend.services.admin;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.admin.AdminLogResponseDTO;
import aagapp_backend.dto.admin.AdminLogResponseWithCounts;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.admin.AdminLogs;
import aagapp_backend.repository.admin.AdminLogsInterface;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AdminLogService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;
    @Autowired
    private AdminLogsInterface adminLogsRepository;
    public void logAction(String activity,
                          String role,
                          String performedBy,
                          Long targetId,
                          String targetType) {
        AdminLogs log = new AdminLogs();
        log.setMessage(activity);
        log.setTargetRole(role);
        log.setPerformedBy(performedBy);
        log.setTargetId(targetId);
        log.setTargetType(targetType);
        log.setCreatedDate(ZonedDateTime.now());
        adminLogsRepository.save(log);
    }

    public Page<AdminLogs> getMyNotifications(Long userId, String role, Pageable pageable) {
        return adminLogsRepository.findAllVisibleToUser(userId, role, pageable);
    }

   /* @Transactional
    public Page<AdminLogs> getAllLogs(Pageable pageable, String roleName, String performedBy, String targetType, String search) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM admin_logs l WHERE 1=1");

            if (roleName != null && roleName.equalsIgnoreCase(Constant.ADMIN)) {
                sql.append(" AND LOWER(l.assignedRole) = LOWER(:roleName)");
            }




            if (performedBy != null && !performedBy.isEmpty()) {
                sql.append(" AND l.performed_by = :performedBy");
            }

            if (targetType != null && !targetType.isEmpty()) {
                sql.append(" AND l.target_type = :targetType");
            }

            if (search != null && !search.isEmpty()) {
                sql.append(" AND (l.activity ILIKE :search OR l.performed_by ILIKE :search OR l.target_type ILIKE :search)");
            }

            sql.append(" ORDER BY l.id DESC");

            Query query = entityManager.createNativeQuery(sql.toString(), AdminLogs.class);

            if (roleName != null && !roleName.isEmpty()) {
                query.setParameter("roleName", roleName);
            }
            if (performedBy != null && !performedBy.isEmpty()) {
                query.setParameter("performedBy", performedBy);
            }

            if (targetType != null && !targetType.isEmpty()) {
                query.setParameter("targetType", targetType);
            }

            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<AdminLogs> logs = query.getResultList();

            // Count Query
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM admin_logs l WHERE 1=1");

            if (roleName != null  && roleName.equalsIgnoreCase(Constant.ADMIN)) {
                countSql.append(" AND LOWER(l.assignedRole) = LOWER(:roleName)");
            }


            if (performedBy != null && !performedBy.isEmpty()) {
                countSql.append(" AND l.performed_by = :performedBy");
            }

            if (targetType != null && !targetType.isEmpty()) {
                countSql.append(" AND l.target_type = :targetType");
            }

            if (search != null && !search.isEmpty()) {
                countSql.append(" AND (l.activity ILIKE :search OR l.performed_by ILIKE :search OR l.target_type ILIKE :search)");
            }

            Query countQuery = entityManager.createNativeQuery(countSql.toString());

            if (roleName != null && !roleName.isEmpty()) {
                countQuery.setParameter("roleName", roleName);
            }

            if (performedBy != null && !performedBy.isEmpty()) {
                countQuery.setParameter("performedBy", performedBy);
            }

            if (targetType != null && !targetType.isEmpty()) {
                countQuery.setParameter("targetType", targetType);
            }

            if (search != null && !search.isEmpty()) {
                countQuery.setParameter("search", "%" + search + "%");
            }


            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(logs, pageable, total);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving admin logs", e);
        }
    }*/
/*   @Transactional
   public Page<AdminLogs> getAllLogs(Pageable pageable, String roleName, String performedBy, String targetType, String search) {
       try {
           StringBuilder sql = new StringBuilder("SELECT * FROM admin_logs l WHERE 1=1");

           if (roleName != null && !roleName.equalsIgnoreCase(Constant.ADMIN)) {
               sql.append(" AND LOWER(l.assignedRole) = LOWER(:roleName)");
           }

           if (performedBy != null && !performedBy.isEmpty()) {
               sql.append(" AND l.performed_by = :performedBy");
           }

           if (targetType != null && !targetType.isEmpty()) {
               sql.append(" AND l.targettype = :targetType");
           }

           if (search != null && !search.isEmpty()) {
               sql.append(" AND (l.activity ILIKE :search OR l.performed_by ILIKE :search OR l.targettype ILIKE :search)");
           }

           sql.append(" ORDER BY l.id DESC");

           Query query = entityManager.createNativeQuery(sql.toString(), AdminLogs.class);

           // Set parameters only if used in SQL
           if (roleName != null && !roleName.equalsIgnoreCase(Constant.ADMIN)) {
               query.setParameter("roleName", roleName);
           }
           if (performedBy != null && !performedBy.isEmpty()) {
               query.setParameter("performedBy", performedBy);
           }
           if (targetType != null && !targetType.isEmpty()) {
               query.setParameter("targetType", targetType);
           }
           if (search != null && !search.isEmpty()) {
               query.setParameter("search", "%" + search + "%");
           }

           query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
           query.setMaxResults(pageable.getPageSize());

           List<AdminLogs> logs = query.getResultList();

           // Count Query
           StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM admin_logs l WHERE 1=1");

           if (roleName != null && !roleName.equalsIgnoreCase(Constant.ADMIN)) {
               countSql.append(" AND LOWER(l.assignedRole) = LOWER(:roleName)");
           }
           if (performedBy != null && !performedBy.isEmpty()) {
               countSql.append(" AND l.performed_by = :performedBy");
           }
           if (targetType != null && !targetType.isEmpty()) {
               countSql.append(" AND l.targettype = :targetType");
           }
           if (search != null && !search.isEmpty()) {
               countSql.append(" AND (l.activity ILIKE :search OR l.performed_by ILIKE :search OR l.targettype ILIKE :search)");
           }

           Query countQuery = entityManager.createNativeQuery(countSql.toString());

           if (roleName != null && !roleName.equalsIgnoreCase(Constant.ADMIN)) {
               countQuery.setParameter("roleName", roleName);
           }
           if (performedBy != null && !performedBy.isEmpty()) {
               countQuery.setParameter("performedBy", performedBy);
           }
           if (targetType != null && !targetType.isEmpty()) {
               countQuery.setParameter("targetType", targetType);
           }
           if (search != null && !search.isEmpty()) {
               countQuery.setParameter("search", "%" + search + "%");
           }

           Long total = ((Number) countQuery.getSingleResult()).longValue();

           return new PageImpl<>(logs, pageable, total);
       } catch (Exception e) {
           // Don’t throw an error for empty results
           return new PageImpl<>(Collections.emptyList(), pageable, 0);
       }
   }*/


    public AdminLogResponseWithCounts getAllLogsWithCounts(Pageable pageable, String roleName, String performedBy, String targetType, String search) {
        try {
            String baseWhere = " FROM admin_logs l WHERE 1=1";

            StringBuilder filterSql = new StringBuilder(baseWhere);
            if (roleName != null && !roleName.equalsIgnoreCase(Constant.ADMIN)) {
                filterSql.append(" AND LOWER(l.assignedRole) = LOWER(:roleName)");
            }
            if (performedBy != null && !performedBy.isEmpty()) {
                filterSql.append(" AND l.performed_by = :performedBy");
            }
            if (targetType != null && !targetType.isEmpty()) {
                filterSql.append(" AND l.targettype = :targetType");
            }
            if (search != null && !search.isEmpty()) {
                filterSql.append(" AND (l.activity ILIKE :search OR l.performed_by ILIKE :search OR l.targettype ILIKE :search)");
            }

            // Main logs query
            String selectSql = "SELECT *" + filterSql + " ORDER BY l.id DESC";
            Query query = entityManager.createNativeQuery(selectSql, AdminLogs.class);
            setQueryParameters(query, roleName, performedBy, targetType, search);
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
            List<AdminLogs> logs = query.getResultList();

            // Total count
            Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*)" + filterSql);
            setQueryParameters(countQuery, roleName, performedBy, targetType, search);
            Long total = ((Number) countQuery.getSingleResult()).longValue();

            // Unread count
            Query unreadQuery = entityManager.createNativeQuery("SELECT COUNT(*)" + filterSql + " AND l.read = false");
            setQueryParameters(unreadQuery, roleName, performedBy, targetType, search);
            long unreadCount = ((Number) unreadQuery.getSingleResult()).longValue();

            long readCount = total - unreadCount;

            Page<AdminLogs> pageResult = new PageImpl<>(logs, pageable, total);
            return new AdminLogResponseWithCounts(pageResult, readCount, unreadCount);
        } catch (Exception e) {
            return new AdminLogResponseWithCounts(new PageImpl<>(Collections.emptyList(), pageable, 0), 0, 0);
        }
    }

    private void setQueryParameters(Query query, String roleName, String performedBy, String targetType, String search) {
        if (roleName != null && !roleName.equalsIgnoreCase(Constant.ADMIN)) {
            query.setParameter("roleName", roleName);
        }
        if (performedBy != null && !performedBy.isEmpty()) {
            query.setParameter("performedBy", performedBy);
        }
        if (targetType != null && !targetType.isEmpty()) {
            query.setParameter("targetType", targetType);
        }
        if (search != null && !search.isEmpty()) {
            query.setParameter("search", "%" + search + "%");
        }
    }


    public AdminLogResponseDTO mapToDto(AdminLogs log) {
        AdminLogResponseDTO dto = new AdminLogResponseDTO();

        dto.setId(log.getId());
        dto.setSenderId(log.getSenderid());
        dto.setSenderRole(log.getSenderrole());
        dto.setMessage(log.getMessage());
        dto.setTargetType(log.getTargetType());
        dto.setTargetId(log.getTargetId());
        dto.setTargetRole(log.getTargetRole());
        dto.setReceiverId(log.getReceiverId());
        dto.setAssignedRole(log.getAssignedRole());
        dto.setAssignedUserId(log.getAssignedUserId());
        dto.setAssignedBy(log.getAssignedBy());
        dto.setRead(log.isRead());
        dto.setPerformedBy(log.getPerformedBy());
        dto.setCreatedDate(log.getCreatedDate());
        dto.setUpdatedDate(log.getUpdatedDate());

        // Fetch sender details
        if ("VENDOR".equalsIgnoreCase(log.getSenderrole())) {
            VendorEntity vendor = vendorRepository.findById(log.getSenderid()).orElse(null);
            if (vendor != null) {
                dto.setName(vendor.getName());
                dto.setProfilePic(vendor.getProfilePic());
            }
        } else if ("USER".equalsIgnoreCase(log.getSenderrole())) {
            CustomCustomer customer = customCustomerRepository.findById(log.getSenderid()).orElse(null);
            if (customer != null) {
                dto.setName(customer.getName());
                dto.setProfilePic(customer.getProfilePic());
            }
        }

        return dto;
    }




    public void updateRead(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Log ID cannot be null");
        }

        AdminLogs log = adminLogsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Admin log not found with ID: " + id));

        if (!log.isRead()) {
            log.setRead(true);
            adminLogsRepository.save(log);
        }
    }


    public long countUnreadLogs(String roleName) {
        return adminLogsRepository.countUnreadLogsForRole(roleName);
    }

    public long countReadLogs(String roleName) {
        return adminLogsRepository.countReadLogsForRole(roleName);
    }


}
