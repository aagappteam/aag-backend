package aagapp_backend.entity.game;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.GameRoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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


    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    private List<Player> players;


    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameRoomStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}
