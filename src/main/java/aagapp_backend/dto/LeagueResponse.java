package aagapp_backend.dto;

import aagapp_backend.enums.LeagueStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeagueResponse {

    private Long id;
    private String name;
    private String gameName;
    private List<TeamResponse> teams;
    private Long challengingVendorId;
    private String challengingVendorName;
    private String challengingVendorProfilePic;
    private Double fee;
    private Integer move;
    private LeagueStatus status;
    private String leagueUrl;
    private Long aagGameId;
    private ThemeResponse theme;
    private Integer minPlayersPerTeam;
    private Integer maxPlayersPerTeam;
    private String shareableLink;
    private ZonedDateTime scheduledAt;
    private ZonedDateTime endDate;
    private Long opponentVendorId;
    private String opponentVendorProfilePic;
    private String opponentVendorName;
    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;
}
