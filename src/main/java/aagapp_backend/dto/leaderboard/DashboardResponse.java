package aagapp_backend.dto.leaderboard;

import aagapp_backend.dto.GetGameResponseDTO;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private PageDTO<DashboardResponseDTO> latestGames;
    private PageDTO<PopularGameDTO> popularGames;
    private PageDTO<DashboardResponseDTO> entryFeeGames;
    private PageDTO<InfluencerGameDTO> influencerGames;
}
