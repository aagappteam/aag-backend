package aagapp_backend.entity.players;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Username cannot be null")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @NotNull(message = "Status cannot be null")
    @Column(name = "status", nullable = false, length = 20)
    private String status; // e.g., "waiting", "playing"

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public Player(Long id, String username, String status) {
        this.id = id;
        this.username = username;
        this.status = status;
    }

    public Player(){

    }
}
