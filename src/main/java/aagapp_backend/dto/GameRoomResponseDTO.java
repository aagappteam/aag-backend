package aagapp_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GameRoomResponseDTO {
    private Long gameId;
    private Long roomId;
    private String gamePassword;
    private Integer moves;
    private BigDecimal totalPrize;
    private int maxParticipants;
}
