package aagapp_backend.entity.league;

import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.LeagueRoomStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "ludo_league_rooms",
        indexes = {
                @Index(name = "idx_room_code", columnList = "room_code"),
                @Index(name = "idx_status", columnList = "status"),
/*
                @Index(name = "idx_game_id_league", columnList = "game_id"),
*/
                /*@Index(name = "idx_current_player_id", columnList = "current_player_id"),*/
                @Index(name = "idx_created_at_league", columnList = "created_at"),
                @Index(name = "idx_game_id_status", columnList = "game_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class LeagueRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", unique = true, nullable = false, length = 10)
    private String roomCode;

    @Column(name = "gamepassword", nullable = true)
    private String gamepassword;

    private String gameType; // LUDO, SNAKE_LADDER, KNIFE_THROW, FRUIT_NINJA

    @OneToMany(mappedBy = "leagueRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "leagueRoomReference")
    private List<Player> currentPlayers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeagueRoomStatus status;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private League game;

    private Long currentPlayerId; // ID of the current player's turn

    @Column(name = "turn_order")
    private int turnOrder = 0;

    @ElementCollection
    @CollectionTable(name = "ludo_game_room_winners", joinColumns = @JoinColumn(name = "game_room_id"))
    @Column(name = "player_id")
    private Set<Long> winners = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "active_players_count")
    private int activePlayersCount;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.status == null) {
            this.status = LeagueRoomStatus.INITIALIZED;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.maxPlayers == 0) {
            this.maxPlayers = 2;
        }
        this.activePlayersCount = this.currentPlayers.size();
    }

    public Player getPlayerById(Long playerId) {
        for (Player player : currentPlayers) {
            if (player.getPlayerId().equals(playerId)) {
                return player;
            }
        }
        return null;  // Player not found
    }
}
