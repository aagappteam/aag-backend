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
    private List<PlayerDto> players;
    private Double entryFee;
    private Long vendorId; // New field to store the vendorId
}
