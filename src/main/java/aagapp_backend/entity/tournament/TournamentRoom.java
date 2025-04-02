package aagapp_backend.entity.tournament;

import aagapp_backend.entity.players.Player;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournament_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tournamentId;
    private int maxParticipants;
    private int currentParticipants;
    private int round; // Track current round (e.g., 1, 2, 3)

    @OneToMany(mappedBy = "tournamentRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Player> currentPlayers = new ArrayList<>();

    private String status; // OPEN, IN_PROGRESS, COMPLETED

    // Getters and Setters
}
