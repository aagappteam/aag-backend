package aagapp_backend.services.payment;

import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.payment.PaymentFeaturesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.LimitExceededException;
import java.time.LocalDate;

@Service
public class PaymentFeatures {

    @Autowired
    private PaymentFeaturesRepository paymentFeaturesRepository;

    @Autowired
    private GameRepository gameRepository;

    public boolean canPublishGame(Long vendorId) throws LimitExceededException {
        // Get the active plan for the vendor
        PaymentFeatures activePlan = paymentFeaturesRepository.findActivePlanByVendorId(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("No active plan found"));

        int dailyUsage = gameRepository.countGamesByVendorIdAndDate(vendorId, LocalDate.now());
        int weeklyUsage = gameRepository.countGamesByVendorIdAndWeek(vendorId);

        if (dailyUsage >= activePlan.getDailyLimit()) {
            throw new LimitExceededException("Daily publishing limit reached");
        }

        if (weeklyUsage >= activePlan.getWeeklyLimit()) {
            throw new LimitExceededException("Weekly publishing limit reached");
        }

        return true;
    }

}
