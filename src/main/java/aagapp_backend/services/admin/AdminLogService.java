package aagapp_backend.services.admin;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.admin.AdminLogs;
import aagapp_backend.repository.admin.AdminLogsInterface;
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
   @Transactional
   public Page<AdminLogs> getAllLogs(Pageable pageable, String roleName, String performedBy, String targetType, String search) {
       try {
           StringBuilder sql = new StringBuilder("SELECT * FROM admin_logs l WHERE 1=1");

           // Apply role filter only if not ADMIN
           if (roleName != null && !roleName.equalsIgnoreCase(Constant.ADMIN)) {
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
               countSql.append(" AND l.target_type = :targetType");
           }
           if (search != null && !search.isEmpty()) {
               countSql.append(" AND (l.activity ILIKE :search OR l.performed_by ILIKE :search OR l.target_type ILIKE :search)");
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


}
