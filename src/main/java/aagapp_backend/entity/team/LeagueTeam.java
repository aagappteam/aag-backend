package aagapp_backend.entity.team;


import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.players.Player;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "league_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeagueTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamName;

    private String profilePic;

    private Integer totalScore = 0;

    private Boolean isWinner = false;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    @JsonIgnore
    private VendorEntity vendor;

    @ManyToOne
    @JoinColumn(name = "league_id")
    @JsonIgnore
    private League league;


    @OneToMany(mappedBy = "team")
    @JsonManagedReference(value = "teamPlayers")
    @JsonIgnore
    private List<Player> players = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
