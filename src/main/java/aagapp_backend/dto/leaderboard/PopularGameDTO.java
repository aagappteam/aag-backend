package aagapp_backend.dto.leaderboard;

import aagapp_backend.dto.GetGameResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PopularGameDTO {
    private GetGameResponseDTO game;
    private long roomCount;
}