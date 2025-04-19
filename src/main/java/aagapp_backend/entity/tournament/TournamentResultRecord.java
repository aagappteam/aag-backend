package aagapp_backend.entity.tournament;


import aagapp_backend.entity.league.League;
import aagapp_backend.entity.players.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_result_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResultRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private Integer score;

    private Boolean isWinner;

    private LocalDateTime playedAt;
}
