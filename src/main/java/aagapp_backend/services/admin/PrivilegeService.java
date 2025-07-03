package aagapp_backend.services.admin;

import aagapp_backend.entity.admin.Privilege;
import aagapp_backend.repository.admin.PrivilegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PrivilegeService {

    @Autowired
    private PrivilegeRepository privilegeRepo;

    public List<Map<String, Object>> getStructuredMenuWithSubmenus(List<Privilege> privileges) {
        Map<String, List<Map<String, Object>>> grouped = privileges.stream()
                .filter(p -> "SUBMENU".equalsIgnoreCase(p.getType()))
                .collect(Collectors.groupingBy(
                        Privilege::getParentMenu,
                        Collectors.mapping(sub -> {
                            Map<String, Object> subMap = new LinkedHashMap<>();
                            subMap.put("submenuId", sub.getId());
                            subMap.put("submenuName", sub.getName());
                            return subMap;
                        }, Collectors.toList())
                ));

        return privileges.stream()
                .filter(p -> "MENU".equalsIgnoreCase(p.getType()))
                .map(menu -> {
                    Map<String, Object> menuMap = new LinkedHashMap<>();
                    menuMap.put("menuId", menu.getId());
                    menuMap.put("menuName", menu.getName());
                    menuMap.put("submenus", grouped.getOrDefault(menu.getName(), List.of()));
                    return menuMap;
                })
                .collect(Collectors.toList());
    }


    public List<Map<String, Object>> getAllMenusAndSubmenus() {
        List<Privilege> all = privilegeRepo.findAll();
        return getStructuredMenuWithSubmenus(all);
    }
}

