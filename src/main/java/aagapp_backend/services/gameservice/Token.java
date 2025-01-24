package aagapp_backend.services.gameservice;
public class Token {
    private Long playerId;
    private String tokenId;
    private int position;

    public Token(Long playerId, String tokenId, int position) {
        this.playerId = playerId;
        this.tokenId = tokenId;
        this.position = position;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}