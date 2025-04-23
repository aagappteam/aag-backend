package aagapp_backend.entity.players;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.team.LeagueTeam;
import aagapp_backend.entity.tournament.TournamentRoom;
import aagapp_backend.enums.PlayerStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.action.internal.OrphanRemovalAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "players",
        indexes = {
                @Index(name = "idx_player_name", columnList = "playerName"),
                @Index(name = "idx_game_room_id", columnList = "game_room_id"),
                @Index(name = "idx_league_room_id", columnList = "league_room_id"),
                @Index(name = "idx_tournament_room_id", columnList = "tournament_room_id")
        }
)
@Getter
@Setter
public class Player {
    @Id
    private Long playerId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "player_id")
    @JsonIgnore
    private CustomCustomer customer;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "game_room_id")
    @JsonBackReference(value = "gameRoomReference")
    private GameRoom gameRoom;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "league_room_id")
    private LeagueRoom leagueRoom;

    @ManyToOne
    @JoinColumn(name = "team_id")
//    @JsonBackReference(value = "teamPlayers")
    private LeagueTeam team;


    @Column(name = "league_passes", nullable = false)
    private int leaguePasses=0;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "tournament_room_id")
    private TournamentRoom tournamentRoom;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
    public String getPlayerProfilePic() {
        if (customer != null && customer.getProfilePic() != null) {
            return customer.getProfilePic();
        }
        return Constant.PROFILE_IMAGE_URL;
    }

    public String getPlayerName() {
        if (customer != null && customer.getName() != null) {
            return customer.getName();
        }
        return "Aag User";
    }


    @PrePersist
    public void setDefaultStatus() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }

    }
}