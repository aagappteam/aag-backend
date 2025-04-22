package aagapp_backend.dto.leaderboard;


import aagapp_backend.enums.GameStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
public class DashboardResponseDTO {

    private Long id;
    private String gamename;
    private Double fee;

    private Long aaggameid;
    private String gameIcon;
    private String themeName;
    private String themeImageUrl;


    private String vendorName;
    private String vendorProfilePicUrl;


}