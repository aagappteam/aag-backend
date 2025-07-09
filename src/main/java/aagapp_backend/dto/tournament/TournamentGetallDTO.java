package aagapp_backend.dto.tournament;



import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.enums.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentGetallDTO {
    private Long id;
    private Long vendorId;
    private String vendorProfilePic;

    private String name;
    private Double totalPrizePool;
    private int round;
    private int totalrounds;
    private BigDecimal roomprize;
    private ThemeEntity theme;
    private Long existinggameId;
    private String gameUrl;
    private int participants;
    private int currentJoinedPlayers;
    private int entryFee;
    private int move;
    private TournamentStatus status;
    private String shareableLink;
    private ZonedDateTime createdDate;
    private ZonedDateTime scheduledAt;
    private ZonedDateTime updatedDate;
    private ZonedDateTime endDate;
    private ZonedDateTime statusUpdatedAt;
}

