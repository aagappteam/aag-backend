package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomAdmin;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.admin.AdminLogs;
import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.notification.NotificationShare;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.ActivityType;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.enums.TournamentStatus;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.NotificationShareRepository;
import aagapp_backend.repository.admin.AdminLogsInterface;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.earning.InfluencerMonthlyEarningRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.payment.PaymentRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.payment.PaymentService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CommonService {


    @Autowired
    private EmailService emailService;

    @Autowired
    private AdminLogsInterface adminLogsInterface;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;
    private CustomCustomerService customCustomerService;
    private PlayerRepository playerRepository;
    private PaymentService paymentService;
    private PaymentRepository paymentRepository;
    private InfluencerMonthlyEarningRepository earningRepository;
    private EntityManager entityManager;
    private NotificationRepository notificationRepository;
    private NotificationShareRepository notificationShareRepository;

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setPlayerRepository(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Autowired
    public void setPaymentService(@Lazy PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Autowired
    public void setEarningRepository(InfluencerMonthlyEarningRepository earningRepository) {
        this.earningRepository = earningRepository;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Autowired
    public void setNotificationShareRepository(NotificationShareRepository notificationShareRepository) {
        this.notificationShareRepository = notificationShareRepository;
    }

    public <T> T findOrThrow(Optional<T> opt, String entityName, Object id) {
        return opt.orElseThrow(() -> new BusinessException(entityName + " not found with ID: " + id, HttpStatus.BAD_REQUEST));
    }

//    2 minutes cron auto reject stale tournaments & leagues
@Scheduled(fixedRate = 120000) // every 2 minutes
public void autoRejectUnapprovedTournamentsAndLeagues() throws IOException {
    ZonedDateTime fifteenMinutesAgo = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).minusMinutes(15);

    // Reject stale tournaments
    List<Tournament> staleTournaments = tournamentRepository.findAllByStatusAndCreatedDateBefore(
            TournamentStatus.PENDING, fifteenMinutesAgo);
    for (Tournament t : staleTournaments) {
        t.setStatus(TournamentStatus.REJECTED);
        tournamentRepository.save(t);

        String title = "Tournament Rejected";
        String body = "Your tournament has been automatically rejected by the admin.";

        notificationService.sendRejectionNotificationToVendor(t.getVendorEntity(), "Tournament", t.getName());
        emailService.sendTournamentRejectionEmail(t.getVendorEntity(), title, body,t);
    }

    // Reject stale leagues
    List<League> staleLeagues = leagueRepository.findAllByStatusAndCreatedDateBefore(
            LeagueStatus.PENDING, fifteenMinutesAgo);
    for (League l : staleLeagues) {
        l.setStatus(LeagueStatus.REJECTED);
        leagueRepository.save(l);

        String title = "League Rejected";
        String body = "Your league has been automatically rejected by the admin.";

        notificationService.sendRejectionNotificationToVendor(l.getVendorEntity(), "League", l.getGameName());
//       @todo:- need to send rejection mail template
        emailService.sendTournamentEmail(l.getVendorEntity(), title, body);
    }
}




    public String resolveGameImageUrl(AagAvailableGames game, Long themeId) {
        return game.getThemes().stream()
                .filter(theme -> theme.getId().equals(themeId))
                .map(ThemeEntity::getGameimageUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(game.getGameImage());
    }

    public void deductFromWallet(Long playerId, Double amount,String description) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));
        Wallet wallet = player.getCustomer().getWallet();

        CustomCustomer customer = customCustomerService.getCustomerById(playerId);
        if (customer == null) {
            throw new BusinessException("Customer not found for player with ID: " + playerId, HttpStatus.BAD_REQUEST);
        }

        BigDecimal gameAmount = BigDecimal.valueOf(amount);
        BigDecimal fivePercent = gameAmount.multiply(BigDecimal.valueOf(Constant.BONUS_PERCENT)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ninetyFivePercent = gameAmount.subtract(fivePercent);

        BigDecimal unplayedBD = BigDecimal.valueOf(wallet.getUnplayedBalance());
        BigDecimal winning = wallet.getWinningAmount();
        BigDecimal bonusBalance = customer.getBonusBalance(); // Should be a BigDecimal

        System.out.println("Requested Amount: " + gameAmount);
        System.out.println("Bonus Part: " + fivePercent + " | Cash Part: " + ninetyFivePercent);

        BigDecimal bonusUsed = BigDecimal.ZERO;
        BigDecimal cashRequired = gameAmount;

        // ✅ Try to use bonus
        if (bonusBalance.compareTo(fivePercent) >= 0) {
            bonusUsed = fivePercent;
            cashRequired = ninetyFivePercent;
            customer.setBonusBalance(bonusBalance.subtract(bonusUsed));
            System.out.println("Used bonus: " + bonusUsed);
        } else {
            System.out.println("No or insufficient bonus, using 100% from wallet.");
        }

        //  Deduct from unplayed + winning
        BigDecimal totalWallet = unplayedBD.add(winning);
        if (totalWallet.compareTo(cashRequired) < 0) {
            throw new BusinessException("Insufficient wallet balance", HttpStatus.BAD_REQUEST);
        }

        if (unplayedBD.compareTo(cashRequired) >= 0) {
            wallet.setUnplayedBalance(unplayedBD.subtract(cashRequired).doubleValue());
            System.out.println("Deducted full cash part from unplayed.");
        } else {
            BigDecimal remaining = cashRequired.subtract(unplayedBD);
            wallet.setUnplayedBalance(0.0);
            wallet.setWinningAmount(winning.subtract(remaining));
            System.out.println("Deducted " + unplayedBD + " from unplayed and " + remaining + " from winning.");
        }



        // ✅ Notification
        Notification notification = new Notification();
        notification.setCustomerId(customer.getId());
        notification.setDescription("Wallet balance deducted");
        notification.setAmount(gameAmount.doubleValue());
        notification.setName(customer.getName()!=null?customer.getName():"N/A");
        notification.setDetails(description);
        notification.setRole("Customer");



        notificationRepository.save(notification);
    }


    @Transactional
    public void addVendorEarningForPayment(Long vendorId, BigDecimal paymentAmount, BigDecimal vendorSharePercent) {
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<PaymentEntity> activePlanOptional = paymentService.getActivePlanByVendorId(vendorId);

        if (activePlanOptional.isEmpty()) {
            throw new IllegalStateException("No active payment plan found for vendor ID: " + vendorId);
        }

        PaymentEntity activePlan = activePlanOptional.get(0);
        Long paymentId = activePlan.getId();
        Optional<InfluencerMonthlyEarning> earningOpt = earningRepository.findLatestByInfluencerId(vendorId);

        InfluencerMonthlyEarning earning = earningOpt.orElseGet(() -> {
            InfluencerMonthlyEarning newEarning = new InfluencerMonthlyEarning();
            newEarning.setInfluencerId(vendorId);
            newEarning.setPaymentId(paymentId);
            newEarning.setMonthYear(monthYear);
            newEarning.setRechargeAmount(activePlan.getAmount() != null ? BigDecimal.valueOf(activePlan.getAmount()) : BigDecimal.ZERO);
            newEarning.setMultiplier(Constant.MULTIPLIER);
            newEarning.setEarnedAmount(BigDecimal.ZERO);
            return newEarning;
        });

        // Step 3: Calculate vendor's share
        BigDecimal shareAmount = paymentAmount.multiply(vendorSharePercent);

        // Step 4: Add earnings
        earning.setEarnedAmount(earning.getEarnedAmount().add(shareAmount));
        earningRepository.save(earning);

        // Step 5: Log notification
        NotificationShare notification = new NotificationShare();
        notification.setVendorId(vendorId);
        notification.setDescription("Vendor Earning for Payment");
        notification.setAmount(shareAmount.doubleValue());
        notification.setDetails("You earned Rs. " + shareAmount.stripTrailingZeros().toPlainString() + " from a recent game played by a user.");

//        notification.setDetails("You earned Rs. " + shareAmount.doubleValue() + " from a recent game played by a user.");
        notificationShareRepository.save(notification);
    }


    public void createOrUpdateMonthlyPlan(Long vendorId, BigDecimal ammount, int multiplier) {

        String monthYear = LocalDate.now().toString().substring(0, 7); // e.g., "2025-05"
        PaymentStatus status = PaymentStatus.ACTIVE;

//        Optional<PaymentEntity> activePlanOptional = paymentRepository.findActivePlanByVendorId(serviceProviderId, LocalDateTime.now(), status);
        List<PaymentEntity> activePlanOptional = paymentService.getActivePlanByVendorId(vendorId);


        if (activePlanOptional.isEmpty()) {
            throw new BusinessException("No active plan found for the influencer.", HttpStatus.NOT_FOUND);
        }

        Long paymentId = activePlanOptional.get(0).getId();

        Optional<InfluencerMonthlyEarning> existingOpt = earningRepository.findByInfluencerIdAndPaymentId(
                vendorId, paymentId
        );

        if (existingOpt.isEmpty()) {
            // Create new record
            InfluencerMonthlyEarning newEarning = new InfluencerMonthlyEarning();
            newEarning.setInfluencerId(vendorId);
            newEarning.setPaymentId(paymentId);
            newEarning.setMonthYear(monthYear);
            newEarning.setRechargeAmount(ammount);
            newEarning.setMultiplier(Constant.MULTIPLIER);
            newEarning.setEarnedAmount(BigDecimal.ZERO);
            earningRepository.save(newEarning);

        } else {
            // Update existing record
            InfluencerMonthlyEarning existing = existingOpt.get();
            existing.setRechargeAmount(ammount);
            existing.setMultiplier(multiplier);
            earningRepository.save(existing);
        }
    }

    @Async
    public void notifyAdminsByRoleGeneric(int role, Object entity) throws MessagingException, IOException {
        List<CustomAdmin> admins = entityManager.createQuery(
                        "SELECT a FROM CustomAdmin a WHERE a.role = :role AND a.active = 1", CustomAdmin.class)
                .setParameter("role", role)
                .getResultList();

        String type;
        String name;
        Double fee;
        Long id;
        Long senderid;
        String senderrole;
        ZonedDateTime createdDate;
        String gameIcon;
        String vendorname;

        if (entity instanceof League) {
            League league = (League) entity;
            type = "League_Approval";
            name = league.getName();
            senderid= league.getVendorEntity().getService_provider_id();
            senderrole= Constant.ROLE_VENDOR;

            vendorname = league.getVendorEntity().getName();
            fee = league.getFee();
            id = league.getId();
            createdDate = league.getCreatedDate();
            gameIcon = league.getTheme().getGameimageUrl(); // Assumes getter
        } else if (entity instanceof Tournament) {
            Tournament tournament = (Tournament) entity;
            type = "Tournament_Approval";
            name = tournament.getName();
            senderid= tournament.getVendorEntity().getService_provider_id();
            senderrole= Constant.ROLE_VENDOR;
            vendorname = tournament.getVendorEntity().getName();

            fee = Double.valueOf(tournament.getEntryFee());
            id = tournament.getId();
            createdDate = tournament.getCreatedDate();
            gameIcon = tournament.getTheme().getGameimageUrl(); // Assumes getter
        } else {
            throw new IllegalArgumentException("Unsupported entity type");
        }

        // Log admin notification
        AdminLogs adminLogs = new AdminLogs();
        adminLogs.setMessage("New Request for " + type + " created by" + vendorname);
        adminLogs.setTargetRole(Constant.ROLE_ADMIN);
        adminLogs.setPerformedBy("System");
        adminLogs.setSenderid(senderid);
        adminLogs.setSenderrole(senderrole);
        adminLogs.setAssignedRole(Constant.ROLE_ADMIN);
        adminLogs.setTargetId(id);
        adminLogs.setTargetType(type);
        adminLogs.setRead(false);
        adminLogs.setCreatedDate(createdDate);
        adminLogsInterface.save(adminLogs);

        // Send email to all admins
        for (CustomAdmin admin : admins) {
            if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
                emailService.sendEmailLeague(admin, type, name, fee, id, createdDate, gameIcon,vendorname);
            }
        }
    }

    @Async
    public void notifyAdminsByRole(int role, String type,String name,Long targetid,String message, Long senderId,
                                   String senderRole) throws MessagingException, IOException {
        List<CustomAdmin> admins = entityManager.createQuery(
                        "SELECT a FROM CustomAdmin a WHERE a.role = :role AND a.active = 1", CustomAdmin.class)
                .setParameter("role", role)
                .getResultList();

        ZonedDateTime createdDate = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

        // Log admin notification
        AdminLogs adminLogs = new AdminLogs();
        adminLogs.setMessage("New Request for " + type + " created by" + name);
        adminLogs.setTargetRole(Constant.ROLE_ADMIN);
        adminLogs.setPerformedBy("System");
        adminLogs.setAssignedRole(Constant.ROLE_ADMIN);
        adminLogs.setSenderid(senderId);
        adminLogs.setSenderrole(senderRole);
        adminLogs.setTargetId(targetid);
//        adminLogs.setTargetType(type);
        adminLogs.setTargetType(type.replace(" ", "_"));
        adminLogs.setRead(false);
        adminLogs.setCreatedDate(createdDate);
        adminLogsInterface.save(adminLogs);

        // Send email to all admins
        for (CustomAdmin admin : admins) {
            if (admin.getEmail() != null && !admin.getEmail().isEmpty()) {
                emailService.sendAdminCommonmail(admin, type, name, message);
            }
        }
    }



    public void addXpPoints(ActivityType activityType, Player player) {

        CustomCustomer customer = playerRepository.findById(player.getPlayerId()).map(Player::getCustomer).orElse(null);

        int xpToAdd = 0;

        switch (activityType) {
            case GAME:
                xpToAdd = customer.getIsWeeklyBoosterActive() ? 4 : 2;
                break;
            case LEAGUE:
                xpToAdd = customer.getIsWeeklyBoosterActive() ? 10 : 5;
                break;
            case TOURNAMENT:
                xpToAdd = customer.getIsWeeklyBoosterActive() ? 10 : 5;
                break;
            default:
                throw new IllegalArgumentException("Unsupported activity type: " + activityType);
        }

        customer.setXpPoints(customer.getXpPoints() + xpToAdd);
        customCustomerRepository.save(customer);
    }



    // Every Monday at 00:00 AM
    @Scheduled(cron = "0 0 0 * * MON")
    public void resetWeeklyBoosters() {
        List<CustomCustomer> customers = customCustomerRepository.findAll();

        for (CustomCustomer customer : customers) {
            customer.setWeeklyBoostersLeft(2);
            customer.setBoosterActivatedAt(null);
        }

        customCustomerRepository.saveAll(customers);

    }
}
