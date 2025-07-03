package aagapp_backend.controller.admin.privlidge;

import aagapp_backend.components.JwtUtil;
import aagapp_backend.components.cache.PrivilegeMappingCache;
import aagapp_backend.entity.Role;
import aagapp_backend.entity.SuccessResponse;
import aagapp_backend.entity.admin.Privilege;
import aagapp_backend.repository.admin.PrivilegeRepository;
import aagapp_backend.repository.admin.RoleRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.RoleService;
import aagapp_backend.services.admin.PrivilegeService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.referal.ReferralService;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/privileges")
public class PrivilegeController {

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    private PrivilegeMappingCache privilegeMappingCache;

    @Autowired
    private PrivilegeRepository privilegeRepo;
    @Autowired
    private RoleService roleService;
    @Autowired private JwtUtil jwtUtil;

    @Autowired
    private ResponseService responseService;

    @PostMapping("/reload-mapping")
    public String reloadMappings() {
        privilegeMappingCache.reload();
        return "Privilege Mappings Reloaded";
    }


    @PostMapping
    public ResponseEntity<?> createMenusSubmenus(@RequestBody Privilege privilege, HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        Long adminId = jwtUtil.extractAdminId(token);

        // Validate Name
        if (privilege.getName() == null || privilege.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Privilege name is required");
        }

        //  Validate Type
        if (privilege.getType() == null || (!privilege.getType().equalsIgnoreCase("MENU")
                && !privilege.getType().equalsIgnoreCase("SUBMENU"))) {
            throw new IllegalArgumentException("Type must be either MENU or SUBMENU");
        }

        //  If SUBMENU → Parent Menu must exist
        if (privilege.getType().equalsIgnoreCase("SUBMENU")) {
            if (privilege.getParentMenu() == null || privilege.getParentMenu().trim().isEmpty()) {
                throw new IllegalArgumentException("Parent menu is required for SUBMENU");
            }

            boolean parentExists = privilegeRepo.existsByNameAndType(privilege.getParentMenu(), "MENU");
            if (!parentExists) {
                throw new IllegalArgumentException("Parent menu does not exist");
            }
        } else {
            // For MENU → parentMenu should be null
            privilege.setParentMenu(null);
        }

        //  Check Duplicate
        Privilege existing = privilegeRepo.findByName(privilege.getName());
        if (existing != null) {
            throw new IllegalArgumentException("Privilege already exists with this name");
        }

        String roleName = roleService.findRoleName(Math.toIntExact(adminId));
        //  Save Privilege
        privilege.setCreatedBy(roleName);
         privilegeRepo.save(privilege);

        return responseService.generateSuccessResponse( "Privilege created successfully", privilege,HttpStatus.ACCEPTED);
    }


    @GetMapping
    public ResponseEntity<?> getAllPrivileges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Privilege> privilegePage = privilegeRepo.findAll(pageable);

        List<Privilege> privileges = privilegePage.getContent();

        // Grouping Menus and Submenus
        Map<String, List<String>> menuWithSubmenus = new HashMap<>();

        privileges.forEach(p -> {
            if ("MENU".equalsIgnoreCase(p.getType())) {
                menuWithSubmenus.putIfAbsent(p.getName(), new ArrayList<>());
            }
        });

        privileges.forEach(p -> {
            if ("SUBMENU".equalsIgnoreCase(p.getType()) && p.getParentMenu() != null) {
                menuWithSubmenus.computeIfAbsent(p.getParentMenu(), k -> new ArrayList<>())
                        .add(p.getName());
            }
        });

        Map<String, Object> response = new HashMap<>();
//        response.put("privileges", privileges);
        response.put("menuStructure", menuWithSubmenus);
        response.put("status", "OK");
        response.put("message", "Privileges fetched successfully");
        response.put("status_code", 200);
        response.put("currentPage", privilegePage.getNumber());
        response.put("totalItems", privilegePage.getTotalElements());
        response.put("totalPages", privilegePage.getTotalPages());
        response.put("size", privilegePage.getSize());


