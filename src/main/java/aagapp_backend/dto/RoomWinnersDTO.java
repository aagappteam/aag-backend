package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class RoomWinnersDTO {
    private String roomCode;
    private Long gameRoomId;
    private List<PlayerResponseDTO> winners;
    public RoomWinnersDTO(String roomCode, Long gameRoomId, List<PlayerResponseDTO> winners) {
        this.roomCode = roomCode;
        this.gameRoomId = gameRoomId;
        this.winners = winners;
    }
}