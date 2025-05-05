package aagapp_backend.entity.tournament;


import aagapp_backend.entity.league.League;
import aagapp_backend.entity.players.Player;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Table(
        name = "tournament_result_record"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResultRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;
    private String status = "READY_TO_PLAY";


    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal ammount;

    private Integer score;

    private Boolean isWinner;

    private Integer round;

    private LocalDateTime playedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime updatedDate;


    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }
}
