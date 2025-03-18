package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameResponse {
    private Long gameId;
    private String gameImage;
    private String gameName;
}
