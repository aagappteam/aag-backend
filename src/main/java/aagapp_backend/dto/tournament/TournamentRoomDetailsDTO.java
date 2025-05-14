package aagapp_backend.dto.tournament;

import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.TournamentRoom;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TournamentRoomDetailsDTO {
    private Long id;
    private int maxParticipants;
    private int currentParticipants;
    private int round;
    private String gamepassword;
    private List<Player> currentPlayers;
    private String status;
    private ZonedDateTime updatedDate;
    private String tournamentStatus;  // <- extra field

    // Constructor
    public TournamentRoomDetailsDTO(TournamentRoom room, String tournamentStatus) {
        this.id = room.getId();
        this.maxParticipants = room.getMaxParticipants();
        this.currentParticipants = room.getCurrentParticipants();
        this.round = room.getRound();
        this.gamepassword = room.getGamepassword();
        this.currentPlayers = room.getCurrentPlayers();
        this.status = room.getStatus();
        this.updatedDate = room.getUpdatedDate();
        this.tournamentStatus = tournamentStatus;
    }

}
