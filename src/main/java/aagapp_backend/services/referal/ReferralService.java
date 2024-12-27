package aagapp_backend.services.referal;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.repository.CustomCustomerRepository;
import aagapp_backend.services.CustomCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class ReferralService {

    @Autowired
    private CustomCustomerRepository customCustomerRepository;
    @Autowired
    private CustomCustomerService customCustomerService;

    public String generateReferralCode(CustomCustomer customerId) {
        String characters = Constant.REFERAL_STRING;
        StringBuilder referralCode = new StringBuilder(7);
        Random random = new Random();
        // Generate a 7-character referral code
        for (int i = 0; i < 7; i++) {
            int index = random.nextInt(characters.length());
            referralCode.append(characters.charAt(index));
        }
        return referralCode.toString();
    }


    public void updateReferrerEarnings(String referredCode) {
        CustomCustomer referrer = customCustomerService.findCustomCustomerByReferralCode(referredCode);
        if (referrer != null) {
            referrer.setReferralCount(referrer.getReferralCount() + 1);
            referrer.setBonusBalance(referrer.getBonusBalance().add(BigDecimal.valueOf(Constant.USER_REFERAL_BALANCE)));
            customCustomerRepository.save(referrer);
        }
    }
}

