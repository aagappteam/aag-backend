package aagapp_backend.services;

import aagapp_backend.entity.*;
import aagapp_backend.entity.payment.PlanEntity;
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

       /* if (entityManager.createQuery("SELECT COUNT(t) FROM ThemeEntity t", Long.class).getSingleResult() == 0) {

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
        }*/

/*        if(entityManager.createQuery("SELECT count(e) FROM CustomAdmin e", Long.class).getSingleResult()==0)
        {
            entityManager.merge(new CustomAdmin(1L,2,passwordEncoder.encode("Admin#01"),"admin","7740066387","+91",0,currentTimestamp,"SUPER_ADMIN"));
            entityManager.merge(new CustomAdmin(2L,1,passwordEncoder.encode("SuperAdmin#1357"),"superadmin","9872548680","+91",0,currentTimestamp,"SUPER_ADMIN"));
        }*/
/*
       String alterQuery = "ALTER TABLE themes DROP COLUMN name";
        Query query = entityManager.createNativeQuery(alterQuery);
        query.executeUpdate();*/

/*       String alterQuery = "ALTER TABLE payments DROP COLUMN plan_name";
        Query query = entityManager.createNativeQuery(alterQuery);
        query.executeUpdate();*/


        // Insert predefined plans if not already present
      /*  if (entityManager.createQuery("SELECT COUNT(p) FROM Plan p", Long.class).getSingleResult() == 0) {
            PlanEntity plan1 = new PlanEntity();
            plan1.setPlanName("Standard");
            plan1.setPlanVariant("Monthly");
            plan1.setPrice(10000.0);
            plan1.setSubtitle("For individual users");
            plan1.setCreatedAt(currentTimestamp);
            plan1.setUpdatedAt(null);  // No updates yet

            PlanEntity plan2 = new PlanEntity();
            plan2.setPlanName("Standard");
            plan2.setPlanVariant("Yearly");
            plan2.setPrice(120000.0);
            plan2.setSubtitle("For individual users");
            plan2.setCreatedAt(currentTimestamp);
            plan2.setUpdatedAt(null);  // No updates yet

            PlanEntity plan3 = new PlanEntity();
            plan3.setPlanName("Pro");
            plan3.setPlanVariant("Monthly");
            plan3.setPrice(20000.0);
            plan3.setSubtitle("For professional teams");
            plan3.setCreatedAt(currentTimestamp);
            plan3.setUpdatedAt(null);  // No updates yet

            PlanEntity plan4 = new PlanEntity();
            plan4.setPlanName("Pro");
            plan4.setPlanVariant("Yearly");
            plan4.setPrice(240000.0);
            plan4.setSubtitle("For professional teams");
            plan4.setCreatedAt(currentTimestamp);
            plan4.setUpdatedAt(null);  // No updates yet

            PlanEntity plan5 = new PlanEntity();
            plan5.setPlanName("1M+ Elite");
            plan5.setPlanVariant("Monthly");
            plan5.setPrice(30000.0);
            plan5.setSubtitle("Perfect for growing teams");
            plan5.setCreatedAt(currentTimestamp);
            plan5.setUpdatedAt(null);  // No updates yet

            PlanEntity plan6 = new PlanEntity();
            plan6.setPlanName("1M+ Elite");
            plan6.setPlanVariant("Yearly");
            plan6.setPrice(360000.0);
            plan6.setSubtitle("Perfect for growing teams");
            plan6.setCreatedAt(currentTimestamp);
            plan6.setUpdatedAt(null);  // No updates yet

            // Persist the plans into the database
            entityManager.persist(plan1);
            entityManager.persist(plan2);
            entityManager.persist(plan3);
            entityManager.persist(plan4);
            entityManager.persist(plan5);
            entityManager.persist(plan6);

            System.out.println("Predefined plans inserted into the database.");
        }*/


        // Insert predefined plan features if not already present
       /* if (entityManager.createQuery("SELECT COUNT(pf) FROM PlanFeature pf", Long.class).getSingleResult() == 0) {
            // Fetch the plans first
            PlanEntity plan1 = entityManager.find(PlanEntity.class, 1L);  // Assuming plan1 has id = 1
            PlanEntity plan2 = entityManager.find(PlanEntity.class, 2L);  // Assuming plan2 has id = 2
            PlanEntity plan3 = entityManager.find(PlanEntity.class, 3L);  // Assuming plan3 has id = 3
            PlanEntity plan4 = entityManager.find(PlanEntity.class, 4L);  // Assuming plan4 has id = 4
            PlanEntity plan5 = entityManager.find(PlanEntity.class, 5L);  // Assuming plan5 has id = 5
            PlanEntity plan6 = entityManager.find(PlanEntity.class, 6L);  // Assuming plan6 has id = 6

            // Features for Plan 1 (100K+ Standard Monthly)
            entityManager.persist(new PlanFeature(plan1, "Must Have 100K+ Followers", "A requirement for individual users."));
            entityManager.persist(new PlanFeature(plan1, "Upto 4 Themes/Skins for the game", "Customization options for the game."));
            entityManager.persist(new PlanFeature(plan1, "Monthly Feature Slots for the games", "Monthly slots for featured games."));
            entityManager.persist(new PlanFeature(plan1, "Referral Bonus", "Referral program bonuses."));
            entityManager.persist(new PlanFeature(plan1, "Analytics Dashboard", "Access to analytics dashboard."));
            entityManager.persist(new PlanFeature(plan1, "Priority Support", "Access to premium support."));

            // Features for Plan 2 (100K+ Standard Yearly)
            entityManager.persist(new PlanFeature(plan2, "Must Have 100K+ Followers", "A requirement for individual users."));
            entityManager.persist(new PlanFeature(plan2, "Upto 4 Themes/Skins for the game", "Customization options for the game."));
            entityManager.persist(new PlanFeature(plan2, "Yearly Feature Slots for the games", "Yearly slots for featured games."));
            entityManager.persist(new PlanFeature(plan2, "Referral Bonus", "Referral program bonuses."));
            entityManager.persist(new PlanFeature(plan2, "Analytics Dashboard", "Access to analytics dashboard."));
            entityManager.persist(new PlanFeature(plan2, "Priority Support", "Access to premium support."));

            // Features for Plan 3 (500K+ Pro Monthly)
            entityManager.persist(new PlanFeature(plan3, "Must Have 500K+ Followers", "A requirement for professional teams."));
            entityManager.persist(new PlanFeature(plan3, "Upto 6 Themes/Skins for the game", "Customization options for the game."));
            entityManager.persist(new PlanFeature(plan3, "Daily/Monthly Feature Slots for the games", "Access to daily and monthly feature slots."));
            entityManager.persist(new PlanFeature(plan3, "Performance-Based Events Unlock", "Unlock events based on performance."));
            entityManager.persist(new PlanFeature(plan3, "Referral Bonus", "Referral program bonuses."));
            entityManager.persist(new PlanFeature(plan3, "Analytics Dashboard", "Access to analytics dashboard."));
            entityManager.persist(new PlanFeature(plan3, "Priority Support", "Access to premium support."));
            entityManager.persist(new PlanFeature(plan3, "Daily/ Weekly League Access option", "Access to daily and weekly leagues."));
            entityManager.persist(new PlanFeature(plan3, "Limited Time Tournament Access option", "Access to time-limited tournaments."));
            entityManager.persist(new PlanFeature(plan3, "Weekly/Monthly Promotional Activities", "Access to promotional activities."));

            // Features for Plan 4 (500K+ Pro Yearly)
            entityManager.persist(new PlanFeature(plan4, "Must Have 500K+ Followers", "A requirement for professional teams."));
            entityManager.persist(new PlanFeature(plan4, "Upto 6 Themes/Skins for the game", "Customization options for the game."));
            entityManager.persist(new PlanFeature(plan4, "Daily/Monthly Feature Slots for the games", "Access to daily and monthly feature slots."));
            entityManager.persist(new PlanFeature(plan4, "Performance-Based Events Unlock", "Unlock events based on performance."));
            entityManager.persist(new PlanFeature(plan4, "Referral Bonus", "Referral program bonuses."));
            entityManager.persist(new PlanFeature(plan4, "Analytics Dashboard", "Access to analytics dashboard."));
            entityManager.persist(new PlanFeature(plan4, "Priority Support", "Access to premium support."));
            entityManager.persist(new PlanFeature(plan4, "Daily/ Weekly League Access option", "Access to daily and weekly leagues."));
            entityManager.persist(new PlanFeature(plan4, "Limited Time Tournament Access option", "Access to time-limited tournaments."));
            entityManager.persist(new PlanFeature(plan4, "Weekly/Monthly Promotional Activities", "Access to promotional activities."));

            // Features for Plan 5 (1M+ Elite Monthly)
            entityManager.persist(new PlanFeature(plan5, "Must Have 1M+ Followers", "A requirement for growing teams."));
            entityManager.persist(new PlanFeature(plan5, "Updated Daily Game Publish Limit", "Increase in daily game publish limit."));
            entityManager.persist(new PlanFeature(plan5, "Upto 12 Themes/Skins for the game", "More customization options."));
            entityManager.persist(new PlanFeature(plan5, "Daily/Weekly/Monthly Feature Slots for the games", "Access to more feature slots."));
            entityManager.persist(new PlanFeature(plan5, "Daily/ Weekly/Monthly League Access option", "Access to leagues across different time frames."));
            entityManager.persist(new PlanFeature(plan5, "Daily/Weekly/Monthly Tournament Access option", "Access to tournaments in different time frames."));
            entityManager.persist(new PlanFeature(plan5, "Daily/Weekly/Monthly Promotional Activities", "Access to promotional activities across time frames."));
            entityManager.persist(new PlanFeature(plan5, "Special Events Access", "Access to special events."));
            entityManager.persist(new PlanFeature(plan5, "Referral Bonus", "Referral program bonuses."));
            entityManager.persist(new PlanFeature(plan5, "Analytics Dashboard", "Access to analytics dashboard."));
            entityManager.persist(new PlanFeature(plan5, "Customized Game invite link", "A personalized game invite link."));

            // Features for Plan 6 (1M+ Elite Yearly)
            entityManager.persist(new PlanFeature(plan6, "Must Have 1M+ Followers", "A requirement for growing teams."));
            entityManager.persist(new PlanFeature(plan6, "Updated Daily Game Publish Limit", "Increase in daily game publish limit."));
            entityManager.persist(new PlanFeature(plan6, "Upto 12 Themes/Skins for the game", "More customization options."));
            entityManager.persist(new PlanFeature(plan6, "Daily/Weekly/Monthly Feature Slots for the games", "Access to more feature slots."));
            entityManager.persist(new PlanFeature(plan6, "Daily/ Weekly/Monthly League Access option", "Access to leagues across different time frames."));
            entityManager.persist(new PlanFeature(plan6, "Daily/Weekly/Monthly Tournament Access option", "Access to tournaments in different time frames."));
            entityManager.persist(new PlanFeature(plan6, "Daily/Weekly/Monthly Promotional Activities", "Access to promotional activities across time frames."));
            entityManager.persist(new PlanFeature(plan6, "Special Events Access", "Access to special events."));
            entityManager.persist(new PlanFeature(plan6, "Referral Bonus", "Referral program bonuses."));
            entityManager.persist(new PlanFeature(plan6, "Analytics Dashboard", "Access to analytics dashboard."));
            entityManager.persist(new PlanFeature(plan6, "Customized Game invite link", "A personalized game invite link."));

            System.out.println("Predefined plan features inserted into the database.");
        }*/

/*        String alterQuery = "ALTER TABLE vendor_table \n" +
                "ADD COLUMN total_wallet_balance DOUBLE PRECISION DEFAULT 0.0,\n" +
                "ADD COLUMN total_participated_in_game_tournament_league INT DEFAULT 0;";



        Query query = entityManager.createNativeQuery(alterQuery);
        query.executeUpdate();*/
     /*   String alterQuery = "ALTER TABLE vendor_table \n" +
                "ADD COLUMN total_wallet_balance DOUBLE DEFAULT 0.0,\n" +
                "ADD COLUMN total_participated_in_game_tournament_league INT DEFAULT 0;";
        Query query = entityManager.createNativeQuery(alterQuery);
        query.executeUpdate();*/



    }

}
