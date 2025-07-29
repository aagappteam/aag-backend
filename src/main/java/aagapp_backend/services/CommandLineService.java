package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.*;
import aagapp_backend.entity.admin.Privilege;
import aagapp_backend.entity.admin.PrivilegeMapping;
import aagapp_backend.entity.faqs.FAQs;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.game.PriceEntity;
import aagapp_backend.entity.payment.PlanEntity;
import aagapp_backend.entity.ticket.PredefinedQA;
import aagapp_backend.enums.GameStatus;
import aagapp_backend.enums.TicketUserType;
import aagapp_backend.repository.game.PriceRepository;
import aagapp_backend.repository.game.ThemeRepository;
import aagapp_backend.services.faqs.FAQService;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;  // Import for LocalDateTime
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CommandLineService implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FAQService faqService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private ThemeRepository themeRepository;


    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // Get current timestamp
        LocalDateTime currentTimestamp = LocalDateTime.now(); // or use new Date() for java.util.Date

        if (entityManager.createQuery("SELECT COUNT(r) FROM Role r", Long.class).getSingleResult() == 0) {
            // Use current timestamp (LocalDateTime)
            entityManager.merge(new Role(1, Constant.SUPPORT, currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
            entityManager.merge(new Role(2, Constant.ADMIN, currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
            entityManager.merge(new Role(3, Constant.SUPER_ADMIN, currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
            entityManager.merge(new Role(4, Constant.VENDOR, currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
            entityManager.merge(new Role(5, Constant.CUSTOMER, currentTimestamp, currentTimestamp, "SUPER_ADMIN"));
        }

        if (entityManager.createQuery("SELECT COUNT(qa) FROM PredefinedQA qa", Long.class).getSingleResult() == 0) {
            List<PredefinedQA> qaList = List.of(
                    new PredefinedQA(null, "How can I play a game on the AAG App?", "To play a game on the AAG App, install the app and log in. Choose your favorite game from the game list and click on \"Play Now\".", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "How can I withdraw my winnings?", "To withdraw your winnings, go to the \"Wallet\" section, click on the \"Withdraw\" button, and enter your UPI ID or bank details.", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "Can I deposit money to play games?", "Yes, you can recharge your balance in the \"Wallet\" section. UPI, net banking, and cards are available as payment options.", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "What are reward points and how do I earn them?", "You earn reward points through daily login, referrals, and winning games. These can be redeemed in your wallet.", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "What should I do if the game crashes in the middle?", "If the game crashes due to a technical issue, contact the support team. If your balance or prize was affected, we will verify and process a refund.", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "What benefit do I get from referring a friend?", "If you refer a friend, both of you receive a bonus reward when they play a game for the first time.", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "What is the minimum withdrawal amount?", "The minimum withdrawal amount is ₹50q. Withdrawals below this amount are not allowed.", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "Can I play multiple games at the same time?", "No, only one game can be active at a time. Multiple sessions are not supported.", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "What if I accidentally deposit money into the wrong wallet?", "Please contact the support team immediately. If the transaction is valid and verified, a reversal is possible.", TicketUserType.CUSTOMER),
                    new PredefinedQA(null, "Where can I see the leaderboard and rankings?", "Go to the \"Leaderboard\" tab to see your ranking and other users’ rankings, game-wise or overall.", TicketUserType.CUSTOMER),

                    new PredefinedQA(null, "How can I publish my game on the AAG platform?", "Log in to the vendor dashboard, go to the \"Upload Game\" section, upload your game details and APK. The game will go live after team approval.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "What are the requirements to publish a game?", "To publish a game, you must provide the APK file, screenshots, game description, and privacy policy document.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "How can I view the revenue report for my game?", "In the vendor dashboard, go to the \"Analytics\" section to view revenue, user engagement, and play counts.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "How can I set up a bonus or reward system?", "Go to the \"Reward Settings\" tab to define the points users get for winning or losing a game.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "What happens if a user raises a complaint?", "If a complaint is validated, the AAG team will notify you. If needed, you will be required to fix or update your game.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "Is there a sandbox environment for game testing?", "Yes, during upload you can select the sandbox/test mode to test the game without making it live for users.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "When can I update my game?", "You can upload a new APK and updated details anytime and send an update request. The game will be updated after review.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "What is the revenue share model with AAG?", "You must follow a revenue-sharing agreement with AAG, which by default is a 70:30 model.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "What should I do if my game is rejected?", "If your game is rejected, the reason will be provided. You can make the required changes and resubmit it.", TicketUserType.VENDOR),
                    new PredefinedQA(null, "How many games can I upload?", "You can upload as many games as you want, but each game must pass quality approval.", TicketUserType.VENDOR)
            );

            for (PredefinedQA qa : qaList) {
                entityManager.persist(qa);
            }

            System.out.println("✅ Predefined QA inserted successfully.");
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
        if (entityManager.createQuery("SELECT COUNT(p) FROM PlanEntity p WHERE SIZE(p.features) > 0", Long.class).getSingleResult() == 0) {

            PlanEntity plan1 = entityManager.find(PlanEntity.class, 1L); // 100K+ Standard Monthly
            PlanEntity plan3 = entityManager.find(PlanEntity.class, 3L); // 500K+ Pro Monthly
            PlanEntity plan5 = entityManager.find(PlanEntity.class, 5L); // 1M+ Elite Monthly

            if (plan1 != null) {
                plan1.setFeatures(List.of(
                        "Must Have 100K+ Followers",
                        "Upto 2 Themes/Skins for the game",
                        "5 daily game publish limit",
                        "Monthly Feature Slots for the games",
                        "Performance-Based Event Unlock",
                        "Referral Bonus",
                        "Analytics Dashboard",
                        "Priority Support",
                        "Unique Game invite link",
                        "No Dedicated Relationship Manager",
                        "No Daily/ Weekly Promotional Activities",
                        "No Event Customization Option",
                        "Limited Games Access"
                ));
                entityManager.merge(plan1);
            }

            if (plan3 != null) {
                plan3.setFeatures(List.of(
                        "Must Have 500K+ Followers",
                        "Upto 4 Themes/Skins for the game",
                        "10 daily game publish limit",
                        "Updated Daily Game Publish Limit",
                        "Daily/Monthly Feature Slots for the games",
                        "Performance-Based Events Unlock",
                        "Referral Bonus",
                        "Analytics Dashboard",
                        "Priority Support",
                        "Unique Game invite link",
                        "Daily/ Weekly League Access option",
                        "Weekly/Monthly Promotional Activities",
                        "Updated Games Access",
                        "Limited Event Customization Options",
                        "No Dedicated Relationship Manager"
                ));
                entityManager.merge(plan3);
            }

            if (plan5 != null) {
                plan5.setFeatures(List.of(
                        "Must Have 1M+ Followers",
                        "Upto 7 Themes/Skins for the game",
                        "15 daily game publish limit",
                        "Updated Daily Game Publish Limit",
                        "Daily/Weekly/Monthly Feature Slots for the games",
                        "Daily/ Weekly/Monthly League Access option",
                        "Daily/Weekly/Monthly Tournament Access option",
                        "Daily/Weekly/Monthly Promotional Activities",
                        "Special Events Access",
                        "Referral Bonus",
                        "Analytics Dashboard",
                        "Customized Game invite link",
                        "All Platform Games Access",
                        "Priority Access to Beta Features",
                        "Additional Event Customization Options",
                        "Dedicated Relationship Manager"
                ));
                entityManager.merge(plan5);
            }

            System.out.println("✅ Predefined plan features added to plan entities.");
        }



// Insert predefined games if not already present
        if (entityManager.createQuery("SELECT COUNT(g) FROM AagAvailableGames g", Long.class).getSingleResult() == 0) {

            // --- Create Snakes & Ladders Game ---
            AagAvailableGames snakesAndLaddersGame = new AagAvailableGames();
            snakesAndLaddersGame.setGameName("Snakes & Ladders Ultimate");
            snakesAndLaddersGame.setGameImage("https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/snakes+and+leader.png");
            snakesAndLaddersGame.setMinRange(1);
            snakesAndLaddersGame.setMaxRange(100);
            snakesAndLaddersGame.setGameStatus(GameStatus.ACTIVE);

            // Themes for Snakes & Ladders
            List<ThemeEntity> snakeThemes = List.of(
                    new ThemeEntity("Standard", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/snakes+%26+ladder/Standard+Snakes+%26+Ladder.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Snakes.png", currentTimestamp),
                    new ThemeEntity("Ice", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/snakes+%26+ladder/Ice+Snakes+%26+Ladder.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Game+Icons/Group+1000005295.png", currentTimestamp),
                    new ThemeEntity("Underwater", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/snakes+%26+ladder/Under+water+Snakes+%26+Ladder.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Artboard+4.png", currentTimestamp),
                    new ThemeEntity("Forest", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/snakes+%26+ladder/Jungle+Snakes+%26+Ladder.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Jungle+snakes+%26+Ladder+(2).png", currentTimestamp),
                    new ThemeEntity("Ninja", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/snakes+%26+ladder/snakes+%26+ladder+final-01.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Ninja+Snakes+and+ladder.png", currentTimestamp),
                    new ThemeEntity("Hell", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/snakes+%26+ladder/Hell+Snakes+%26+Ladder.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Hell+snake+%26+ladder.png", currentTimestamp),
                    new ThemeEntity("Heaven", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/snakes+%26+ladder/Heaven+Snakes+%26+Ladder.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Heaven+snakes+%26+ladder.png", currentTimestamp)
            );

            for (ThemeEntity theme : snakeThemes) {
                theme.getGames().add(snakesAndLaddersGame);
            }

            themeRepository.saveAll(snakeThemes);
            snakesAndLaddersGame.setThemes(snakeThemes);

            List<Double> snakePrices = List.of(3.0, 5.0, 7.0, 10.0, 25.0, 50.0);
            List<PriceEntity> snakePriceEntities = new ArrayList<>();
            for (Double priceValue : snakePrices) {
                PriceEntity price = new PriceEntity();
                price.setPriceValue(priceValue);
                price.getGames().add(snakesAndLaddersGame);
                snakePriceEntities.add(price);
            }

            priceRepository.saveAll(snakePriceEntities);
            snakesAndLaddersGame.setPrice(snakePriceEntities);

            entityManager.persist(snakesAndLaddersGame);

            // --- Create Ludo Game ---
            AagAvailableGames ludoGame = new AagAvailableGames();
            ludoGame.setGameName("Ludo");
            ludoGame.setGameImage("https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Ludo.png");
            ludoGame.setMinRange(1);
            ludoGame.setMaxRange(100);
            ludoGame.setGameStatus(GameStatus.ACTIVE);

            // Themes for Ludo
            List<ThemeEntity> ludoThemes = List.of(
                    new ThemeEntity("Standard", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Standard+ludo+theme.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/ludo.png", currentTimestamp),
                    new ThemeEntity("Ice", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Ice+Ludo.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Game+Icons/iceludo+icon.png", currentTimestamp),
                    new ThemeEntity("Underwater", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Undewater+Ludo.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Game+Icons/Underwater+Ludo.png", currentTimestamp),
                    new ThemeEntity("Forest", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Jungle+Theme.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Jungle+ludo+(2).png", currentTimestamp),
                    new ThemeEntity("Ninja", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Ninja+Ludo.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/Ninja+Ludo.png", currentTimestamp),
                    new ThemeEntity("Hell", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Hell+Ludo.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/hell+ludo+(2).png", currentTimestamp),
                    new ThemeEntity("Heaven", "https://aag-data.s3.ap-south-1.amazonaws.com/all+themes/all+themes/ludo/Heaven+Ludo.png", "https://aag-data.s3.ap-south-1.amazonaws.com/game-folder/heaven+ludo+(2).png", currentTimestamp)
            );

            for (ThemeEntity theme : ludoThemes) {
                theme.getGames().add(ludoGame);
            }

            themeRepository.saveAll(ludoThemes);
            ludoGame.setThemes(ludoThemes);

            List<Double> ludoPrices = List.of(3.0, 5.0, 7.0, 10.0, 25.0, 50.0);
            List<PriceEntity> ludoPriceEntities = new ArrayList<>();
            for (Double priceValue : ludoPrices) {
                PriceEntity price = new PriceEntity();
                price.setPriceValue(priceValue);
                price.getGames().add(ludoGame);
                ludoPriceEntities.add(price);
            }

            priceRepository.saveAll(ludoPriceEntities);
            ludoGame.setPrice(ludoPriceEntities);

            entityManager.persist(ludoGame);

            System.out.println("Predefined games, themes, and prices inserted into the database.");
        }

        if (entityManager.createQuery("SELECT COUNT(p) FROM CustomAdmin p", Long.class).getSingleResult() == 0) {
            // Fetch role from DB using role name or ID
            Role role = entityManager.createQuery("SELECT r FROM Role r WHERE r.roleName = :roleName", Role.class)
                    .setParameter("roleName", "ADMIN")
                    .getSingleResult();

            // Admin 1
            CustomAdmin admin1 = new CustomAdmin();
            admin1.setRole(role.getRoleId());
            admin1.setPassword("1234");
            admin1.setUser_name("Rajendra Gupta");
            admin1.setEmail("no-reply@aagapp.com");
            admin1.setOtp("0000");
            admin1.setMobileNumber(Constant.MOBILE_6306470701);
            admin1.setCountry_code("+91");
            admin1.setToken(null);
            admin1.setActive(1);
            admin1.setCreated_at(new Date());
            admin1.setUpdated_at(null);

            entityManager.persist(admin1);

            // Admin 2
            CustomAdmin admin2 = new CustomAdmin();
            admin2.setRole(role.getRoleId());
            admin2.setPassword("1234");
            admin2.setUser_name("Shivang Singh");
            admin2.setEmail("nadeem@aagapp.com");
            admin2.setOtp("0000");
            admin2.setMobileNumber(Constant.MOBILE_9628577197);
            admin2.setCountry_code("+91");
            admin2.setToken(null);
            admin2.setActive(1);
            admin2.setCreated_at(new Date());
            admin2.setUpdated_at(null);

            entityManager.persist(admin2);
        }




        // ✅ Insert Privileges if empty
        if (entityManager.createQuery("SELECT COUNT(p) FROM Privilege p", Long.class).getSingleResult() == 0) {


            // User Management Menu & Submenus
            entityManager.merge(new Privilege(1L, "ACCESS_USER_MANAGEMENT", "MENU", null, "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(2L, "VIEW_USER", "SUBMENU", "ACCESS_USER_MANAGEMENT", "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(3L, "EDIT_USER", "SUBMENU", "ACCESS_USER_MANAGEMENT", "ADMIN", currentTimestamp));

            // Vendor Management Menu & Submenus
            entityManager.merge(new Privilege(4L, "ACCESS_VENDOR_MANAGEMENT", "MENU", null, "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(5L, "VIEW_VENDOR", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(6L, "EDIT_VENDOR", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(7L, "VIEW_KYC", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "ADMIN", currentTimestamp));

            // Finance Menu & Submenus
            entityManager.merge(new Privilege(8L, "ACCESS_FINANCE_MENU", "MENU", null, "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(9L, "VIEW_WITHDRAW", "SUBMENU", "ACCESS_FINANCE_MENU", "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(10L, "DOWNLOAD_WITHDRAW", "SUBMENU", "ACCESS_FINANCE_MENU", "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(11L, "VIEW_TRANSACTIONS", "SUBMENU", "ACCESS_FINANCE_MENU", "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(12L, "DOWNLOAD_TRANSACTIONS", "SUBMENU", "ACCESS_FINANCE_MENU", "ADMIN", currentTimestamp));

            // Games Management Menu & Submenus
            entityManager.merge(new Privilege(13L, "ACCESS_GAMES_MANAGEMENT", "MENU", null, "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(14L, "VIEW_GAMES", "SUBMENU", "ACCESS_GAMES_MANAGEMENT", "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(15L, "VIEW_LEAGUES", "SUBMENU", "ACCESS_GAMES_MANAGEMENT", "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(16L, "VIEW_TOURNAMENTS", "SUBMENU", "ACCESS_GAMES_MANAGEMENT", "ADMIN", currentTimestamp));

            // Home & Sub Features
            entityManager.merge(new Privilege(17L, "Home", "MENU", null, "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(18L, "ROLES", "MENU", null, "ADMIN", currentTimestamp));
            entityManager.merge(new Privilege(19L, "Subscriptions", "MENU", null, "ADMIN", currentTimestamp));

            // EMPTY Role entries (limited access menus)
            entityManager.merge(new Privilege(20L, "ACCESS_BLOCK_MENU", "SUBMENU", "ACCESS_USER_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(21L, "ACCESS_SUSPEND_MENU", "SUBMENU", "ACCESS_USER_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(22L, "ACCESS_VENDOR_REQUEST_MENU", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(23L, "ACCESS_VENDOR_JOURNEY_MENU", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "EMPTY", currentTimestamp));

            entityManager.merge(new Privilege(24L, "Invoice", "MENU", null, "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(25L, "Support Ticket", "MENU", null, "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(26L, "FAQ", "MENU", null, "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(27L, "Analytics & Reporting", "MENU", null, "EMPTY", currentTimestamp));

            entityManager.merge(new Privilege(28L, "User Transactions", "SUBMENU", "ACCESS_USER_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(29L, "Vendors Request", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(30L, "Vendor journey", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(31L, "Vendors Earning", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(32L, "Vendors Transactions", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "EMPTY", currentTimestamp));

            entityManager.merge(new Privilege(33L, "User Withdrawal Request", "SUBMENU", "ACCESS_USER_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(34L, "Vendors Approval Request", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(35L, "Vendors", "SUBMENU", "ACCESS_VENDOR_MANAGEMENT", "EMPTY", currentTimestamp));

            entityManager.merge(new Privilege(36L, "Kyc Request", "MENU", null, "EMPTY", currentTimestamp));
            entityManager.merge(new Privilege(37L, "Withdraw Request", "MENU", null, "EMPTY", currentTimestamp));
        }


        //  Insert API Privilege Mapping if empty
        if (entityManager.createQuery("SELECT COUNT(p) FROM PrivilegeMapping p", Long.class).getSingleResult() == 0) {
            entityManager.merge(new PrivilegeMapping(null, "/withdraw/view", "GET", "VIEW_WITHDRAW"));
            entityManager.merge(new PrivilegeMapping(null, "/withdraw/download", "GET", "DOWNLOAD_WITHDRAW"));
            entityManager.merge(new PrivilegeMapping(null, "/transactions/view", "GET", "VIEW_TRANSACTIONS"));
            entityManager.merge(new PrivilegeMapping(null, "/transactions/download", "GET", "DOWNLOAD_TRANSACTIONS"));
            entityManager.merge(new PrivilegeMapping(null, "/tickets/view", "GET", "VIEW_TICKETS"));
            entityManager.merge(new PrivilegeMapping(null, "/adminreview/resolve-ticket", "POST", "RESOLVE_TICKETS"));
            entityManager.merge(new PrivilegeMapping(null, "/customer/get-all-customers", "GET", "VIEW_USER"));
            entityManager.merge(new PrivilegeMapping(null, "/customer/update", "PUT", "EDIT_USER"));
            entityManager.merge(new PrivilegeMapping(null, "/vendor/get-all-vendors", "GET", "VIEW_VENDOR"));
            entityManager.merge(new PrivilegeMapping(null, "/vendor/update/", "PUT", "EDIT_VENDOR"));
            entityManager.merge(new PrivilegeMapping(null, "/kyc/all", "GET", "VIEW_KYC"));
            entityManager.merge(new PrivilegeMapping(null, "/adminreview/export-excel", null, "Invoice"));
            entityManager.merge(new PrivilegeMapping(null, "/admin/dashboard", "GET", "Home"));
            entityManager.merge(new PrivilegeMapping(null, "/games/get-all-games-by-admin", "GET", "VIEW_GAMES"));
            entityManager.merge(new PrivilegeMapping(null, "/leagues/get-leagues-with-filters", "GET", "VIEW_LEAGUES"));
            entityManager.merge(new PrivilegeMapping(null, "/tournament/get-tournaments-by-admin", "GET", "VIEW_TOURNAMENTS"));
            entityManager.merge(new PrivilegeMapping(null, "/plans/get-allplans", "GET", "Subscriptions"));
        }

        if (entityManager.createQuery("SELECT COUNT(f) FROM FAQs f", Long.class).getSingleResult() == 0) {

// ---------- Subscription and Plans ----------
            entityManager.merge(new FAQs("Subscription and Plans", "How can I check my current subscription plan?",
                    "Visit the \"Subscription Page\" to view your current plan details, including validity, features, and cost.", "Vendor"));

            entityManager.merge(new FAQs("Subscription and Plans", "Can I upgrade my subscription plan?",
                    "Yes, go to the \"Subscription Page,\" explore other plans, and select the desired plan. Click on the request plan, your subscription request will be sent to the admin, once approved, you can pay and upgrade your subscription.", "Vendor"));

            entityManager.merge(new FAQs("Subscription and Plans", "What happens to my plan if I upgrade mid-cycle?",
                    "The new plan activates immediately, and the remaining balance from your current plan is adjusted against the new plan cost.", "Vendor"));

            entityManager.merge(new FAQs("Subscription and Plans", "Can I downgrade or cancel my subscription?",
                    "Yes, you can manage your plan from the \"Subscription Page.\" Downgrades or cancellations will take effect at the end of your current billing cycle. You need to renew your subscription after each plan cycle.", "Vendor"));

            entityManager.merge(new FAQs("Subscription and Plans", "How are the monthly and annual plan costs different?",
                    "Monthly plans are billed monthly, while annual plans offer a discounted rate for upfront payment and a year-long validity. Initially only monthly payments will be accepted. You can raise tickets with admin for annual subscription interest.", "Vendor"));

// ---------- AAG Wallet and Earnings ----------
            entityManager.merge(new FAQs("AAG Wallet and Earnings", "How is my target revenue determined?",
                    "Your target revenue is calculated based on your subscription plan, activity, and participation metrics.", "Vendor"));

            entityManager.merge(new FAQs("AAG Wallet and Earnings", "What is the minimum balance required to request a withdrawal?",
                    "You must achieve at least 50% of your target revenue of the plan to be eligible for a withdrawal request.", "Vendor"));

            entityManager.merge(new FAQs("AAG Wallet and Earnings", "Can I withdraw my earnings in multiple transactions?",
                    "Yes, but your withdrawals are subject to meeting the minimum balance criteria and cannot exceed the available eligible amount.", "Vendor"));

            entityManager.merge(new FAQs("AAG Wallet and Earnings", "Are there any charges for withdrawing earnings?",
                    "No, withdrawals are free. However, ensure your AAG Wallet balance meets the eligibility criteria.", "Vendor"));

            entityManager.merge(new FAQs("AAG Wallet and Earnings", "How long does it take for a withdrawal request to process?",
                    "Withdrawal requests are processed within 2-5 business days, depending on your payment method and bank.", "Vendor"));

            entityManager.merge(new FAQs("AAG Wallet and Earnings", "What are the required steps for withdrawal?",
                    "You need to complete your KYC and add your bank account to successfully request for a withdrawal.", "Vendor"));

            entityManager.merge(new FAQs("AAG Wallet and Earnings", "What are the required steps for the KYC process?",
                    "You need to complete your KYC by submitting your ID proof (Aadhaar, Pan card) details from your setting section in the AAGVeer app.", "Vendor"));

// ---------- Publishing Games and Events ----------
            entityManager.merge(new FAQs("Publishing Games and Events", "How do I publish a game?",
                    "Go to the \"Publish Games\" section, select a game from the list, choose the theme and entry fee, and either publish it immediately or schedule it for a later date.", "Vendor"));

            entityManager.merge(new FAQs("Publishing Games and Events", "Can I edit a scheduled game?",
                    "Yes, you can edit a scheduled game up to one day before publish time. After that, changes are locked.", "Vendor"));

            entityManager.merge(new FAQs("Publishing Games and Events", "What is the daily publishing limit for games,leagues and tournaments?",
                    "The daily limit depends on your subscription plan. Check the \"Subscription Page\" for plan-specific details.", "Vendor"));

            entityManager.merge(new FAQs("Publishing Games and Events", "How can I schedule a tournament?",
                    "In the \"Publish Games\" section, choose \"Tournament,\" set the parameters (e.g., theme, entry fee, schedule), and confirm. Once the prize pool is allotted and confirmed by admin, the tournament is published.", "Vendor"));

            entityManager.merge(new FAQs("Publishing Games and Events", "Where can I view my published games and events?",
                    "Visit the \"Publish History\" section to see all your past published games and events.", "Vendor"));

// ---------- Leaderboard and Analytics ----------
            entityManager.merge(new FAQs("Leaderboard and Analytics", "How is the leaderboard ranking calculated?",
                    "Rankings are based on popular categories such as revenue, points, and popularity across all subscription tiers.", "Vendor"));

            entityManager.merge(new FAQs("Leaderboard and Analytics", "Can I see my performance compared to other vendors?",
                    "Yes, the leaderboard provides category-based comparisons of your performance with other vendors.", "Vendor"));

            entityManager.merge(new FAQs("Leaderboard and Analytics", "How often is the leaderboard updated?",
                    "The leaderboard is updated in real-time as new games, tournaments, and events are hosted.", "Vendor"));

// ---------- Account and Profile Management ----------
            entityManager.merge(new FAQs("Account and Profile Management", "How do I update my profile information?",
                    "Go to the \"Account\" section in the menu to update your details, such as contact information and social media links.", "Vendor"));

            entityManager.merge(new FAQs("Account and Profile Management", "What should I do if my account is under verification?",
                    "Wait for our onboarding team to review and approve your details. If any issues arise, you will be contacted. You can raise a support ticket if you need further help.", "Vendor"));

            entityManager.merge(new FAQs("Account and Profile Management", "What happens when I make my account private?",
                    "When a vendor makes their account private, their points/scores data are hidden from the leaderboard. Other AAGVeers can still see the ranking of the private account AAGVeer", "Vendor"));

            entityManager.merge(new FAQs("Account and Profile Management", "Can I Pause my Account?",
                    "Yes, If you want to take a break from AAGVeer you can pause your account. Pausing an account hides your profile on user app AAG until your account is reactivated.", "Vendor"));

            // ---------- General Queries and Support ----------
            entityManager.merge(new FAQs("General Queries and Support", "Where can I find the terms and conditions?",
                    "Terms and conditions are available in the sidebar section of the menu.", "Vendor"));

            entityManager.merge(new FAQs("General Queries and Support", "How do I contact customer support?",
                    "Use the \"Support\" option in the app menu to reach out to our customer service team.", "Vendor"));

            entityManager.merge(new FAQs("General Queries and Support", "Are there any tutorials or guides for using the app?",
                    "Yes, check the \"Help\" section in the menu for tutorials, guides, and FAQs.", "Vendor"));

            entityManager.merge(new FAQs("General Queries and Support", "What should I do if the app is not functioning correctly?",
                    "Ensure you are using the latest version of the app. If the issue persists, contact support through the \"Support\" section.", "Vendor"));

//            User


            entityManager.merge(new FAQs("General Queries and Support", "Is AAG a safe gaming platform?",
                    "Yes, AAG is a completely safe and trustworthy skill-based gaming platform. All games on the platform are fair and secure. We use advanced fraud detection mechanisms to prevent any unfair gameplay and ensure a transparent and safe gaming experience for all users.", "User"));

            entityManager.merge(new FAQs("General Queries and Support", "Can I play games for free on AAG?",
                    "AAG does not offer traditional free-to-play games. Games on AAG are played through influencer-hosted challenges, where users can participate in skill-based matches organized by verified content creators.", "User"));

            entityManager.merge(new FAQs("General Queries and Support", "How many games and themes are available on AAG?",
                    "Currently, AAG offers 2 skill-based games – Ludo and Snakes & Ladders – each available in 7 unique and engaging themes.", "User"));

            entityManager.merge(new FAQs("General Queries and Support", "Who can host games on AAG?",
                    "Only verified influencers on AAG can publish and host games. Players can join matches that they have initiated.", "User"));

            entityManager.merge(new FAQs("General Queries and Support", "How can I start playing a game?",
                    "Once you join a game hosted by an influencer or for self-explore, select your preferred game, pay the entry fee, and compete with other players in real time.", "User"));

            entityManager.merge(new FAQs("General Queries and Support", "Is AAG available on both Android and iOS?",
                    "AAG is currently available only on Android devices. The iOS version is under development and will be released soon.", "User"));

            entityManager.merge(new FAQs("General Queries and Support", "How do I deposit money into my AAG Wallet?",
                    "Go to the Wallet section in the AAG app, enter the desired amount, and choose from various payment options like UPI, Google Pay, PhonePe, etc.", "User"));

            entityManager.merge(new FAQs("AAG Wallet and Earnings", "How can I withdraw my winnings?",
                    "To withdraw:\nFirst, add your UPI ID or bank account to your profile.\nThen go to the Wallet → Withdraw section.\nEnter the amount (minimum ₹110) and confirm the withdrawal.", "User"));

            entityManager.merge(new FAQs("AAG Wallet and Earnings", "What should I do if my winnings are not credited?",
                    "If you haven’t received your withdrawal even after 24 hours, kindly contact AAG Customer Support via the app’s Help section. We’re happy to assist!", "User"));

            entityManager.merge(new FAQs("General Queries and Support", "Is AAG part of any industry body or gaming association?",
                    "AAG follows industry best practices and legal compliance to offer a safe and responsible gaming experience. We are committed to secure, skill-based gameplay only.", "User"));

        }



        //  Role to Privilege mapping
        if (entityManager.createQuery("SELECT COUNT(rp) FROM Role r JOIN r.privileges rp", Long.class).getSingleResult() == 0) {
            Role admin = entityManager.find(Role.class, 1L);
            Role support = entityManager.find(Role.class, 2L);
            Role finance = entityManager.find(Role.class, 3L);
            Role vendor = entityManager.find(Role.class, 4L);

            List<Privilege> allPrivileges = entityManager.createQuery("FROM Privilege", Privilege.class).getResultList();

            // Admin → All
            admin.setPrivileges(new HashSet<>(allPrivileges));

/*            //  Finance
            finance.setPrivileges(allPrivileges.stream()
                    .filter(p -> List.of(1L, 2L, 3L, 4L, 5L).contains(p.getId()))
                    .collect(Collectors.toSet()));

            //  Support
            support.setPrivileges(allPrivileges.stream()
                    .filter(p -> List.of(6L, 7L, 8L).contains(p.getId()))
                    .collect(Collectors.toSet()));

            //  Vendor
            vendor.setPrivileges(allPrivileges.stream()
                    .filter(p -> List.of(12L, 13L, 14L).contains(p.getId()))
                    .collect(Collectors.toSet()));*/

            entityManager.merge(admin);
            entityManager.merge(finance);
            entityManager.merge(support);
            entityManager.merge(vendor);
        }


    }




}


