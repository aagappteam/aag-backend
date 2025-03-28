package aagapp_backend.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlayerScore {
    private int playerId;
    private int score;
    private int prize;
}
