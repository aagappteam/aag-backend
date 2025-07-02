package aagapp_backend.controller.auth;

import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.Role;
import aagapp_backend.entity.admin.Privilege;
import aagapp_backend.repository.admin.RoleRepository;
import aagapp_backend.services.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepo;

    @GetMapping("/get-my-privileges")
    public ResponseEntity<?> getMyPrivileges(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        Integer roleId = jwtUtil.extractRoleId(token);

        String roleName = roleService.findRoleName(roleId);

        Role role = roleRepo.findById(Long.valueOf(roleId)).orElseThrow();

        List<String> privileges = role.getPrivileges().stream()
                .map(Privilege::getName)
                .toList();

        return ResponseEntity.ok(Map.of(
                "role", roleName,
                "privileges", privileges
        ));
    }


}
