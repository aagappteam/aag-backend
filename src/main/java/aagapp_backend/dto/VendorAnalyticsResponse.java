package aagapp_backend.dto;

import lombok.*;
import org.checkerframework.checker.units.qual.N;

import java.util.Map;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorAnalyticsResponse {
    private Long vendorId;

    private Long totalFollowers;
    private Long followersInRange;

    private Long totalGames;
    private Long gamesInRange;

    private Long totalLeagues;
    private Long leaguesInRange;

    private Long totalTournaments;
    private Long tournamentsInRange;

    private Map<String, Double> publishingTimeDistribution;
}

