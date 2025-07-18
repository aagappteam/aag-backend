package aagapp_backend.controller.admin.privlidge;

import aagapp_backend.components.JwtUtil;
import aagapp_backend.components.cache.PrivilegeMappingCache;
import aagapp_backend.dto.CustomAdminDTO;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.Role;
import aagapp_backend.entity.admin.Privilege;
import aagapp_backend.repository.admin.CustomAdminRepository;
import aagapp_backend.repository.admin.PrivilegeRepository;
import aagapp_backend.repository.admin.RoleRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.RoleService;
import aagapp_backend.services.admin.CustomAdminSpecification;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import jakarta.servlet.http.HttpServletRequest;
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
        String token = jwtUtil.resolveToken(req);
        Long adminId = jwtUtil.extractAdminId(token);
        String adminName = roleService.findRoleName(Math.toIntExact(adminId));

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


/*    //  Create New Role
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        String token = jwtUtil.resolveToken(req);
        Long adminId = jwtUtil.extractAdminId(token);

        String roleName = request.get("roleName").toString();
        List<Integer> privilegeIds = (List<Integer>) request.get("privilegeIds");

        Set<Privilege> privileges = privilegeIds.stream()
                .map(id -> privilegeRepo.findById(Long.valueOf(id)).orElseThrow())
                .collect(Collectors.toSet());

        Role role = new Role();
        role.setRoleName(roleName);
        role.setPrivileges(privileges);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setCreatedBy("ADMIN_ID_" + adminId);

        roleRepo.save(role);

        return ResponseEntity.ok(Map.of("message", "Role created successfully", "roleId", role.getRoleId()));
    }*/

    //  Get All Roles
    @GetMapping
    public ResponseEntity<?> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "roleId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Integer roleId
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
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

            List<Privilege> privileges = role.getPrivileges().stream().toList();
            roleMap.put("privileges", privileges);

            // 🔍 Submenus grouped by parentMenu
            Map<String, List<Map<String, Object>>> groupedSubmenus = privileges.stream()
                    .filter(p -> "SUBMENU".equalsIgnoreCase(p.getType()))
                    .collect(Collectors.groupingBy(
                            Privilege::getParentMenu,
                            Collectors.mapping(p -> Map.of(
                                    "submenuId", p.getId(),
                                    "submenuName", p.getName()
                            ), Collectors.toList())
                    ));

            // 🔍 All parent menu names
            Set<String> parentMenuNames = groupedSubmenus.keySet();

            // ✅ Fetch actual MENU privileges by name
            List<Privilege> menuPrivileges = privilegeRepo.findAllByNameIn(parentMenuNames);

            // 🧩 Combine menu + submenus
            List<Map<String, Object>> menusList = menuPrivileges.stream()
                    .map(menu -> Map.of(
                            "menuId", menu.getId(),
                            "menuName", menu.getName(),
                            "submenus", groupedSubmenus.getOrDefault(menu.getName(), List.of())
                    ))
                    .collect(Collectors.toList());

            roleMap.put("menus", menusList);
            return roleMap;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("roles", rolesList);
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
            Long adminId = jwtUtil.extractAdminId(token);

            String mobile = request.get("mobile").toString();
            String userName = request.get("userName").toString();
            String password = request.get("password").toString();
            String countryCode = request.getOrDefault("countryCode", "+91").toString();

            Integer roleId = Integer.valueOf(request.get("roleId").toString());

            // Check if mobile exists
            Optional<CustomAdmin> existingUser = adminRepo.findByMobileNumber(mobile);
            if (existingUser.isPresent()) {
                return responseService.generateErrorResponse("Mobile number already exists", HttpStatus.BAD_REQUEST);
            }


            //  Fetch Role
            Role role = roleRepo.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

            //  Check if role is vendor or customer
            if (role.getRoleName().equalsIgnoreCase("VENDOR") || role.getRoleName().equalsIgnoreCase("CUSTOMER")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Cannot create VENDOR or CUSTOMER from this API"));
            }

            //  Save CustomAdmin (User)
            CustomAdmin user = new CustomAdmin();
            user.setMobileNumber(mobile);
            user.setUser_name(userName);
            user.setPassword(passwordEncoder.encode(password));
            user.setCountry_code(countryCode);
            user.setRole(role.getRoleId());
            user.setActive(1);
            user.setCreated_at(new Date());

            adminRepo.save(user);
            return responseService.generateSuccessResponse("User created successfully",user, HttpStatus.OK);


        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }


