package aagapp_backend.entity.players;

import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.enums.PlayerStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Getter
@Setter
public class
Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @Column(name = "player_id")
    private Long playerId;


    @NotNull(message = "Status cannot be null")
    @Column(name = "player_status", nullable = false)
    private PlayerStatus playerStatus;

    @ManyToOne
    @JoinColumn(name = "game_room_id")
    @JsonBackReference(value = "gameRoomReference")
    private GameRoom gameRoom;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void setDefaultStatus() {
        if (this.playerStatus == null) {
            this.playerStatus = PlayerStatus.READY_TO_PLAY;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }
}
