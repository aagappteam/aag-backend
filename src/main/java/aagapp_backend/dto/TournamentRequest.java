package aagapp_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class TournamentRequest {
    private String name;
    private Double totalPrizePool;
    private Long themeId;
    private Long existinggameId;
    private int participants;
    @NotNull
    private int entryFee;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;

    public TournamentRequest(String name, Double totalPrizePool, Long themeId, Long existinggameId, int participants, int entryFee, ZonedDateTime scheduledAt) {
        this.name = name;
        this.totalPrizePool = totalPrizePool;
        this.themeId = themeId;
        this.existinggameId = existinggameId;
        this.participants = participants;
        this.entryFee = entryFee;
        this.scheduledAt = scheduledAt;
    }
}
