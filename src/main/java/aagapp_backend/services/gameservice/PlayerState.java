package aagapp_backend.services.gameservice;

import java.util.HashMap;
import java.util.Map;

public class PlayerState {
    private Long playerId;
    private String name;
    private Map<String, Integer> tokenPositions = new HashMap<>();
    private int homeCount = 0;
    private boolean hasWon;

    public PlayerState(Long playerId) {
        this.playerId = playerId;
    }

    public void updateTokenPosition(String tokenId, int position) {
        tokenPositions.put(tokenId, position);
        if (position == 100) { // Assuming 100 is home position
            homeCount++;
        }
    }

    public void resetTokenPosition(String tokenId) {
        tokenPositions.put(tokenId, 0);
    }

    public boolean isTokenInHome(String tokenId) {
        return tokenPositions.getOrDefault(tokenId, 0) == 100;
    }

    public boolean allTokensInHome() {
        return homeCount == tokenPositions.size();
    }
}
