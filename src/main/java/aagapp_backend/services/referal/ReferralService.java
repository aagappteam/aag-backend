package aagapp_backend.services.referal;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class ReferralService {

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private WalletRepository walletRepository;

    public String generateReferralCode(CustomCustomer customerId) {
        try {
            String characters = Constant.REFERAL_STRING;
            StringBuilder referralCode = new StringBuilder(7);
            Random random = new Random();
            // Generate a 7-character referral code
            for (int i = 0; i < 7; i++) {
                int index = random.nextInt(characters.length());
                referralCode.append(characters.charAt(index));
            }
            return referralCode.toString();
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return null;
        }
    }

    public String generateVendorReferralCode(VendorEntity vendorEntity) {
        try {
            String characters = Constant.REFERAL_STRING;
            StringBuilder referralCode = new StringBuilder(7);
            Random random = new Random();
            // Generate a 7-character referral code
            for (int i = 0; i < 7; i++) {
                int index = random.nextInt(characters.length());
                referralCode.append(characters.charAt(index));
            }
            return referralCode.toString();
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return null;
        }
    }


    public void updateReferrerEarnings(String referredCode) {
        try {
            CustomCustomer referrer = customCustomerService.findCustomCustomerByReferralCode(referredCode);
            if (referrer != null) {

                Wallet referrerWallet = walletRepository.findByCustomCustomer(referrer);

                // Update referral count and bonus balance
                referrer.setReferralCount(referrer.getReferralCount() + 1);
                BigDecimal currentBonus = referrer.getBonusBalance() != null ? referrer.getBonusBalance() : BigDecimal.ZERO;
                referrer.setBonusBalance(currentBonus.add(BigDecimal.valueOf(Constant.USER_REFERAL_BALANCE)));

                // Save the updated referrer (this will also save the wallet if it's changed)
                customCustomerRepository.save(referrer);

                Notification referralNotification = new Notification();
                referralNotification.setRole("Customer");
                referralNotification.setCustomerId(referrer.getId());
                referralNotification.setDescription("Referral Bonus Earned");
                referralNotification.setAmount((double) Constant.USER_REFERAL_BALANCE);
                referralNotification.setDetails("You earned ₹" + Constant.USER_REFERAL_BALANCE + " for a successful referral!");
                notificationRepository.save(referralNotification);
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }
    }

}

