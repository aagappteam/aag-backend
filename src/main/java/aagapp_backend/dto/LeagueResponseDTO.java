package aagapp_backend.dto;

import aagapp_backend.enums.GameStatus;
import aagapp_backend.enums.LeagueStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LeagueResponseDTO {

    private Long id;
    private String name;
    private Double fee;
    private Integer move;
    private LeagueStatus status;
    private String shareableLink;


    private String themeName;
    private String themeImageUrl;
    private String createdAt;

    private String scheduledAt;
    private String endDate;
    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;

    private String vendorName;
    private String vendorProfilePicUrl;
}
