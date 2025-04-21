package aagapp_backend.entity.game;


import aagapp_backend.enums.MoveType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long gameId;
    private Long playerId;
    private Integer moveNumber;
    private Integer diceRollValue;
    private String tokenMoved;

    @Enumerated(EnumType.STRING)
    private MoveType moveType; // Can be REGULAR, BOT_MOVE, AUTO_SKIP

    @ManyToOne
    @JoinColumn(name = "game_room_id", nullable = false)
    private GameRoom gameRoom;

}
