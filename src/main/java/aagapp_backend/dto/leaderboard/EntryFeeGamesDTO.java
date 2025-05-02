package aagapp_backend.dto.leaderboard;

import aagapp_backend.dto.GetGameResponseDTO;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntryFeeGamesDTO {
    private PageDTO<GetGameResponseDTO> low;
    private PageDTO<GetGameResponseDTO> medium;
    private PageDTO<GetGameResponseDTO> high;
}