package aagapp_backend.dto.tournament;

import aagapp_backend.enums.GameStatus;
import aagapp_backend.enums.TournamentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
public class TournamentResponseDTOCommon {

    private Long id;
    private String name;
    private String vendorname;
    private String totalprizepool;
    private String roundprize;
    private String gameIcon;
    private Integer fee;
    private Integer move;
    private TournamentStatus status;
    private String shareableLink;
    private Long aaggameid;

    private String themeName;
    private String themeImageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime scheduledAt;


}
