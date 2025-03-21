package aagapp_backend.dto;

import aagapp_backend.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameRequestDTO {
    private String gameName;
    private String gameImage;
    private List<ThemeRequestDTO> themes;  // List of themes with id and imageUrl
    private GameStatus gameStatus;
    private Integer minRange;
    private Integer maxRange;
    private List<PriceRequestDTO> prices;
}
