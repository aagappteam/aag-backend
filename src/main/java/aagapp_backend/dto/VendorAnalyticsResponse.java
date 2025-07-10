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
    private int followersThisWeek;
    private Long totalGamesPublished;
    private int gamesPublishedThisWeek;
    private Map<String, Double> gamePublishingTimeDistribution;
}

