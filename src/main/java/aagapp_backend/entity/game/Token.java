package aagapp_backend.entity.game;
import aagapp_backend.entity.players.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tokenId;

    private Long roomId;

    private int position = 0;

    @Column(name = "move_count")
    private int moveCount = 0;

    @Column(name = "move_type")
    private String moveType;

    private boolean isHome;

    @Column(name = "is_active")
    private boolean isActive = true;

    private Long playerId;

    private int homeColumnEntry; // Position where home column starts

    private int homeColumnPosition = 0; // Tracks movement inside the home column

    public Token(Long playerId, String tokenId, int position) {
        this.playerId = playerId;
        this.tokenId = tokenId;
        this.position = position;
        this.isActive = false;
    }

}
