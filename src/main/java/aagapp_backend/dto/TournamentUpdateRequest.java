package aagapp_backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentUpdateRequest {

    private Long existinggameId;
    private Integer participants;
    private Integer entryFee;
    private BigDecimal roomprize;
    private Double totalPrizePool;
    private Integer move;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;
}
