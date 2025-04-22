package aagapp_backend.dto.leaderboard;

import aagapp_backend.dto.GetGameResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InfluencerGameDTO {
    private GetGameResponseDTO game;
    private String influencerName;
}