package aagapp_backend.services.payment;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.payment.PaymentEntity;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.payment.PaymentRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import javax.naming.LimitExceededException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentFeatures {
    private GameRepository gameRepository;
    private GameService gameService;
    private PaymentRepository paymentRepository;
    @Autowired
    private VendorRepository vendorRepository;

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
            List<PaymentEntity> activePlanOptional = paymentRepository.findActivePlanByVendorId(vendorId, LocalDateTime.now(), status);


            VendorEntity vendor = vendorRepository.findById(vendorId).orElse(null);
            if (activePlanOptional.isEmpty()) {
                return ResponseService.generateErrorResponse(
                        "No active plan found for the vendor.",
                        HttpStatus.NOT_FOUND
                );
            }

            PaymentEntity activePlan = activePlanOptional.get(0);

            Optional<VendorEntity> vendorOpt1 = vendorRepository.findByMobileNumber(Constant.MOBILE_6306470701);
            Optional<VendorEntity> vendorOpt2 = vendorRepository.findByMobileNumber(Constant.MOBILE_9628577197);

            if ((vendorOpt1.isPresent() && vendorId.equals(vendorOpt1.get().getService_provider_id())) ||
                    (vendorOpt2.isPresent() && vendorId.equals(vendorOpt2.get().getService_provider_id()))) {

                return ResponseService.generateSuccessResponse(
                        "You can publish the game (bypass limit).",
                        activePlan,
                        HttpStatus.OK
                );
            }


            int dailyLimit = vendor.getPublishedLimit() != null ? vendor.getPublishedLimit() : 0;
            System.out.println("Daily Usage: " + dailyLimit);

            System.out.println("Daily Usage: " + vendor.getDailyLimit() );

            System.out.println("Daily Limit: " + activePlan.getDailyLimit());

            if (dailyLimit >= activePlan.getDailyLimit()) {
                return ResponseService.generateErrorResponse(
                        "Daily limit reached. You can't publish more games today.",
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
