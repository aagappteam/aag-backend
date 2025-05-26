package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.earning.InfluencerMonthlyEarning;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.notification.NotificationShare;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.NotificationShareRepository;
import aagapp_backend.repository.earning.InfluencerMonthlyEarningRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.services.exception.BusinessException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Service
public class CommonService {
    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private InfluencerMonthlyEarningRepository earningRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationShareRepository notificationShareRepository;

    public <T> T findOrThrow(Optional<T> opt, String entityName, Object id) {
        return opt.orElseThrow(() -> new BusinessException(entityName + " not found with ID: " + id, HttpStatus.BAD_REQUEST));
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
        BigDecimal fivePercent = gameAmount.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
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

        // ✅ Deduct from unplayed + winning
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
        notification.setDetails(description);
        notification.setRole("Customer");



        notificationRepository.save(notification);
    }

    public void createOrUpdateMonthlyPlan(Long influencerId, BigDecimal rechargeAmount, int multiplier) {
        String monthYear = LocalDate.now().toString().substring(0, 7);

        InfluencerMonthlyEarning existing = earningRepository.findByInfluencerIdAndMonthYear(influencerId, monthYear);

        if (existing == null) {
            InfluencerMonthlyEarning newEarning = new InfluencerMonthlyEarning();
            newEarning.setInfluencerId(influencerId);
            newEarning.setMonthYear(monthYear);
            newEarning.setRechargeAmount(rechargeAmount);
            newEarning.setMultiplier(multiplier);
            newEarning.setEarnedAmount(BigDecimal.ZERO); // Start with 0
            earningRepository.save(newEarning);
        } else {
            existing.setRechargeAmount(rechargeAmount);
            existing.setMultiplier(multiplier);
            earningRepository.save(existing);
        }
    }

    @Transactional
    public void addVendorEarningForPayment(Long vendorId, BigDecimal paymentAmount, BigDecimal vendorSharePercent) {
        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        InfluencerMonthlyEarning earning = earningRepository.findByInfluencerIdAndMonthYear(vendorId, monthYear);

        if (earning == null) {
            earning = new InfluencerMonthlyEarning();
            earning.setInfluencerId(vendorId);
            earning.setMonthYear(monthYear);
            earning.setRechargeAmount(BigDecimal.ZERO);
            earning.setMultiplier(Constant.MULTIPLIER);
            earning.setEarnedAmount(BigDecimal.ZERO);
        }

        // Calculate vendor share on payment (5 Rs * 5% = 0.25 Rs)
        BigDecimal shareAmount = paymentAmount.multiply(vendorSharePercent);

        earning.setEarnedAmount(earning.getEarnedAmount().add(shareAmount));

        NotificationShare notification = new NotificationShare();
        notification.setVendorId(vendorId);
        notification.setDescription("Vendor Earning for Payment");
        notification.setAmount(shareAmount.doubleValue());
        notification.setDetails("Rs. " + shareAmount + " earned for payment");
        notificationShareRepository.save(notification);

        earningRepository.save(earning);
    }


}
