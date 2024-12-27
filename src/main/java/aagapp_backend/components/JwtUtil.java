package aagapp_backend.components;

import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.RoleService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {
    private final ExceptionHandlingImplement exceptionHandling;
    private final RoleService roleService;
    private final CustomCustomerService customCustomerService;
    private final TokenBlacklist tokenBlacklist;
    private  EntityManager entityManager;

    private final String secretKeyString = "DASYWgfhMLL0np41rKFAGminD1zb5DlwDzE1WwnP8es=";
    private Key secretKey;

    @Autowired
    public JwtUtil(ExceptionHandlingImplement exceptionHandling,
                   RoleService roleService,
                   CustomCustomerService customCustomerService,
                   @Lazy TokenBlacklist tokenBlacklist
                   ) {
        this.exceptionHandling = exceptionHandling;
        this.roleService = roleService;
        this.customCustomerService = customCustomerService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @PostConstruct
    public void init() {
        try {
            // Use java.util.Base64 to decode the base64 string to a byte array
            byte[] secretKeyBytes = Base64.getDecoder().decode(secretKeyString);

            if (secretKeyBytes.length * 8 < 256) {
                throw new IllegalArgumentException("Key length is less than 256 bits.");
            }

            this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes); // Cache the key
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public String generateToken(Long id, Integer role, String ipAddress, String userAgent) {
        try {
            String uniqueTokenId = UUID.randomUUID().toString();

            boolean isMobile = isMobileDevice(userAgent);

            JwtBuilder jwtBuilder = Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setId(uniqueTokenId)
                    .claim("id", id)
                    .claim("role", role)
                    .claim("userAgent",userAgent)
                    .claim("ipAddress", ipAddress)
                    .setIssuedAt(new Date())
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256);

            if (!isMobile) {
//                jwtBuilder.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)); // 10 hours
            }

            return jwtBuilder.compact();

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    private boolean isMobileDevice(String userAgent) {
       try{
           String devicePattern = "android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini";
           return userAgent != null && userAgent.toLowerCase().matches(".*(" + devicePattern + ").*");
       }catch (Exception e){
           exceptionHandling.handleException(e);
           return false;
       }
    }


    private Key getSignInKey() {
        return this.secretKey;
    }


    public String extractUserAgent(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }


            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userAgent", String.class);

        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature.");
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error extracting userAgent from JWT token", e);
        }
    }


    public Long extractId(String token) {

        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }


            String userAgent = extractUserAgent(token);

            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("id", Long.class);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public Date getExpiryTime(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration();

        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(null, null, "Token is expired");
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    @Transactional
    public Boolean validateToken(String token, String ipAddress, String userAgent) {

        try {

            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }
            if (isTokenExpired(token,userAgent)) {
                throw new IllegalArgumentException("Token is expired");
            }

            Long id = extractId(token);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String tokenId = claims.getId();
            if (tokenBlacklist.isTokenBlacklisted(tokenId)) {
                return false;
            }
            int role=extractRoleId(token);
            CustomCustomer existingCustomer=null;
            VendorEntity existingServiceProvider=null;
            CustomAdmin existingAdmin=null;
            if(roleService.findRoleName(role).equals(Constant.roleUser)){
                existingCustomer = customCustomerService.readCustomerById(id);
                if (existingCustomer == null) {
                    return false;
                }
            }
            else if(roleService.findRoleName(role).equals(Constant.rolevendor)) {
                existingServiceProvider = entityManager.find(VendorEntity.class, id);
                if(existingServiceProvider==null)
                    return false;
            }
            else if(roleService.findRoleName(role).equals(Constant.SUPPORT))
            {
                existingAdmin= entityManager.find(CustomAdmin.class, id);
                if(existingAdmin==null)
                {
                    return false;
                }
            }

            String storedIpAddress = claims.get("ipAddress", String.class);


            return ipAddress.trim().equals(storedIpAddress != null ? storedIpAddress.trim() : "");
        } catch (ExpiredJwtException e) {
            logoutUser(token);
            return false;
        } catch (MalformedJwtException | IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return false;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }

    @Transactional
    private boolean isTokenExpired(String token, String userAgent) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            boolean isMobile = isMobileDevice(userAgent);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();

            if (isMobile && expiration == null) {
                return false; // Mobile token doesn't have expiration
            }
            Long id = this.extractId(token);

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerById(id);
            
            if(expiration!=null){
                if (existingCustomer != null) {
                    existingCustomer.setToken(null);
                    entityManager.persist(existingCustomer);
                } 

                return expiration != null && expiration.before(new Date());
            }
            
            return false;   



        } catch (ExpiredJwtException e) {
            logoutUser(token);
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Token is expired and cannot be used.");
        } catch (MalformedJwtException | SignatureException e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error checking token expiration", e);
        }
    }

    public boolean logoutUser(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {

                throw new IllegalArgumentException("Token is required");


            }
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            tokenBlacklist.blacklistToken(token,claims.getExpiration().getTime());
            return true;
        }catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }

    public Integer extractRoleId(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", Integer.class);

        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature.");
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error in JWT token", e);
        }
    }

}
