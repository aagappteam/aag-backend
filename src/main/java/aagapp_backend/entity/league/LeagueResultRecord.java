package aagapp_backend.entity.league;

import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.team.LeagueTeam;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "league_result_record",
        indexes = {
                @Index(name = "idx_league_id", columnList = "league_id"),
                @Index(name = "idx_player_id_league_result_record", columnList = "player_id"),
                @Index(name = "idx_room_id", columnList = "roomId"),
                @Index(name = "idx_played_at", columnList = "playedAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeagueResultRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Room id can not be null")
    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "league_id", nullable = false)
    @NotNull
    private League league;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Integer totalScore=0;

    @ManyToOne
    @JoinColumn(name = "league_team_id")
    @NotNull(message = "Team id can not be null")
    private LeagueTeam leagueTeam;


    private Boolean isWinner;

    private LocalDateTime playedAt;
}