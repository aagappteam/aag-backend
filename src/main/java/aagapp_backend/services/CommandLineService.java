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

        if (entityManager.createQuery("SELECT COUNT(t) FROM ThemeEntity t", Long.class).getSingleResult() == 0) {

            // Adding some predefined themes
            ThemeEntity theme1 = new ThemeEntity();
            theme1.setName("Space Adventure");
            theme1.setDescription("A thrilling space-themed experience.");
            theme1.setImageUrl("https://example.com/images/space-adventure.jpg");
            theme1.setCreatedDate(java.sql.Timestamp.valueOf(currentTimestamp));
            theme1.setUpdatedDate(null);  // No updates yet

            ThemeEntity theme2 = new ThemeEntity();
            theme2.setName("Fantasy World");
            theme2.setDescription("A magical fantasy world with dragons and wizards.");
            theme2.setImageUrl("https://example.com/images/fantasy-world.jpg");
            theme2.setCreatedDate(java.sql.Timestamp.valueOf(currentTimestamp));
            theme2.setUpdatedDate(null);  // No updates yet

            ThemeEntity theme3 = new ThemeEntity();
            theme3.setName("Mystery Night");
            theme3.setDescription("A dark and mysterious world full of puzzles.");
            theme3.setImageUrl("https://example.com/images/mystery-night.jpg");
            theme3.setCreatedDate(java.sql.Timestamp.valueOf(currentTimestamp));
            theme3.setUpdatedDate(null);  // No updates yet

            // Persist the themes into the database
            entityManager.persist(theme1);
            entityManager.persist(theme2);
            entityManager.persist(theme3);

            System.out.println("Predefined themes inserted into the database.");
        } else {
            System.out.println("Themes table already populated.");
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
