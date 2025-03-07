package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GameVendorDTO {
    private Long gameId;
    private String gameName;
    private ZonedDateTime scheduledAt;
    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;
    private ZonedDateTime endDate;
    private Double fee;
    private Integer move;
    private String status;
    private String shareableLink;

    // Vendor details
    private String vendorFirstName;
    private String vendorLastName;
    private String vendorProfilePicture;
    private String vendorMobileNumber;
    private String vendorEmail;
    private Boolean vendorIsVerified;

    // Theme details
    private Long themeId;
    private String themeImageUrl;

    // Game participation details
    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;
    private Boolean isGameLive;
}
