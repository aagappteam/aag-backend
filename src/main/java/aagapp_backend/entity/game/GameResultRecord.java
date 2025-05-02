package aagapp_backend.entity.game;

import aagapp_backend.entity.players.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_result_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameResultRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "winning_ammount", nullable = true)
    private BigDecimal winningammount;

    private Integer score;

    private Boolean isWinner;

    private LocalDateTime playedAt;
}
