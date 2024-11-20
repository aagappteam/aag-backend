package aagapp_backend.repository.privilege;
import aagapp_backend.entity.MethodPrivilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MethodPrivilegeRepository extends JpaRepository<MethodPrivilege, Integer> {

    Optional<MethodPrivilege> findByUriPatternAndHttpMethod(String uriPattern, String httpMethod);

    Optional<MethodPrivilege> findByPrivilegeName(String privilegeName);

    Optional<MethodPrivilege> findByCreatedBy(String createdBy);
}