        return ResponseEntity.ok(response);
    }

    @GetMapping("/menus")
    public ResponseEntity<?> getAllMenusAndSubmenus() {
        List<Map<String, Object>> menus = privilegeService.getAllMenusAndSubmenus();
        return responseService.generateSuccessResponse("Privilege fetched successfully", menus, HttpStatus.OK);

    }


/*    @GetMapping("/menus")
    public ResponseEntity<?> getAllMenusAndSubmenus() {
        List<Privilege> privileges = privilegeRepo.findAll();

        Map<String, List<Map<String, Object>>> grouped = privileges.stream()
                .filter(p -> p.getType() != null && p.getType().equalsIgnoreCase("SUBMENU"))
                .collect(Collectors.groupingBy(
                        Privilege::getParentMenu,
                        Collectors.mapping(sub -> Map.of(
                                "submenuId", sub.getId(),
                                "submenuName", sub.getName()
                        ), Collectors.toList())
                ));

        List<Map<String, Object>> menus = privileges.stream()
                .filter(p -> p.getType() != null && p.getType().equalsIgnoreCase("MENU"))
                .map(menu -> Map.of(
                        "menuId", menu.getId(),
                        "menuName", menu.getName(),
                        "submenus", grouped.getOrDefault(menu.getName(), List.of())
                )).toList();

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Privilege fetched successfully",
                "data", Map.of("menus", menus)
        ));
    }*/

    @GetMapping("/menu-tree")
    public ResponseEntity<?> getMenuTree() {
        try{
            List<Privilege> privileges = privilegeRepo.findAll();

            // Group Menus
            List<Privilege> menus = privileges.stream()
                    .filter(p -> "MENU".equalsIgnoreCase(p.getType()))
                    .toList();

            // Group Submenus
            List<Privilege> submenus = privileges.stream()
                    .filter(p -> "SUBMENU".equalsIgnoreCase(p.getType()))
                    .toList();

            List<Map<String, Object>> menuList = new ArrayList<>();

            for (Privilege menu : menus) {
                Map<String, Object> menuMap = new HashMap<>();
                menuMap.put("menuName", menu.getName());

                List<String> submenuList = submenus.stream()
                        .filter(s -> menu.getName().equalsIgnoreCase(s.getParentMenu()))
                        .map(Privilege::getName)
                        .toList();

                menuMap.put("submenus", submenuList);

                menuList.add(menuMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("menus", menuList);

            return responseService.generateSuccessResponse( "Privilege fetched successfully", response,HttpStatus.OK);
        }catch (Exception e){
            exceptionHandlingImplement.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{id}")
    public Privilege get(@PathVariable Long id) {
        return privilegeRepo.findById(id).orElse(null);
    }



    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        privilegeRepo.deleteById(id);
        return "Deleted";
    }

    @PostMapping("/assign-privileges")
    public ResponseEntity<?> assignPrivilegesToRole(@RequestBody Map<String, Object> request) {
        try {
            Long roleId = Long.valueOf(request.get("roleId").toString());
            List<Integer> newPrivilegeIds = (List<Integer>) request.get("privilegeIds");

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            // Fetch current privileges
            Set<Privilege> currentPrivileges = role.getPrivileges();

            // Fetch new privileges to add
            Set<Privilege> newPrivileges = newPrivilegeIds.stream()
                    .map(id -> privilegeRepo.findById(Long.valueOf(id))
                            .orElseThrow(() -> new RuntimeException("Privilege not found: " + id)))
                    .collect(Collectors.toSet());

            // Merge
            currentPrivileges.addAll(newPrivileges);

            role.setPrivileges(currentPrivileges);
            role.setUpdatedAt(LocalDateTime.now());

            roleRepository.save(role);

            return responseService.generateSuccessResponse("Privileges updated successfully", role, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


}

