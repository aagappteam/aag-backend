package aagapp_backend.components;

import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.RoleService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.FilterChain;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The type Jwt authentication filter.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
    private static final Pattern UNSECURED_URI_PATTERN = Pattern.compile(
            "^/api/v1/(account|otp|test|files/aagdocument/.+/[^/]+|swagger-ui.html|swagger-resources|v2/api-docs|images|webjars).*"
    );

    @Value("${apiKey}")
    private String apiKey;

    private JwtUtil jwtUtil;
    private CustomCustomerService customCustomerService;
    private RoleService roleService;
    private TokenBlacklist tokenBlacklist;
    private ExceptionHandlingImplement exceptionHandling;
    private EntityManager entityManager;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setTokenBlacklist(TokenBlacklist tokenBlacklist) {
        this.tokenBlacklist = tokenBlacklist;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/ludo-websocket")) {
            // Log the WebSocket request and skip JWT authentication
            logger.info("Bypassing JWT authentication for WebSocket handshake");
            chain.doFilter(request, response);
            return;
        }
        if (requestURI.startsWith("/ws") || requestURI.startsWith("/websocket")) {
            logger.info("Bypassing JWT filter for WebSocket/SockJS request: " + requestURI);
            chain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        try {


            if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")) {
                chain.doFilter(request, response);
                return;
            }

            if (isUnsecuredUri(requestURI) || bypassimages(requestURI)) {

                chain.doFilter(request, response);
                return;
            }



            if (isApiKeyRequiredUri(request) && validateApiKey(request)) {
                chain.doFilter(request, response);
                return;
            }

            boolean responseHandled = authenticateUser(request, response);
            if (!responseHandled) {
                chain.doFilter(request, response);
            } else {
                return;
            }



        } catch (ExpiredJwtException e) {
            handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is expired");
            return;

        } catch (MalformedJwtException e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid JWT token");
            exceptionHandling.handleException(e);
            return;

        } catch (Exception e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            exceptionHandling.handleException(e);
            return;
        }
    }

    private boolean bypassimages(String requestURI) {
        try {
            return UNSECURED_URI_PATTERN.matcher(requestURI).matches();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isApiKeyRequiredUri(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String path = requestURI.split("\\?")[0].trim();

        List<Pattern> bypassPatterns = Arrays.asList(
                Pattern.compile("^/api/v1/category-custom/get-products-by-category-id/\\d+$"),
                Pattern.compile("^/api/v1/category-custom/get-all-categories$")
        );

        boolean isBypassed = bypassPatterns.stream().anyMatch(pattern -> pattern.matcher(path).matches());
        return isBypassed;
    }

    private boolean validateApiKey(HttpServletRequest request) {
        String requestApiKey = request.getHeader("x-api-key");
        return apiKey.equals(requestApiKey);
    }

    private boolean isUnsecuredUri(String requestURI) {
        // Extract first segment after the leading '/'
        String firstSegment = "";
        if (requestURI != null && requestURI.length() > 1) {
            // Remove the leading '/'
            String trimmedUri = requestURI.substring(1);
            String[] segments = trimmedUri.split("/");
            if (segments.length > 0) {
                firstSegment = segments[0];
            }
        }

        return "/".equals(requestURI)
                || requestURI.startsWith("/account")
                || requestURI.startsWith("/actuator")
                || requestURI.startsWith("/winning")
                || requestURI.startsWith("/otp")
//                || "vendor".equals(firstSegment) // Only allow URIs that start with '/vendor/...'
                || requestURI.startsWith("/health")
                || requestURI.startsWith("/test")
                || requestURI.startsWith("/files/aagdocument/**")
                || requestURI.startsWith("/files/**")
                || requestURI.startsWith("/aagdocument/**")
                || requestURI.startsWith("/swagger-ui.html")
                || requestURI.startsWith("/swagger-resources")
                || requestURI.startsWith("/v2/api-docs")
                || requestURI.startsWith("/images")
                || requestURI.startsWith("/webjars")
                || requestURI.startsWith("/initate-payment")
                || requestURI.startsWith("/.well-known/assetlinks.json")
                || requestURI.startsWith("/response")
                || requestURI.startsWith("/resp")
                || requestURI.startsWith("/enq")
                || requestURI.startsWith("/MerchantAcknowledgement")
                || requestURI.startsWith("/Bank")

                ;
    }


/*    private boolean isUnsecuredUri(String requestURI) {

        return "/".equals(requestURI)
                || requestURI.startsWith("/account")
                || requestURI.startsWith("/winning")

                || requestURI.startsWith("/otp")
//                || requestURI.startsWith("/vendor")
                || requestURI.startsWith("/health")
                || requestURI.startsWith("/test")
                || requestURI.startsWith("/files/aagdocument/**")
                || requestURI.startsWith("/files/**")
                || requestURI.startsWith("/aagdocument/**")
                || requestURI.startsWith("/swagger-ui.html")
                || requestURI.startsWith("/swagger-resources")
                || requestURI.startsWith("/v2/api-docs")
                || requestURI.startsWith("/images")
                || requestURI.startsWith("/webjars")
                || requestURI.startsWith("/initate-payment") // Added leading slash
                || requestURI.startsWith("/.well-known/assetlinks.json") // Added leading slash
                || requestURI.startsWith("/response") // Added leading slash
                || requestURI.startsWith("/resp") // Added leading slash
                || requestURI.startsWith("/enq") // Added leading slash
                || requestURI.startsWith("/MerchantAcknowledgement") // Added leading slash
                || requestURI.startsWith("/Bank");

    }*/

    /*private boolean isUnsecuredUri(String requestURI) {
        return requestURI.startsWith("/api/v1/account")
                || requestURI.startsWith("/api/v1/winning")

                || requestURI.startsWith("/api/v1/otp")
                || requestURI.startsWith("/api/v1/health")
                || requestURI.startsWith("/api/v1/test")
                || requestURI.startsWith("/api/v1/files/aagdocument/**")
                || requestURI.startsWith("/api/v1/files/**")
                || requestURI.startsWith("/api/v1/aagdocument/**")
                || requestURI.startsWith("/api/v1/swagger-ui.html")
                || requestURI.startsWith("/api/v1/swagger-resources")
                || requestURI.startsWith("/api/v1/v2/api-docs")
                || requestURI.startsWith("/api/v1/images")
                || requestURI.startsWith("/api/v1/webjars")
                || requestURI.startsWith("/api/v1/initate-payment") // Added leading slash
                || requestURI.startsWith("/api/v1/.well-known/assetlinks.json") // Added leading slash
                || requestURI.startsWith("/api/v1/response") // Added leading slash
                || requestURI.startsWith("/api/v1/resp") // Added leading slash
                || requestURI.startsWith("/api/v1/enq") // Added leading slash
                || requestURI.startsWith("/api/v1/MerchantAcknowledgement") // Added leading slash
                || requestURI.startsWith("/api/v1/Bank"); // Added leading slash

    }*/


    private boolean authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            respondWithUnauthorized(response, "JWT token cannot be empty");
            return true;
        }

        String jwt = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
        Long id = jwtUtil.extractId(jwt);

        if (tokenBlacklist.isTokenBlacklisted(jwt)) {
            respondWithUnauthorized(response, "Token has been blacklisted");
            return true;
        }

        if (id == null) {
            respondWithUnauthorized(response, "Invalid details in token");
            return true;
        }

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        try {
            if (!jwtUtil.validateToken(jwt, ipAddress, userAgent)) {
                respondWithUnauthorized(response, "Invalid JWT token");
                return true;
            }
        } catch (ExpiredJwtException e) {
            jwtUtil.logoutUser(jwt);
            respondWithUnauthorized(response, "Token is expired");
            return true;
        }
       /* if (authenticateByRole(id, jwt, ipAddress, userAgent, response,request)) {
            return false;
        }
        return true;*/


        CustomCustomer customCustomer = null;
        VendorEntity serviceProvider = null;
        CustomAdmin cusomAdmin = null;

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.roleUser)) {
                customCustomer = customCustomerService.readCustomerById(id);
                if (customCustomer != null && jwtUtil.validateToken(jwt, ipAddress, userAgent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            customCustomer.getId(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return false;
                } else {
                    jwtUtil.logoutUser(jwt);
                    respondWithUnauthorized(response, "Invalid data provided for this customer");
                    return true;
                }
            } else if (roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.rolevendor)) {
                serviceProvider = entityManager.find(VendorEntity.class, id);
                if (serviceProvider != null && jwtUtil.validateToken(jwt, ipAddress, userAgent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            serviceProvider.getService_provider_id(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return false;
                } else {
                    respondWithUnauthorized(response, "Invalid data provided for this vendor");
                    return true;
                }
            } else if (roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.ADMIN) || roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.SUPER_ADMIN) || roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.roleAdminServiceProvider)) {
                cusomAdmin=entityManager.find(CustomAdmin.class,id);
                if (cusomAdmin != null && jwtUtil.validateToken(jwt, ipAddress, userAgent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            cusomAdmin.getAdmin_id(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return false;
                } else {
                    respondWithUnauthorized(response, "Invalid data provided for this user");
                    return true;
                }
            } else {
                respondWithUnauthorized(response, "Invalid data provided for this user");
                return true;
            }
        }
        return false;
    }

    private void respondWithUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"UNAUTHORIZED\",\"status_code\":401,\"message\":\"" + message + "\"}");
        response.getWriter().flush();
    }
    private void handleException(HttpServletResponse response, int statusCode, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(statusCode);
            response.setContentType("application/json");

            String status = getStatusMessage(statusCode);

            String jsonResponse = createJsonResponse(status, statusCode, message);

            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        }
    }

    // Helper method to map status codes to status strings
    private String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case HttpServletResponse.SC_BAD_REQUEST:
                return "BAD_REQUEST";
            case HttpServletResponse.SC_UNAUTHORIZED:
                return "UNAUTHORIZED";
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
            default:
                return "ERROR";
        }
    }
    private boolean authenticateByRole(Long id, String jwt, String ipAddress, String userAgent, HttpServletResponse response,HttpServletRequest request) throws IOException {
        String roleName = roleService.findRoleName(jwtUtil.extractRoleId(jwt));

        // Use if-else statements instead of switch-case for non-constant role names
        if (Constant.roleUser.equals(roleName)) {
            return authenticateCustomer(id, jwt, ipAddress, userAgent, response,request);
        } else if (Constant.rolevendor.equals(roleName)) {
            return authenticateVendor(id, jwt, ipAddress, userAgent, response,request);
        } else if (Constant.ADMIN.equals(roleName) || Constant.SUPER_ADMIN.equals(roleName) || Constant.roleAdminServiceProvider.equals(roleName)) {
            return authenticateAdmin(id, jwt, ipAddress, userAgent, response,request);
        } else {
            respondWithUnauthorized(response, "Invalid data provided for this user");
            return true;
        }
    }


    private boolean authenticateCustomer(Long id, String jwt, String ipAddress, String userAgent, HttpServletResponse response,HttpServletRequest request) throws IOException {
        CustomCustomer customCustomer = customCustomerService.readCustomerById(id);
        if (customCustomer != null && jwtUtil.validateToken(jwt, ipAddress, userAgent)) {
            setAuthentication(customCustomer.getId(),request);
            return true;
        } else {
            jwtUtil.logoutUser(jwt);
            respondWithUnauthorized(response, "Invalid data provided for this customer");
            return true;
        }
    }

    private boolean authenticateVendor(Long id, String jwt, String ipAddress, String userAgent, HttpServletResponse response,HttpServletRequest request) throws IOException {
        VendorEntity serviceProvider = entityManager.find(VendorEntity.class, id);
        if (serviceProvider != null && jwtUtil.validateToken(jwt, ipAddress, userAgent)) {
            setAuthentication(serviceProvider.getService_provider_id(),request);
            return true;
        } else {
            respondWithUnauthorized(response, "Invalid data provided for this vendor");
            return true;
        }
    }

    private boolean authenticateAdmin(Long id, String jwt, String ipAddress, String userAgent, HttpServletResponse response,HttpServletRequest request) throws IOException {
        CustomAdmin customAdmin = entityManager.find(CustomAdmin.class, id);
        if (customAdmin != null && jwtUtil.validateToken(jwt, ipAddress, userAgent)) {
            setAuthentication(customAdmin.getAdmin_id(),request);
            return true;
        } else {
            respondWithUnauthorized(response, "Invalid data provided for this admin");
            return true;
        }
    }

    private void setAuthentication(Long id, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(id, null, new ArrayList<>());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    // Helper method to construct the JSON response body
    private String createJsonResponse(String status, int statusCode, String message) {
        return String.format(
                "{\"status\":\"%s\",\"status_code\":%d,\"message\":\"%s\"}",
                status,
                statusCode,
                message
        );
    }


/*    private void handleException(HttpServletResponse response, int statusCode, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(statusCode);
            response.setContentType("application/json");

            String status = (statusCode == HttpServletResponse.SC_BAD_REQUEST) ? "BAD_REQUEST" :
                    (statusCode == HttpServletResponse.SC_UNAUTHORIZED) ? "UNAUTHORIZED" : "ERROR";

            String jsonResponse = String.format(
                    "{\"status\":\"%s\",\"status_code\":%d,\"message\":\"%s\"}",
                    status,
                    statusCode,
                    message
            );
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        }
    }*/


}