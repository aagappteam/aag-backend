package aagapp_backend.dto;

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


}