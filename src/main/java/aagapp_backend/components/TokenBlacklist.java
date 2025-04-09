package aagapp_backend.components;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.RoleService;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Token blacklist.
 */
@Service
public class TokenBlacklist {
    private JwtUtil jwtUtil;
    private EntityManager em;
    private CustomCustomerService customCustomerService;

    @Autowired
    private RoleService roleService;

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();


    @Transactional
    public void blacklistToken(String token,Long exp) {
        try {
            blacklistedTokens.put(token, exp);
            Long id = jwtUtil.extractId(token);
            Integer role=jwtUtil.extractRoleId(token);
            if(roleService.findRoleName(role).equals(Constant.roleUser))
            {
                CustomCustomer existingCustomer = em.find(CustomCustomer.class,id);
                if (existingCustomer != null) {
                    existingCustomer.setToken(null);
                    em.merge(existingCustomer);
                } else {
                    throw new RuntimeException("Customer not found for the given token");
                }
            }
            else if(roleService.findRoleName(role).equals(Constant.rolevendor))
            {
                VendorEntity existintServiceProviderEntity = em.find(VendorEntity.class,id);
                if (existintServiceProviderEntity != null) {
                    existintServiceProviderEntity.setToken(null);
                    em.merge(existintServiceProviderEntity);
                } else {
                    throw new RuntimeException("Vendor not found for the given token");
                }
            }else{
                throw new RuntimeException("Invalid role");

            }
        } catch (ExpiredJwtException e) {
            jwtUtil.logoutUser(token);
            throw new ExpiredJwtException(null, null, "Token is expired");
        } catch (Exception e) {
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }


    public boolean isTokenBlacklisted(String token) {
        Long expirationTime = blacklistedTokens.get(token);

        if (expirationTime != null && expirationTime > System.currentTimeMillis()) {
            return true;
        } else {
            blacklistedTokens.remove(token);
            return false;
        }
    }

    // Scheduled task to clean expired tokens every 10 hours (36000000 milliseconds)
   /* @Scheduled(fixedRate = 36000000)
    public void cleanExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        int removedTokensCount = 0;

        Iterator<Map.Entry<String, Long>> iterator = blacklistedTokens.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            long expirationTime = entry.getValue();

            if (expirationTime < currentTime) {
                iterator.remove();
                removedTokensCount++;
            }
        }

        if (removedTokensCount > 0) {
            System.out.println("Cleaned up {} expired tokens from blacklist"+ removedTokensCount);
        } else {
            System.out.println("No expired tokens to clean up");
        }
    }*/
}
