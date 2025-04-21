package aagapp_backend.dto;

import aagapp_backend.entity.VendorEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class LeagueRequest {

    private Long opponentVendorId;
    private Long themeId;
    private Long existinggameId;

    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;

    @NotNull
    private Double fee;

    private Integer move;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime endDate;
}
