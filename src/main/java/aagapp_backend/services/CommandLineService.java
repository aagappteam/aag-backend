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
                    new PredefinedQA(null, "What is the minimum withdrawal amount?", "The minimum withdrawal amount is ₹50. Withdrawals below this amount are not allowed.", TicketUserType.CUSTOMER),
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

            entityManager.merge(new FAQs("General", "What is AAG?",
                    "AAG (Aapka Apna Game) is a real-money gaming platform that allows users to play and earn money by engaging in various games. You can participate in games like Ludo, Snake & Ladders, and more, and earn real cash rewards based on your performance.",
                    "User"));

            entityManager.merge(new FAQs("General", "What are the KYC requirements?",
                    "To fully access all features on AAG, you will need to complete your KYC (Know Your Customer) verification. This process includes submitting a government-issued ID (e.g., Aadhaar, Passport, Voter ID) and a selfie for identity confirmation.",
                    "User"));

            entityManager.merge(new FAQs("General", "How can I withdraw my earnings?",
                    "You can withdraw your earnings by requesting a withdrawal via the Withdrawal section of the app. Ensure that your KYC is completed before making any withdrawals. Your funds will be transferred to your registered bank account or wallet, depending on the withdrawal method selected.",
                    "User"));

            entityManager.merge(new FAQs("General", "Is AAG secure to use?",
                    "Yes, AAG uses state-of-the-art security measures to protect your data and transactions. We encrypt all sensitive information and work with trusted payment partners to ensure secure transactions.",
                    "User"));

            entityManager.merge(new FAQs("General", "What games can I play on AAG?",
                    "Currently, AAG offers casual games like Ludo, Snake & Ladders, and more. These games are designed for all ages and skill levels. New games may be added to the platform in the future.",
                    "User"));

            entityManager.merge(new FAQs("General", "Can I play for free?",
                    "Yes, AAG offers free-to-play games, but to earn real money, you will need to participate in cash-based games. The winnings depend on your performance in the games.",
                    "User"));

            entityManager.merge(new FAQs("General", "How do I contact support?",
                    "If you have any issues or questions, you can reach our support team through the following channels:\nEmail: support@aag.com\nPhone: +91 XXXXXXXXXX\nIn-app Support Chat",
                    "User"));

            entityManager.merge(new FAQs("General", "How do I delete my AAG account?",
                    "If you wish to delete your AAG account, please contact our support team at support@aag.com. Please note that once your account is deleted, all data, including transaction history and funds, will be permanently removed.",
                    "User"));

            entityManager.merge(new FAQs("General", "How do I refer friends to AAG?",
                    "You can refer your friends by using the Referral Code available in the app. When your friends sign up and play on AAG using your code, both you and your friend will receive referral bonuses.",
                    "User"));

            entityManager.merge(new FAQs("General", "Are there any age restrictions for playing on AAG?",
                    "Yes, you must be 18 years or older to participate in real-money games on AAG. The platform is strictly for adult users, and all users must comply with age verification during the sign-up process.",
                    "User"));

            entityManager.merge(new FAQs("General", "How do I improve my chances of winning?",
                    "Winning on AAG depends on skill and luck. To improve your chances:\n1. Practice regularly\n2. Understand the game mechanics\n3. Stay updated on any new game features or changes",
                    "User"));

            // AAGVEER FAQs (Vendor)
            entityManager.merge(new FAQs("Vendor", "What is AAGVEER?",
                    "AAGVEER is a platform designed for influencers and content creators to publish and promote games on the AAG platform. As an AAGVEER vendor, you can earn commissions every time users play the games you publish.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "How do I become an AAGVEER vendor?",
                    "To become an AAGVEER vendor:\nDownload the AAGVEER app from the Play Store or App Store.\nSign up by providing your basic details and linking your social media accounts.\nSubmit your KYC details for verification.\nOnce verified, you will be eligible to publish your own games and start earning commissions.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "What are the subscription plans for AAGVEER?",
                    "AAGVEER offers multiple subscription plans based on your social media following and engagement. You can choose a plan that best suits your needs and get started with game publishing. Contact us to know more about the available plans and pricing.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "How do I publish a game on AAGVEER?",
                    "Once you are onboarded as a verified vendor, you can use the AAGVEER app to upload and publish your own games. Choose the game type, set your parameters, and follow the easy steps to launch it. After approval, your game will be available to all AAG users.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "How do I earn money as an AAGVEER vendor?",
                    "As an AAGVEER vendor, you earn money based on the number of users who play your published games. Every time a user plays your game, you earn a commission. The more popular your game becomes, the more you earn!",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "Can I withdraw my earnings from AAGVEER?",
                    "Yes, you can withdraw your earnings once they are credited to your account. You can make a withdrawal request through the app, and your funds will be transferred to your registered bank account or wallet.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "What is the KYC process for AAGVEER?",
                    "To become a verified AAGVEER vendor, you need to complete your KYC (Know Your Customer) verification. This includes uploading your ID proof (Aadhaar, Voter ID, Passport) and a selfie for identity verification. Once your documents are verified, you can start publishing games.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "How can I upgrade or change my subscription plan?",
                    "You can upgrade or change your subscription plan by logging into your AAGVEER dashboard and selecting the new plan. If you need assistance, feel free to reach out to our support team for guidance.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "How do I track my earnings?",
                    "You can track your earnings in real-time by going to the Earnings section in your AAGVEER app. You’ll be able to see your total earnings, active games, and user engagement statistics. This helps you to stay updated on your performance.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "Can I publish more than one game?",
                    "Yes, you can publish multiple games on AAGVEER. The more games you have, the more chances you have to earn commissions from users playing those games.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "How do I contact support for AAGVEER?",
                    "If you face any issues or need help with your vendor account, you can reach our support team through the following channels:\nEmail: support@aag.com\nPhone: +91 XXXXXXXXXX\nIn-app Support Chat",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "What happens if my KYC is rejected?",
                    "If your KYC is rejected, you will receive an email detailing the reasons for the rejection. You can resubmit your KYC documents after making the necessary corrections. Once approved, you will be able to proceed with your vendor activities.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "Is there any fee to use AAGVEER?",
                    "Yes, AAGVEER operates on a subscription-based model, where you pay a fee depending on the subscription plan you select. There are also potential charges for game promotions and advanced features. Contact us for more details.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "Can I withdraw my subscription fee?",
                    "The subscription fee is non-refundable. However, you can earn back your subscription cost by publishing games and earning commissions through the AAGVEER platform.",
                    "Vendor"));

            entityManager.merge(new FAQs("Vendor", "How long does it take to get verified?",
                    "KYC verification typically takes 2-3 business days. Once your documents are approved, you will be notified via email, and you can start using AAGVEER.",
                    "Vendor"));
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


