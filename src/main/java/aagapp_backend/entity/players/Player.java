package aagapp_backend.entity.players;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.game.Token;
import aagapp_backend.entity.league.LeagueRoom;
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
                @Index(name = "idx_player_status", columnList = "player_status"),
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
    private Long playerId;  // Same as Customer ID

    @OneToOne
    @MapsId
    @JoinColumn(name = "player_id") // Link to Customer's ID
    @JsonIgnore
    private CustomCustomer customer;

    private String playerName;

    private String playerProfilePic;

    /*@NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "player_status", nullable = false)
    private PlayerStatus playerStatus;*/

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "game_room_id")
    @JsonBackReference(value = "gameRoomReference")
    private GameRoom gameRoom;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "league_room_id")
    private LeagueRoom leagueRoom;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "tournament_room_id")
    private TournamentRoom tournamentRoom;

    /*@ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private CustomCustomer customer;*/


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
    public String getPlayerProfilePic() {
        if (playerProfilePic != null) {
            return playerProfilePic;
        } else if (customer != null && customer.getProfilePic() != null) {
            return customer.getProfilePic();
        }
        return Constant.PROFILE_IMAGE_URL;
    }

    public String getPlayerName() {
        if (playerName != null) {
            return playerName;
        } else if (customer != null && customer.getName() != null) {
            return customer.getName();
        }
        return "Aag User";
    }


    @PrePersist
    public void setDefaultStatus() {
        /*if (this.playerStatus == null) {
            this.playerStatus = PlayerStatus.READY_TO_PLAY;
        }*/
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }

    }
}