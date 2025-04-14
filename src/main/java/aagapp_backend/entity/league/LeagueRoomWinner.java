package aagapp_backend.entity.league;


import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.players.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
/*@Table(name = "game_room_winner",
        indexes = {
//                @Index(name = "idx_game_id", columnList = "game_id"),  // Index on game_id
                @Index(name = "idx_player_id", columnList = "player_id")  // Index on player_id
        })*/
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
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Integer score;

    @Column(name = "win_timestamp", nullable = false)
    private LocalDateTime winTimestamp;


}
