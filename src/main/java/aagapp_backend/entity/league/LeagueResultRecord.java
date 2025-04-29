package aagapp_backend.entity.league;

import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.team.LeagueTeam;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Integer totalScore=0;

    @ManyToOne
    @JoinColumn(name = "league_team_id")
    private LeagueTeam leagueTeam;


    private Boolean isWinner;

    private LocalDateTime playedAt;
}