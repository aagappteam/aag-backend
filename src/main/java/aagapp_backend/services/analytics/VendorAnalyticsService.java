package aagapp_backend.services.analytics;

import aagapp_backend.dto.VendorAnalyticsResponse;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.social.UserVendorFollowRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VendorAnalyticsService {

    @Autowired
    private UserVendorFollowRepository followRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private LeagueRepository leagueRepo;
    @Autowired
    private TournamentRepository tournamentRepo;

    public VendorAnalyticsResponse getVendorAnalytics(Long vendorId, LocalDate startDate, LocalDate endDate) {
        ZoneId zone = ZoneId.of("Asia/Kolkata");

        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now(zone);
            DayOfWeek currentDay = today.getDayOfWeek();
            int diffToMonday = currentDay.getValue() - DayOfWeek.MONDAY.getValue();
            startDate = today.minusDays(diffToMonday);
            endDate = today; // Today
        }

        ZonedDateTime startDateTime = startDate.atStartOfDay(zone);
        ZonedDateTime endDateTime = endDate.atTime(23, 59, 59).atZone(zone);


        long totalFollowers = followRepo.countFollowersByVendorId(vendorId);
        long followersInRange = (startDate != null && endDate != null)
                ? followRepo.countFollowersBetweenDates(vendorId, startDate.atStartOfDay(), endDate.atTime(23,59,59))
                : totalFollowers;

        long totalGames = gameRepo.countByVendorId(vendorId);
        long gamesInRange = (startDate != null && endDate != null)
                ? Optional.ofNullable(gameRepo.countGamesBetweenDates(vendorId, startDateTime, endDateTime)).orElse(0L)
                : totalGames;

        long totalLeagues = leagueRepo.countByVendorId(vendorId);
        long leaguesInRange = (startDate != null && endDate != null)
                ? Optional.ofNullable(leagueRepo.countLeaguesBetweenDates(vendorId, startDateTime, endDateTime)).orElse(0L)
                : totalLeagues;

        long totalTournaments = tournamentRepo.countByVendorId(vendorId);
        long tournamentsInRange = (startDate != null && endDate != null)
                ? Optional.ofNullable(tournamentRepo.countTournamentsBetweenDates(vendorId, startDateTime, endDateTime)).orElse(0L)
                : totalTournaments;

        List<ZonedDateTime> timestamps = new ArrayList<>();
        if (startDate != null && endDate != null) {
            timestamps.addAll(gameRepo.findGamesBetweenDates(vendorId, startDateTime, endDateTime)
                    .stream().map(Game::getCreatedDate).filter(Objects::nonNull).toList());
            timestamps.addAll(leagueRepo.findLeaguesBetweenDates(vendorId, startDateTime, endDateTime)
                    .stream().map(League::getCreatedDate).filter(Objects::nonNull).toList());
            timestamps.addAll(tournamentRepo.findTournamentsBetweenDates(vendorId, startDateTime, endDateTime)
                    .stream().map(Tournament::getCreatedDate).filter(Objects::nonNull).toList());
        } else {
            timestamps.addAll(gameRepo.findByVendorId(vendorId)
                    .stream().map(Game::getCreatedDate).filter(Objects::nonNull).toList());
            timestamps.addAll(leagueRepo.findByVendorId(vendorId)
                    .stream().map(League::getCreatedDate).filter(Objects::nonNull).toList());
            timestamps.addAll(tournamentRepo.findByVendorId(vendorId)
                    .stream().map(Tournament::getCreatedDate).filter(Objects::nonNull).toList());
        }

        Map<String, Double> timeDist = calculateTimeDistribution(timestamps, zone);

        VendorAnalyticsResponse resp = new VendorAnalyticsResponse();
        resp.setVendorId(vendorId);
        resp.setTotalFollowers(totalFollowers);
        resp.setFollowersInRange(followersInRange);
        resp.setTotalGames(totalGames);
        resp.setGamesInRange(gamesInRange);
        resp.setTotalLeagues(totalLeagues);
        resp.setLeaguesInRange(leaguesInRange);
        resp.setTotalTournaments(totalTournaments);
        resp.setTournamentsInRange(tournamentsInRange);
        resp.setPublishingTimeDistribution(timeDist);

        return resp;
    }

    private Map<String, Double> calculateTimeDistribution(List<ZonedDateTime> timestamps, ZoneId zone) {
        // Initialize with default values
        Map<String, Double> dist = new HashMap<>();
        dist.put("morning", 0.0);
        dist.put("afternoon", 0.0);
        dist.put("evening", 0.0);
        dist.put("night", 0.0);

        // Group by time bucket
        Map<String, Long> buckets = timestamps.stream()
                .map(ts -> ts.withZoneSameInstant(zone).getHour())
                .map(hour -> {
                    if (hour >= 5 && hour < 12) return "morning";
                    else if (hour >= 12 && hour < 17) return "afternoon";
                    else if (hour >= 17 && hour < 21) return "evening";
                    else return "night";
                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        int total = timestamps.size();
        if (total > 0) {
            buckets.forEach((k, v) -> {
                double percentage = v * 100.0 / total;
                double rounded = Math.round(percentage * 100.0) / 100.0; // Round to 2 decimal places
                dist.put(k, rounded);
            });
        }

        return dist;
    }

}
