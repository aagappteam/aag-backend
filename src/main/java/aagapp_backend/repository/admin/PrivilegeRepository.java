package aagapp_backend.repository.admin;

import aagapp_backend.entity.admin.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Privilege findByNameAndTypeAndParentMenu(String privilegeName, String privilegeType, String parentMenu);

    Privilege findByName(String name);

    boolean existsByNameAndType(String parentMenu, String menu);
}
