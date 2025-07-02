package aagapp_backend.aop;

import aagapp_backend.components.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class RoleCheckAspect {

    @Autowired
    private JwtUtil jwtUtil;

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = jwtUtil.resolveToken(request);

        if (token == null || !jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid or Missing Token");
        }

        Integer userRole = jwtUtil.extractRoleId(token);
        boolean allowed = Arrays.stream(requireRole.value()).anyMatch(r -> r == userRole);

        if (!allowed) {
            throw new RuntimeException("Access Denied for Role");
        }

        return joinPoint.proceed();
    }
}

