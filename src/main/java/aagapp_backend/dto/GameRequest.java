package aagapp_backend.dto;

import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.game.FeeToMove;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameRequest {

    @NotNull
    private String name;

    @NotNull
    private List<FeeToMove> feeToMoves;

    private Long themeId;

    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;
}


