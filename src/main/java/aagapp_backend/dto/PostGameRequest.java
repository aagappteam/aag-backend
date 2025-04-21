package aagapp_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostGameRequest {

    private Long gameId;
    private int roomId;
    private List<PlayerScore> players;
}
