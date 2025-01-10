package aagapp_backend.entity.game;

import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.GameRoomStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
@Table(name = "game_rooms")
@Getter
@Setter
@NoArgsConstructor
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "room_code", unique = true, nullable = false, length = 10)
    private String roomCode;

    @OneToMany(mappedBy = "gameRoom", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "gameRoomReference")
    private List<Player> players = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameRoomStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "max_players", nullable = false)
    private int maxPlayers;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;


    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "gameRoomReference")
    private List<Player> currentPlayers = new ArrayList<>();


    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.status == null) {
            this.status = GameRoomStatus.INITIALIZED;
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.maxPlayers == 0) {
            this.maxPlayers = 2;
        }
//        this.currentPlayersCount = this.currentPlayers.size();


    }


}
