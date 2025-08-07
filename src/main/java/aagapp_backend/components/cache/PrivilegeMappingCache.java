package aagapp_backend.components.cache;

import aagapp_backend.entity.admin.PrivilegeMapping;
import aagapp_backend.repository.admin.PrivilegeMappingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PrivilegeMappingCache {

    @Autowired
    private PrivilegeMappingRepository mappingRepo;

    private Map<String, PrivilegeMapping> privilegeMap = new HashMap<>();

/*    @PostConstruct
    @Transactional
    public void loadMappings() {
        privilegeMap = mappingRepo.findAll().stream()
                .collect(Collectors.toMap(
                        m -> (m.getApiPath() + "|" + m.getMethod()).toUpperCase(),
                        m -> m
                ));
        System.out.println("🔄 Loaded " + privilegeMap.size() + " privilege mappings.");
    }

    public Optional<PrivilegeMapping> getMapping(String path, String method) {
        String key = (path + "|" + method).toUpperCase();
        return Optional.ofNullable(privilegeMap.get(key));
    }

    public void reload() {
        loadMappings();
        System.out.println("♻️ Privilege mappings reloaded.");
    }

    public int getTotalMappings() {
        return privilegeMap.size();
    }*/
}
