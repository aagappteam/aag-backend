package aagapp_backend.entity.tournament;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tournament_round_winner",
        indexes = {
                @Index(name = "idx_tournament_id_tournament_round_winner", columnList = "tournamentId"),
                @Index(name = "idx_player_id_tournament_round_winner", columnList = "playerId"),
                @Index(name = "idx_round_number", columnList = "roundNumber")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRoundWinner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tournamentId;
    private Long playerId;
    private Integer roundNumber;

}
