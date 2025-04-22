package aagapp_backend.entity.team;


import aagapp_backend.entity.league.League;
import aagapp_backend.entity.players.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamChallengingVendorName;

    private Integer teamChallengingVendorScore;

    private String teamOpponentVendorName;

    private Integer teamOpponentVendorScore;


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;


    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;

}
