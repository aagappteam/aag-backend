package aagapp_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LeaveRoomRequest {

    @NotNull(message="Player id can not be null")
    private Long playerId;

    @NotNull(message="Game id can not be null")
    private Long gameId;
}
