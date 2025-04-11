package aagapp_backend.entity.game;

import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.GameRoomStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "game_room_winner",
        indexes = {
                @Index(name = "idx_game_id", columnList = "game_id"),  // Index on game_id
                @Index(name = "idx_player_id", columnList = "player_id")  // Index on player_id
        })
@Getter
@Setter
@NoArgsConstructor
public class GameRoomWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;  // Links to the Game entity

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Integer score;

    @Column(name = "win_timestamp", nullable = false)
    private LocalDateTime winTimestamp;


}
