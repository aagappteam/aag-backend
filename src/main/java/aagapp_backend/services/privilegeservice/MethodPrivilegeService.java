package aagapp_backend.services.privilegeservice;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.MethodPrivilege;
import aagapp_backend.repository.privilege.MethodPrivilegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.lang.constant.Constable;

@Service
public class MethodPrivilegeService {

    @Autowired
    private MethodPrivilegeRepository methodPrivilegeRepository;

    public MethodPrivilege createMethodPrivilege(MethodPrivilege methodPrivilege) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication != null ? authentication.getName() : Constant.rolesuperadmin;

        methodPrivilege.setCreatedBy(currentUser);
        methodPrivilege.setUpdatedBy(currentUser);
        return methodPrivilegeRepository.save(methodPrivilege);
    }

    public MethodPrivilege updateMethodPrivilege(int id, MethodPrivilege updatedMethodPrivilege) {
        MethodPrivilege existing = methodPrivilegeRepository.findById(id).orElseThrow(() -> new RuntimeException("MethodPrivilege not found"));

        existing.setUriPattern(updatedMethodPrivilege.getUriPattern());
        existing.setHttpMethod(updatedMethodPrivilege.getHttpMethod());
        existing.setPrivilegeName(updatedMethodPrivilege.getPrivilegeName());
        existing.setDescription(updatedMethodPrivilege.getDescription());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = authentication != null ? authentication.getName() :Constant.rolesuperadmin;

        existing.setUpdatedBy(currentUser);

        return methodPrivilegeRepository.save(existing);
    }
}
