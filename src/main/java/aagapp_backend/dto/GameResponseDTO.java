package aagapp_backend.dto;

import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GameResponseDTO {
    private Long gameId;
    private String gameName;
    private String gameImage;
    private List<ThemeResponseDTO> themes;
    private List<PriceResponseDTO> prices;  // Added field for prices>
    private GameStatus gameStatus;
    private Integer minRange;   // Added field for minRange
    private Integer maxRange;   // Added field for maxRange

    public GameResponseDTO(AagAvailableGames game) {
        this.gameId = game.getId();
        this.gameName = game.getGameName();
        this.gameImage = game.getGameImage();
        this.gameStatus = game.getGameStatus();

        // Added mapping for minRange and maxRange
        this.minRange = game.getMinRange();
        this.maxRange = game.getMaxRange();

        // Map the themes to the ThemeResponseDTO
        this.themes = game.getThemes().stream()
                .map(theme -> new ThemeResponseDTO(theme))
                .collect(Collectors.toList());

        this.prices = game.getPrice().stream()
                .map(price -> new PriceResponseDTO(price))
                .collect(Collectors.toList());
    }
}
