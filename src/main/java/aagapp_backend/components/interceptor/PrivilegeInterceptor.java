package aagapp_backend.components.interceptor;

import aagapp_backend.components.JwtAuthenticationFilter;
import aagapp_backend.components.JwtUtil;
import aagapp_backend.components.cache.PrivilegeMappingCache;
import aagapp_backend.entity.Role;
import aagapp_backend.entity.admin.PrivilegeMapping;
import aagapp_backend.repository.admin.PrivilegeMappingRepository;
import aagapp_backend.repository.admin.RoleRepository;
import aagapp_backend.services.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class PrivilegeInterceptor implements HandlerInterceptor {
    @Autowired private PrivilegeMappingCache mappingCache;


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired private PrivilegeMappingRepository mappingRepo;
    @Autowired private RoleRepository roleRepo;

    @Autowired
    private ResponseService responseService;

    // ✅ Unsecured public paths (skip token + privilege check)
    private static final Set<String> UNSECURED_PATHS = Set.of(
            "/auth",
            "/account",
            "/otp",
            "/actuator",
            "/health",
            "/error",
            "/winning",
            "/test",
            "/files",
            "/aagdocument",
            "/swagger-ui.html",
            "/swagger-resources",
            "/v2/api-docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/images",
            "/webjars",
            "/initate-payment",
            "/.well-known/assetlinks.json",
            "/response",
            "/resp",
            "/enq",
            "/MerchantAcknowledgement",
            "/Bank",
            "/ws",
            "/ludo-websocket",
            "/payment/payout-callback"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (isUnsecuredUri(path)) {
            return true;
        }

        if (path.startsWith("/auth") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/ludo-websocket") ||
                path.startsWith("/ws") ||
                path.startsWith("/health") ||
                path.startsWith("/error")) {
            return true;
        }

        String token = jwtUtil.resolveToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            jwtAuthenticationFilter.respondWithUnauthorized(response, "Your token is unauthorized");


            return false;
        }

//        Long roleId = Long.valueOf(jwtUtil.extractRoleId(token));
        Integer roleId = Integer.valueOf(jwtUtil.extractRoleId(token));

        //  Skip privilege check for Vendor (4) and User (5)

        if (roleId == 4 || roleId == 5 || roleId == 1 || roleId == 2 ) {
            return true;
        }

        System.out.println("Path: " + path);
        System.out.println("Method: " + method);
        Optional<PrivilegeMapping> mapping = mappingCache.getMapping(path, method);


        System.out.println("mapping " + mapping);

        if (mapping.isEmpty()) {
            return true;
        }

        String requiredPrivilege = mapping.get().getPrivilegeName();
        System.out.println("requiredPrivilege " + requiredPrivilege);

        Role role = roleRepo.findById(roleId).orElseThrow();


        boolean hasPrivilege = role.getPrivileges().stream()
                .peek(p -> System.out.println("Role has privilege: " + p.getName()))
                .anyMatch(p -> {
                    System.out.println("Checking  " + p.getName() + " matches " + requiredPrivilege);
                    return p.getName().equalsIgnoreCase(requiredPrivilege);
                });

        System.out.println("Required Privilege: " + requiredPrivilege);
        System.out.println(" Has privilege: " + hasPrivilege);


        System.out.println("hasPrivilege " + hasPrivilege);

        if (!hasPrivilege) {
            jwtAuthenticationFilter.respondWithUnauthorized(response, "Access Denied");

            return false;
        }

        return true;
    }

    private boolean isUnsecuredUri(String requestURI) {
        return UNSECURED_PATHS.stream().anyMatch(requestURI::startsWith) || "/".equals(requestURI);
    }

}

