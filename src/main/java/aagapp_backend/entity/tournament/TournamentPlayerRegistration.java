package aagapp_backend.entity.tournament;


import aagapp_backend.entity.players.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentPlayerRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    @OnDelete(action = OnDeleteAction.NO_ACTION) // or CASCADE if you want auto delete
    private Player player;


    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    public enum RegistrationStatus {
        REGISTERED, CANCELLED,ACTIVE
    }



}
