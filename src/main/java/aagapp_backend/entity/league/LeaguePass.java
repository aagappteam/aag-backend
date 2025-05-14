package aagapp_backend.entity.league;

import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.team.LeagueTeam;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaguePass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Player player;

    private Long selectedTeamId;

    @ManyToOne
    private League league;

    private int passCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private ZonedDateTime updatedDate;


    @PreUpdate
    public void preUpdate() {
        this.updatedDate = ZonedDateTime.now();
    }

}
