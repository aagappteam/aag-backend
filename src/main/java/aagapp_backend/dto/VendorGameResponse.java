package aagapp_backend.dto;

import aagapp_backend.entity.game.Game;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class VendorGameResponse {
    private Long vendorId;
    private Integer dailyLimit;
    private Integer publishedLimit;
    private List<Map<String, String>> publishedGames;
    private List<Map<String, String> >games;
}
