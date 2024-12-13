package aagapp_backend.services.payment;

import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.payment.PaymentRepository;
import aagapp_backend.services.GameService.GameService;
import aagapp_backend.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import javax.naming.LimitExceededException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Optional;
import java.time.temporal.IsoFields;

@Service
public class PaymentFeatures {


    private GameRepository gameRepository;
    private GameService gameService;
    private PaymentRepository paymentRepository;

    @Autowired
    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Autowired
    public void setGameService(@Lazy GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    public ResponseEntity<?> canPublishGame(Long vendorId) throws LimitExceededException {

        try {
            PaymentStatus status = PaymentStatus.ACTIVE;
            Optional<PaymentEntity> activePlanOptional = paymentRepository.findActivePlanByVendorId(vendorId, LocalDateTime.now(), status);

            if (activePlanOptional.isEmpty()) {
                return ResponseService.generateErrorResponse(
                        "No active plan found for the vendor.",
                        HttpStatus.NOT_FOUND
                );
            }

            PaymentEntity activePlan = activePlanOptional.get();

            // Get the daily usage count of games published by the vendor
            int dailyUsage = gameService.countGamesByVendorIdAndScheduledDate(vendorId, LocalDate.now());


            // Get the current year and week number
         /*   int year = LocalDate.now().getYear();
            int weekOfYear = LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR); */// Correctly get the ISO week of the year

            // Get the weekly usage count of games published by the vendor
          /*  int weeklyUsage = gameService.countGamesByVendorIdAndWeek(vendorId, year, weekOfYear);*/

            System.out.println(dailyUsage +  " dailyUsage " );

            // Compare daily usage with the daily limit
            if (dailyUsage >= activePlan.getDailyLimit()) {
                return ResponseService.generateErrorResponse(
                        "Daily publishing limit reached. You cannot publish more games today.",
                        HttpStatus.FORBIDDEN
                );
            }

            return ResponseService.generateSuccessResponse(
                    "You can publish the game.",
                    activePlan,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            return ResponseService.generateErrorResponse(
                    "Error checking publishing limits: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }



}
