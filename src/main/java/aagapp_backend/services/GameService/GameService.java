package aagapp_backend.services.GameService;

import aagapp_backend.entity.game.Game;
import aagapp_backend.enums.GameStatus;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.payment.PaymentFeaturesRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.payment.PaymentFeatures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.naming.LimitExceededException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PaymentFeaturesRepository paymentFeaturesRepository;

    @Autowired
    private ResponseService responseService;

    public boolean canPublishGame(Long vendorId) {
        PaymentFeatures activePlan = paymentFeaturesRepository.findActivePlanByVendorId(vendorId,)
                .orElseThrow(() -> new ResourceNotFoundException("No active plan found"));

        return responseService.generateErrorResponse("You can send OTP only once in 1 minute", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);


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

    public Game publishGame(Game game) {
        canPublishGame(game.getVendorId());

        if (game.getScheduledAt() != null && game.getScheduledAt().isAfter(LocalDateTime.now())) {
            game.setStatus(GameStatus.SCHEDULED);
        } else {
            game.setStatus(GameStatus.ACTIVE);
            game.setScheduledAt(LocalDateTime.now());
        }

        game.setCreatedAt(LocalDateTime.now());
        game.setUpdatedAt(LocalDateTime.now());
        Game savedGame = gameRepository.save(game);

        // Generate and set the shareable link
        String shareableLink = generateShareableLink(savedGame.getId());
        savedGame.setShareableLink(shareableLink);
        return gameRepository.save(savedGame);
    }

    private String generateShareableLink(Long gameId) {
        return "https://example.com/games/" + gameId;
    }

    public Game getGameDetails(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found"));
    }

    public List<Game> getActiveGamesByVendorId(Long vendorId) {
        return gameRepository.findActiveGamesByVendorId(vendorId);
    }

    public List<Game> getScheduledGamesByVendorId(Long vendorId) {
        return gameRepository.findScheduledGamesByVendorId(vendorId);
    }

    public void updateGameStatuses() {
        List<Game> gamesToUpdate = gameRepository.findAll();
        gamesToUpdate.forEach(game -> {
            if (game.getScheduledAt().isBefore(LocalDateTime.now())) {
                if (game.getStatus() == GameStatus.SCHEDULED) {
                    game.setStatus(GameStatus.ACTIVE);
                } else if (game.getStatus() == GameStatus.ACTIVE &&
                        game.getScheduledAt().plusDays(1).isBefore(LocalDateTime.now())) {
                    game.setStatus(GameStatus.EXPIRED);
                }
                game.setUpdatedDate(LocalDateTime.now());
                gameRepository.save(game);
            }
        });
    }
}

