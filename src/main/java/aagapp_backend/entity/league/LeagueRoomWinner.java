package aagapp_backend.entity.league;


import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.players.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        indexes = {
//                @Index(name = "idx_league_id", columnList = "league_id"),
//                @Index(name = "idx_league_room_id", columnList = "league_room_id"),
//                @Index(name = "idx_player_id", columnList = "player_id"),
                @Index(name = "idx_win_timestamp", columnList = "win_timestamp")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class LeagueRoomWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "league_id", nullable = false)
    private League league;  // Links to the Game entity

    @ManyToOne
    @JoinColumn(name = "league_room_id", nullable = false)
    private LeagueRoom leagueRoom;


    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Integer score;

    @Column(name = "win_timestamp", nullable = false)
    private LocalDateTime winTimestamp;


}
