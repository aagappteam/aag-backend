package aagapp_backend.entity.game;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "game_sessions")
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Game room cannot be null")
    @ManyToOne
    @JoinColumn(name = "game_room_id", nullable = false)  // Foreign key column name
    private GameRoom gameRoom;  // The associated game room

    @NotNull(message = "Game state cannot be null")
    @Size(min = 5, max = 500, message = "Game state must be between 5 and 500 characters")
    @Column(name = "game_state", nullable = false, length = 500)
    private String gameState;  // Store the state of the game as a string or JSON

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public GameSession(){

    }

    public GameSession(Long id, GameRoom gameRoom, String gameState) {
        this.id = id;
        this.gameRoom = gameRoom;
        this.gameState = gameState;
    }

    @Override
    public String toString() {
        return "GameSession{" +
                "id=" + id +
                ", gameRoom=" + gameRoom +
                ", gameState='" + gameState + '\'' +
                '}';
    }


}