/*    @PostMapping("/add-user")
    public ResponseEntity<?> createUserWithRole(@RequestBody Map<String, Object> request, HttpServletRequest req) {
        try {
            String token = jwtUtil.resolveToken(req);
            Long adminId = jwtUtil.extractAdminId(token);

            String mobile = request.get("mobile").toString();
            String userName = request.get("userName").toString();
            String password = request.get("password").toString();
            String countryCode = request.getOrDefault("countryCode", "+91").toString();

            String roleName = request.get("roleName").toString();
            List<Integer> privilegeIds = (List<Integer>) request.get("privilegeIds");

            //  Check if mobile exists
            Optional<CustomAdmin> existingUser = adminRepo.findByMobileNumber(mobile);
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Mobile number already exists"));
            }

            //  Check if roleName is vendor or customer → throw error
            if (roleName.equalsIgnoreCase("VENDOR") || roleName.equalsIgnoreCase("CUSTOMER")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Cannot create VENDOR or CUSTOMER from this API"));
            }

            //  Create Role (or find if exists)
            Role role = roleRepo.findByRoleName(roleName)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleName(roleName);
                        newRole.setCreatedAt(LocalDateTime.now());
                        newRole.setUpdatedAt(LocalDateTime.now());
                        newRole.setCreatedBy("ADMIN_ID_" + adminId);
                        return newRole;
                    });

            //Attach Privileges
            Set<Privilege> privileges = privilegeIds.stream()
                    .map(id -> privilegeRepo.findById(Long.valueOf(id))
                            .orElseThrow(() -> new RuntimeException("Privilege ID not found: " + id)))
                    .collect(Collectors.toSet());

            role.setPrivileges(privileges);
            roleRepo.save(role);

            // 🔥 Save CustomAdmin (User)
            CustomAdmin user = new CustomAdmin();
            user.setMobileNumber(mobile);
            user.setUser_name(userName);
            user.setPassword(passwordEncoder.encode(password));
            user.setCountry_code(countryCode);
            user.setRole(role.getRoleId());
            user.setActive(1);
            user.setCreated_at(new Date());

            adminRepo.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "User created successfully",
                    "userId", user.getAdmin_id(),
                    "roleId", role.getRoleId()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
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

/*    @GetMapping("/custom-admins")
    public ResponseEntity<?> getCustomAdminsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String userName
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Specification<CustomAdmin> spec = Specification
                    .where(CustomAdminSpecification.hasRole(role))
                    .and(CustomAdminSpecification.hasMobileNumber(mobileNumber))
                    .and(CustomAdminSpecification.hasUserName(userName));

            Page<CustomAdmin> customAdminPage = adminRepo.findAll(spec, pageable);
            return responseService.generateSuccessResponseWithCount("Custom Admins fetched successfully", customAdminPage.getContent(),customAdminPage.getTotalElements(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }*/


    @GetMapping("/custom-admins")
    public ResponseEntity<?> getCustomAdminsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String userName
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "admin_id"));

            Integer roleId = null;
            if (roleName != null && !roleName.isBlank()) {
                roleId = roleRepository.findByRoleNameIgnoreCase(roleName)
                        .map(Role::getRoleId)
                        .orElse(null);
            }

            Specification<CustomAdmin> spec = Specification
                    .where(CustomAdminSpecification.hasRole(roleId))
                    .and(CustomAdminSpecification.hasMobileNumber(mobileNumber))
                    .and(CustomAdminSpecification.hasUserName(userName));

            Page<CustomAdmin> customAdminPage = adminRepo.findAll(spec, pageable);

            // Convert each CustomAdmin → CustomAdminDTO with roleName
            List<CustomAdminDTO> dtoList = customAdminPage.getContent().stream().map(admin -> {
                String resolvedRoleName = roleRepository.findById(admin.getRole())
                        .map(Role::getRoleName)
                        .orElse("Unknown");
                return new CustomAdminDTO(admin, resolvedRoleName);
            }).toList();

            return responseService.generateSuccessResponseWithCount(
                    "Custom Admins fetched successfully",
                    dtoList,
                    customAdminPage.getTotalElements(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }





}
