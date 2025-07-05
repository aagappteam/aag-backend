package aagapp_backend.repository.admin;

import aagapp_backend.entity.admin.AdminLogs;
import aagapp_backend.entity.invoice.InvoiceAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLogsInterface  extends JpaRepository<AdminLogs, Long> {


    @Query("SELECT al FROM AdminLogs al " +
            "WHERE (:roleName IS NULL OR al.role = :roleName) " +
            "AND (:performedBy IS NULL OR al.performedBy = :performedBy) " +
            "AND (:targetType IS NULL OR al.targetType = :targetType) " +
            "AND (:search IS NULL OR (" +
            "LOWER(al.activity) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(al.performedBy) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(al.targetType) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<AdminLogs> findWithFilters(
            @Param("roleName") String roleName, // still fine to keep the param named roleName
            @Param("performedBy") String performedBy,
            @Param("targetType") String targetType,
            @Param("search") String search,
            Pageable pageable
    );

}

