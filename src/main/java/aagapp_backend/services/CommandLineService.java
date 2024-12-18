package aagapp_backend.services;

import aagapp_backend.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;  // Import for LocalDateTime
import java.util.Date;          // Import for Date

@Component
public class CommandLineService implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // Get current timestamp
        LocalDateTime currentTimestamp = LocalDateTime.now(); // or use new Date() for java.util.Date

        if (entityManager.createQuery("SELECT COUNT(r) FROM Role r", Long.class).getSingleResult() == 0) {
            // Use current timestamp (LocalDateTime)
            entityManager.merge(new Role(1, "SUPPORT", currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
            entityManager.merge(new Role(2, "ADMIN", currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
            entityManager.merge(new Role(3, "ADMIN_VENDOR_PROVIDER", currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
            entityManager.merge(new Role(4, "VENDOR", currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
            entityManager.merge(new Role(5, "CUSTOMER", currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
        }

/*        if(entityManager.createQuery("SELECT count(e) FROM CustomAdmin e", Long.class).getSingleResult()==0)
        {
            entityManager.merge(new CustomAdmin(1L,2,passwordEncoder.encode("Admin#01"),"admin","7740066387","+91",0,currentTimestamp,"SUPER_ADMIN"));
            entityManager.merge(new CustomAdmin(2L,1,passwordEncoder.encode("SuperAdmin#1357"),"superadmin","9872548680","+91",0,currentTimestamp,"SUPER_ADMIN"));
        }*/

/*        String alterQuery = "ALTER TABLE service_vendor ALTER COLUMN token TYPE VARCHAR(512)";
        Query query = entityManager.createNativeQuery(alterQuery);
        query.executeUpdate();*/

    }
}
