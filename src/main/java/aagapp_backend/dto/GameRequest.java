package aagapp_backend.dto;

import aagapp_backend.entity.ThemeEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.ZonedDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameRequest {

    @NotNull
    private String name;

    private String description;

    @NotNull
    private Double entryFee;

    private Long themeId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;
}


