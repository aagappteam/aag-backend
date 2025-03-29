package aagapp_backend.dto;

import aagapp_backend.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetGameResponseDTO {

    private Long id;
    private Double fee;
    private Integer move;
    private GameStatus status;
    private String shareableLink;
    private Long aaggameid;
    private String gameIcon;

    private String themeName;
    private String themeImageUrl;

    private String scheduledAt;
    private String endDate;
    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;

    private String vendorName;
    private String vendorProfilePicUrl;


}