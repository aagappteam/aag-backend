package aagapp_backend.services;

import aagapp_backend.entity.*;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.game.PriceEntity;
import aagapp_backend.entity.payment.PlanEntity;
import aagapp_backend.enums.GameStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
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
import java.util.List;

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

// Insert predefined games if not already present
        if (entityManager.createQuery("SELECT COUNT(g) FROM AagAvailableGames g", Long.class).getSingleResult() == 0) {

            // Snakes & Ladders Ultimate game
            AagAvailableGames snakesAndLaddersGame = new AagAvailableGames();
            snakesAndLaddersGame.setGameName("Snakes & Ladders Ultimate");
            snakesAndLaddersGame.setGameImage("https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/snakes+and+leader.png");
            snakesAndLaddersGame.setMinRange(1);
            snakesAndLaddersGame.setMaxRange(100);
            snakesAndLaddersGame.setGameStatus(GameStatus.ACTIVE);
            entityManager.persist(snakesAndLaddersGame);

            // Insert themes for Snakes & Ladders Ultimate game
            List<ThemeEntity> snakeThemes = List.of(
                    new ThemeEntity("Standard", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/snakes+%26+ladder/snake+and+ladders-01.png", currentTimestamp),
                    new ThemeEntity("Forest", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/snakes+%26+ladder/forest+theame+%5BRecovered%5D-01.png", currentTimestamp),
                    new ThemeEntity("Ninja", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/snakes+%26+ladder/snake+nd+ladder+ice+theme+final-01.png", currentTimestamp),
                    new ThemeEntity("Underwater", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/snakes+%26+ladder/snake+ladders+under+water+final-01.png", currentTimestamp),
                    new ThemeEntity("Ice", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/snakes+%26+ladder/snake+nd+ladder+ice+theme+final-01.png", currentTimestamp),
                    new ThemeEntity("Heaven", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/snakes+%26+ladder/heaven+snake+ladders-01.png", currentTimestamp),
                    new ThemeEntity("Hell", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/snakes+%26+ladder/hell+snake+ladder+final+2-01.png", currentTimestamp)
            );

            for (ThemeEntity theme : snakeThemes) {
                theme.getGames().add(snakesAndLaddersGame); // Add Snakes & Ladders game to the theme's list of games
                entityManager.persist(theme);
            }

            // Insert prices for Snakes & Ladders Ultimate game
            List<Double> ludoPrices = List.of(3.0, 5.0, 7.0, 10.0, 25.0, 50.0);
            for (Double price : ludoPrices) {
                PriceEntity priceEntity = new PriceEntity();
                priceEntity.setPriceValue(price);
                priceEntity.getGames().add(snakesAndLaddersGame); // Add Snakes & Ladders game to the price's list of games
                entityManager.persist(priceEntity);
            }

            // Ludo game
            AagAvailableGames ludoGame = new AagAvailableGames();
            ludoGame.setGameName("Ludo");
            ludoGame.setGameImage("https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Ludo.png");
            ludoGame.setMinRange(1);
            ludoGame.setMaxRange(100);
            ludoGame.setGameStatus(GameStatus.ACTIVE);
            entityManager.persist(ludoGame);

            // Insert themes for Ludo game (Updated order and theme names)
            List<ThemeEntity> ludoThemes = List.of(
                    new ThemeEntity("Standard", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Standard+ludo+theme.png", currentTimestamp),
                    new ThemeEntity("Forest", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Jungle+Theme.png", currentTimestamp),
                    new ThemeEntity("Underwater", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Undewater+Ludo.png", currentTimestamp),
                    new ThemeEntity("Ice", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Ice+Ludo.png", currentTimestamp),
                    new ThemeEntity("Ninja", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Ninja+Ludo.png", currentTimestamp),
                    new ThemeEntity("Hell", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Hell+Ludo.png", currentTimestamp),
                    new ThemeEntity("Heaven", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Heaven+Ludo.png", currentTimestamp)
            );

            for (ThemeEntity theme : ludoThemes) {
                theme.getGames().add(ludoGame); // Add Ludo game to the theme's list of games
                entityManager.persist(theme);
            }

            // Insert prices for Ludo game
            List<Double> snakeladderludoPrices = List.of(3.0, 5.0, 7.0, 10.0, 25.0, 50.0);
            for (Double price : snakeladderludoPrices) {
                PriceEntity priceEntity = new PriceEntity();
                priceEntity.setPriceValue(price);
                priceEntity.getGames().add(ludoGame); // Add Ludo game to the price's list of games
                entityManager.persist(priceEntity);
            }

            System.out.println("Predefined games, themes, and prices inserted into the database.");
        }





/*        String alterQuery = "ALTER TABLE VendorEntity \n" +
                "ADD COLUMN themeCount INTEGER DEFAULT 3";
        Query query = entityManager.createNativeQuery(alterQuery);
        query.executeUpdate();*/
     /*   String ludo_game_roomsalterQuery = "ALTER TABLE ludo_game_rooms ADD COLUMN gamepassword VARCHAR(255)";
        Query ludo_game_roomsalterQueryquery = entityManager.createNativeQuery(ludo_game_roomsalterQuery);
        ludo_game_roomsalterQueryquery.executeUpdate();


        String ludo_league_roomsalterQuery = "ALTER TABLE ludo_league_rooms ADD COLUMN gamepassword VARCHAR(255)";
        Query ludo_league_roomsalterQueryquery = entityManager.createNativeQuery(ludo_league_roomsalterQuery);
        ludo_league_roomsalterQueryquery.executeUpdate();
*/




    }

}


