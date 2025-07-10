package aagapp_backend.services.analytics;

import aagapp_backend.dto.VendorAnalyticsResponse;
import aagapp_backend.entity.game.Game;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.social.UserVendorFollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VendorAnalyticsService {

    @Autowired
    private UserVendorFollowRepository followRepo;

    @Autowired
    private GameRepository gameRepo;

    public VendorAnalyticsResponse getVendorAnalytics(Long vendorId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDateTime startOfWeekTime = startOfWeek.atStartOfDay();

        Long totalFollowers = followRepo.countFollowersByVendorId(vendorId);
        int followersThisWeek = followRepo.countFollowersThisWeek(vendorId, startOfWeekTime);

        Long totalGames = gameRepo.countByVendorId(vendorId);
        int gamesThisWeek = gameRepo.countGamesThisWeek(vendorId, startOfWeekTime.atZone(ZoneId.systemDefault()));

        List<Game> allGames = gameRepo.findByVendorId(vendorId);

        Map<String, Long> timeBuckets = allGames.stream()
                .filter(game -> game.getCreatedDate() != null)
                .map(game -> game.getCreatedDate().withZoneSameInstant(ZoneId.systemDefault()).getHour())
                .map(hour -> {
                    if (hour >= 5 && hour < 12) return "morning";
                    else if (hour >= 12 && hour < 17) return "afternoon";
                    else if (hour >= 17 && hour < 21) return "evening";
                    else return "night";
                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        int totalTimeCount = allGames.size();
        Map<String, Double> percentageDistribution = new HashMap<>();
        if (totalTimeCount > 0) {
            timeBuckets.forEach((k, v) -> {
                percentageDistribution.put(k, (v * 100.0) / totalTimeCount);
            });
        }

        VendorAnalyticsResponse response = new VendorAnalyticsResponse();
        response.setVendorId(vendorId);
        response.setTotalFollowers(totalFollowers);
        response.setFollowersThisWeek(followersThisWeek);
        response.setTotalGamesPublished(totalGames);
        response.setGamesPublishedThisWeek(gamesThisWeek);
        response.setGamePublishingTimeDistribution(percentageDistribution);

        return response;
    }
}
