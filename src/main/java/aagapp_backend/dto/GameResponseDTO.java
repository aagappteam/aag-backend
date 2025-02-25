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
    private GameStatus gameStatus;

    public GameResponseDTO(AagAvailableGames game) {
        this.gameId = game.getId();
        this.gameName = game.getGameName();
        this.gameImage = game.getGameImage();
        this.gameStatus = game.getGameStatus();
        this.themes = game.getThemes().stream()
                .map(theme -> new ThemeResponseDTO(theme))
                .collect(Collectors.toList());
    }
}
