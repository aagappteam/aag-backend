package aagapp_backend.entity.game;

import aagapp_backend.entity.players.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_rooms")
@Getter
@Setter
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Room code cannot be null")
    @Column(name = "room_code", unique = true, nullable = false, length = 10)
    private String roomCode;

    @ManyToOne
    @JoinColumn(name = "player1_id", nullable = false)
    private Player player1;

    @ManyToOne
    @JoinColumn(name = "player2_id", nullable = true)
    private Player player2;

    @NotNull(message = "Status cannot be null")
    @Column(name = "status", nullable = false, length = 20)
    private String status;    // e.g., "waiting", "ongoing", "completed"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
