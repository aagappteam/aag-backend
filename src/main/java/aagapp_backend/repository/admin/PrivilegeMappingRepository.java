package aagapp_backend.repository.admin;

import aagapp_backend.entity.admin.PrivilegeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface PrivilegeMappingRepository extends JpaRepository<PrivilegeMapping, Long> {
    Optional<PrivilegeMapping> findByApiPathAndMethod(String apiPath, String method);
}
