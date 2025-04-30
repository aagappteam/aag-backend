package aagapp_backend.entity.tournament;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_round_participant", indexes = {
        @Index(name = "idx_tournament_round", columnList = "tournamentId, roundNumber"),
        @Index(name = "idx_tournament_player_round", columnList = "tournamentId, playerId, roundNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRoundParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tournamentId;

    private Long playerId;

    private String status = "READY_TO_PLAY";

    private Integer roundNumber;

    private LocalDateTime joinedAt;

}

