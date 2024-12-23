package aagapp_backend.entity.game;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "game_sessions")
@Getter
@Setter
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Game room cannot be null")
    @ManyToOne
    @JoinColumn(name = "game_room_id", nullable = false)
    private GameRoom gameRoom;  // The associated game room

    @NotNull(message = "Game state cannot be null")
    @Size(min = 5, max = 500, message = "Game state must be between 5 and 500 characters")
    @Column(name = "game_state", nullable = false, length = 500)
    private String gameState;  // Store the state of the game as a string or JSON

}
