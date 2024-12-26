package aagapp_backend.entity.players;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.enums.PlayerStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "players")
@Getter
@Setter
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name="id", nullable = false)
    @JsonBackReference
    private CustomCustomer customCustomer;


    @NotNull(message = "Status cannot be null")
    @Column(name = "status", nullable = false, length = 20)
    private String status; // e.g., "waiting", "playing"

    @ManyToOne
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    @PrePersist
    public void setDefaultStatus() {
        if (this.status == null) {
            this.status = String.valueOf(PlayerStatus.WAITING);
        }
    }
}
