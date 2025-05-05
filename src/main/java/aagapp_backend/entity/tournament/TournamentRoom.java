package aagapp_backend.entity.tournament;

import aagapp_backend.entity.players.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tournament_room",
        indexes = {
                @Index(name = "idx_tournament_id", columnList = "tournament_id"),
//                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_gamepassword", columnList = "gamepassword")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    @JsonIgnore
    private Tournament tournament;

    private int maxParticipants;
    private int currentParticipants;
    private int round=1;

    @Column(name = "gamepassword", nullable = true)
    private String gamepassword;

    @JsonManagedReference
    @OneToMany(mappedBy = "tournamentRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Player> currentPlayers = new ArrayList<>();

    private String status;
}
