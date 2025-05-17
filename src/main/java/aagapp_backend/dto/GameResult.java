package aagapp_backend.dto;

import aagapp_backend.entity.players.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameResult {

    private Long gameId;
    private Long roomId;
    private String conclusionType;
    private Long gameWinner;
    private List<PlayerDtoWinner> players;
}
