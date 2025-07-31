package aagapp_backend.controller.admin.privlidge;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.components.cache.PrivilegeMappingCache;
import aagapp_backend.dto.CustomAdminDTO;
import aagapp_backend.dto.admin.Role.AdminWithRoleResponse;
import aagapp_backend.dto.admin.Role.RoleDTO;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.Role;
import aagapp_backend.entity.admin.Privilege;
import aagapp_backend.repository.admin.CustomAdminRepository;
import aagapp_backend.repository.admin.PrivilegeRepository;
import aagapp_backend.repository.admin.RoleRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.RoleService;
import aagapp_backend.services.admin.AdminService;
import aagapp_backend.services.admin.CustomAdminSpecification;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/role")
public class RoleController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomAdminRepository customAdminRepository;

    @Autowired
    private AdminService adminService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeMappingCache privilegeMappingCache;

    @Autowired
    private CustomAdminRepository adminRepo;

    @Autowired
    private PrivilegeRepository privilegeRepo;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RoleRepository roleRepo;
    @Autowired private JwtUtil jwtUtil;

    @Autowired
    private ResponseService responseService;

    @PostMapping("/create")
    public ResponseEntity<?> createRole(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        String roleName = request.get("roleName").toString();
        if (roleName == null || roleName.isEmpty()) {
            return responseService.generateErrorResponse("Role name is required", HttpStatus.BAD_REQUEST);
        }
        String token = jwtUtil.resolveToken(req);
        Long adminId = jwtUtil.extractAdminId(token);
        String adminName = roleService.findRoleName(Math.toIntExact(adminId));


        if (!roleRepo.findAllByRoleNameIgnoreCase(roleName).isEmpty()) {
            return responseService.generateErrorResponse("Role with name '" + roleName + "' already exists.", HttpStatus.BAD_REQUEST);
        }


        List<Integer> privilegeIds = (List<Integer>) request.get("privilegeIds");

        Set<Privilege> privileges = privilegeIds.stream()
                .map(id -> privilegeRepo.findById(Long.valueOf(id))
                        .orElseThrow(() -> new RuntimeException("Privilege ID not found: " + id)))
                .collect(Collectors.toSet());

        Role role = new Role();
        role.setRoleName(roleName);
        role.setPrivileges(privileges);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setCreatedBy(adminName!=null?adminName:"ADMIN");

        roleRepo.save(role);
        return responseService.generateSuccessResponse("Role created successfully", role, HttpStatus.OK);


    }

    @GetMapping("/get-user")
    public ResponseEntity<?> getAdminById(@RequestHeader(value = "Authorization") String authorization) {
        String token = authorization.substring(7);
        Long userId = jwtUtil.extractId(token);

        Optional<CustomAdmin> optionalAdmin = customAdminRepository.findById(userId);

        if (optionalAdmin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "ERROR", "message", "Admin not found with ID: " + userId));
        }

        CustomAdmin admin = optionalAdmin.get();
        String roleName = "";

        Optional<Role> optionalRole = roleRepository.findById(admin.getRole());
        if (optionalRole.isPresent()) {
            roleName = optionalRole.get().getRoleName();
        }
        AdminWithRoleResponse response = new AdminWithRoleResponse(
                admin.getAdminId(),
                admin.getUser_name(),
                admin.getEmail(),
                admin.getMobileNumber(),
                admin.getCountry_code(),
                admin.getActive(),
                admin.getCreatedBy(),
                admin.getCreated_at(),
                admin.getUpdated_at(),
                roleName
        );

        return responseService.generateSuccessResponse("Admin found.", response, HttpStatus.OK);
    }





    @GetMapping("/all")
    public ResponseEntity<?> getAllRoles() {
        List<Role> roles = roleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<RoleDTO> roleDTOs = roles.stream()
                .map(role -> new RoleDTO(role.getRoleId(), role.getRoleName()))
                .collect(Collectors.toList());

        return responseService.generateSuccessResponse("Roles fetched successfully", roleDTOs, HttpStatus.OK);

    }

    //  Get All Roles
    @GetMapping
    public ResponseEntity<?> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Integer roleId
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Role> rolesPage;

        if (roleId != null) {
            Optional<Role> roleOptional = roleRepo.findById(roleId);
            rolesPage = roleOptional
                    .map(role -> new PageImpl<>(List.of(role), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(Collections.emptyList(), pageable, 0));
        } else {
            rolesPage = roleRepo.findAll(pageable);
        }

        List<Map<String, Object>> rolesList = rolesPage.getContent().stream().map(role -> {
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("roleId", role.getRoleId());
            roleMap.put("roleName", role.getRoleName());
            roleMap.put("createdAt", role.getCreatedAt());
            roleMap.put("updatedAt", role.getUpdatedAt());
            roleMap.put("createdBy", role.getCreatedBy());

            List<Privilege> privileges = new ArrayList<>(role.getPrivileges());

            // Group SUBMENUs by their parentMenu
            Map<String, List<Map<String, Object>>> groupedSubmenus = privileges.stream()
                    .filter(p -> "SUBMENU".equalsIgnoreCase(p.getType()))
                    .collect(Collectors.groupingBy(
                            p -> p.getParentMenu() != null ? p.getParentMenu().trim() : "",
                            Collectors.mapping(p -> Map.of(
                                    "submenuId", p.getId(),
                                    "submenuName", p.getName()
                            ), Collectors.toList())
                    ));

            // Fetch MENU privileges
            List<Privilege> menuPrivileges = privileges.stream()
                    .filter(p -> "MENU".equalsIgnoreCase(p.getType()))
                    .toList();

            // Combine MENU and corresponding SUBMENUs
            List<Map<String, Object>> menusList = menuPrivileges.stream()
                    .map(menu -> Map.of(
                            "menuId", menu.getId(),
                            "menuName", menu.getName(),
                            "submenus", groupedSubmenus.getOrDefault(menu.getName().trim(), List.of())
                    ))
                    .toList();

            roleMap.put("privileges", privileges);
            roleMap.put("menus", menusList);
            return roleMap;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("data", rolesList);
        response.put("currentPage", rolesPage.getNumber());
        response.put("totalItems", rolesPage.getTotalElements());
        response.put("totalPages", rolesPage.getTotalPages());
        response.put("size", rolesPage.getSize());
        response.put("sort", sortDir);

        return ResponseEntity.ok(response);
    }



    @PostMapping("/add-user")
    public ResponseEntity<?> createUserWithRole(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        try {
            String token = jwtUtil.resolveToken(req);

            String mobile = request.get("mobile").toString();
            String userName = request.get("userName").toString();
            String password = request.get("password").toString();
            String countryCode = request.getOrDefault("countryCode", "+91").toString();
            String email = request.getOrDefault("email", "").toString();


            Optional<CustomAdmin> existingUser = adminRepo.findByMobileNumber(mobile);
            if (existingUser.isPresent()) {
                return responseService.generateErrorResponse("Mobile number already exists", HttpStatus.BAD_REQUEST);
            }

            CustomAdmin user = new CustomAdmin();
            user.setMobileNumber(mobile);
            Long id = jwtUtil.extractId(token);

            String adminName = roleService.findRoleName(Math.toIntExact(id));


            user.setCreatedBy(adminName);

            user.setEmail(email);
            user.setUser_name(userName);
            user.setPassword(passwordEncoder.encode(password));
            user.setCountry_code(countryCode!=null?countryCode: Constant.COUNTRY_CODE);
            user.setActive(1);
            user.setCreated_at(new Date());

            adminRepo.save(user);
            return responseService.generateSuccessResponse("User created successfully", user, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assign-role")
    public ResponseEntity<?> assignRoleToUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer roleId = Integer.valueOf(request.get("roleId").toString());

            CustomAdmin user = adminRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setRole(roleId);

            adminRepo.save(user);

            return responseService.generateSuccessResponse("Role assigned successfully", user, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/status/{id}")
    public ResponseEntity<?> updateAdminStatus(
            @PathVariable("id") Long adminId,
            @RequestParam(name = "status") int status,
            @RequestParam(defaultValue = "system") String updatedBy) {
        try {
            boolean success = adminService.setAdminActiveStatus(adminId, status, updatedBy);

            String action = (status == 1) ? "re-activated" : "terminated";
            String message = success
                    ? "Admin " + action + " successfully."
                    : "Admin not found or already " + (status == 1 ? "active" : "terminated") + ".";

            return responseService.generateSuccessResponse(message, success, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }




/*    @PostMapping("/assign-roles")
    public ResponseEntity<?> assignRolesToUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            List<Integer> roleIds = (List<Integer>) request.get("roleIds");

            CustomAdmin user = adminRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Role> roles = roleRepository.findAllById(roleIds);
            user.setRoles(new HashSet<>(roles)); // Replace existing roles

            adminRepo.save(user);
            return responseService.generateSuccessResponse("Roles assigned successfully", user, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }*/




//    edit user and add privlidge

    @GetMapping("/menus-with-selected")
    public ResponseEntity<?> getMenusWithSelectedPrivileges(@RequestParam Integer roleId) {
        try {
            List<Privilege> allPrivileges = privilegeRepo.findAll();

            Role role = roleRepo.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            Set<Long> selectedPrivilegeIds = role.getPrivileges().stream()
                    .map(Privilege::getId)
                    .collect(Collectors.toSet());

            //  Step 3: Group submenus under parent menu
            Map<String, List<Map<String, Object>>> submenuMap = allPrivileges.stream()
                    .filter(p -> "SUBMENU".equalsIgnoreCase(p.getType()))
                    .collect(Collectors.groupingBy(
                            Privilege::getParentMenu,
                            Collectors.mapping(sub -> Map.of(
                                    "submenuId", sub.getId(),
                                    "submenuName", sub.getName(),
                                    "selected", selectedPrivilegeIds.contains(sub.getId())
                            ), Collectors.toList())
                    ));

            //  Step 4: Build final menu list
            List<Map<String, Object>> menus = allPrivileges.stream()
                    .filter(p -> "MENU".equalsIgnoreCase(p.getType()))
                    .map(menu -> Map.of(
                            "menuId", menu.getId(),
                            "menuName", menu.getName(),
                            "selected", selectedPrivilegeIds.contains(menu.getId()),
                            "submenus", submenuMap.getOrDefault(menu.getName(), List.of())
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "status_code", 200,
                    "message", "Menu privileges loaded successfully",
                    "data", menus
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

//    get all custom admin with pagination
@GetMapping("/custom-admins")
public ResponseEntity<?> getCustomAdminsWithPagination(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String roleName,
        @RequestParam(required = false) String mobileNumber,
        @RequestParam(required = false) String userName
) {
    try {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "adminId"));

            Integer roleId = null;
            if (roleName != null && !roleName.isBlank()) {
                // Convert roleName to Integer roleId
                Optional<Role> roleOptional = roleRepository.findByRoleNameIgnoreCase(roleName);
                if (roleOptional.isPresent()) {
                    roleId = roleOptional.get().getRoleId();
                } else {
                    // Return empty list if roleName is invalid
                    return responseService.generateSuccessResponseWithCount(
                            "No admins found for the given role name",
                            Collections.emptyList(),
                            0L,
                            HttpStatus.OK
                    );
                }
            }

        // Apply filtering
        Specification<CustomAdmin> spec = Specification
                .where(CustomAdminSpecification.hasRole(roleId))
                .and(CustomAdminSpecification.hasMobileNumber(mobileNumber))
                .and(CustomAdminSpecification.hasUserName(userName));

        Page<CustomAdmin> customAdminPage = adminRepo.findAll(spec, pageable);

        // Map entities to DTOs with resolved roleName
        List<CustomAdminDTO> dtoList = customAdminPage.getContent().stream().map(admin -> {
            String resolvedRoleName = "Unknown";
            if (admin.getRole() != null) {
                resolvedRoleName = roleRepository.findById(admin.getRole())
                        .map(Role::getRoleName)
                        .orElse("Unknown");
            }
            return new CustomAdminDTO(admin, resolvedRoleName);
        }).toList();


        return responseService.generateSuccessResponseWithCount(
                "Custom Admins fetched successfully",
                dtoList,
                customAdminPage.getTotalElements(),
                HttpStatus.OK
        );

    } catch (Exception e) {
        exceptionHandlingImplement.handleException(e);
        return responseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



/*
    @GetMapping("/custom-adminsold")
    public ResponseEntity<?> getCustomAdminsWithPaginationOld(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String userName
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "adminId"));

            Integer roleId = null;
            if (roleName != null && !roleName.isBlank()) {
                Optional<Role> roleOptional = roleRepository.findByRoleNameIgnoreCase(roleName);
                if (roleOptional.isPresent()) {
                    roleId = roleOptional.get().getRoleId();
                } else {
                    return responseService.generateSuccessResponseWithCount(
                            "No admins found for the given role name",
                            Collections.emptyList(),
                            0L,
                            HttpStatus.OK
                    );
                }
            }

            Specification<CustomAdmin> spec = Specification
                    .where(CustomAdminSpecification.hasRole(roleId))
                    .and(CustomAdminSpecification.hasMobileNumber(mobileNumber))
                    .and(CustomAdminSpecification.hasUserName(userName));

            Page<CustomAdmin> customAdminPage = adminRepo.findAll(spec, pageable);

            List<CustomAdminDTO> dtoList = customAdminPage.getContent().stream()
                    .map(CustomAdminDTO::new) // Use the DTO constructor directly
                    .toList();

            return responseService.generateSuccessResponseWithCount(
                    "Custom Admins fetched successfully",
                    dtoList,
                    customAdminPage.getTotalElements(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            exceptionHandlingImplement.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
*/

}
