package aagapp_backend.dto;

import aagapp_backend.enums.GameStatus;
import aagapp_backend.enums.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TournamentResponseDTO {
    private Long id;
    private String name;
    private Integer fee;
    private Integer move;
    private TournamentStatus status;
    private String shareableLink;
    private Long aaggameid;

    private String themeName;
    private String themeImageUrl;

    private String scheduledAt;



}
