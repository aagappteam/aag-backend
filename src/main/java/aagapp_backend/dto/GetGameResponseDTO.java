package aagapp_backend.dto;

import aagapp_backend.entity.game.Game;
import aagapp_backend.enums.GameStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
public class GetGameResponseDTO {

    private Long id;
    private String gamename;
    private Double fee;
    private Integer move;
    private GameStatus status;
    private String shareableLink;
    private Long aaggameid;
    private String gameIcon;


    private String themeName;
    private String themeImageUrl;
   /* private String createdAt;*/

/*    private String scheduledAt;
    private String endDate;*/

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime endDate;

    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;

    private String vendorName;
    private String vendorProfilePicUrl;


    public GetGameResponseDTO(Game game) {
        this.id = game.getId();
        this.gamename = game.getName();
        this.fee = game.getFee();
        this.move = game.getMove();
        this.status = game.getStatus();
        this.shareableLink = game.getShareableLink();
        this.aaggameid = game.getAaggameid();
        this.gameIcon = game.getImageUrl();
        this.themeName = game.getTheme() != null ? game.getTheme().getName() : null;
        this.themeImageUrl = game.getTheme() != null ? game.getTheme().getImageUrl() : null;
        this.createdAt = game.getCreatedDate();
        this.scheduledAt = game.getScheduledAt();
        this.endDate = game.getEndDate();
        this.minPlayersPerTeam = game.getMinPlayersPerTeam();
        this.maxPlayersPerTeam = game.getMaxPlayersPerTeam();
        this.vendorName = game.getVendorEntity() != null ? game.getVendorEntity().getName() : null;
        this.vendorProfilePicUrl = game.getVendorEntity() != null ? game.getVendorEntity().getProfilePic() : null;
    }
}