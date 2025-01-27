package aagapp_backend.services.gameservice;
public class Token {
    private String tokenId;
    private Long roomId;
    private Long playerId;
    private int position = 0;
    private boolean isAtHome = false;

/*    public Token(Long playerId,String tokenId, Long playerId) {
        this.roomId = roomId;
        this.tokenId = tokenId;
        this.playerId = playerId;
    }*/

    public Token(Long playerId, String tokenId, int position) {
        this.playerId = playerId;
        this.tokenId = tokenId;
        this.position = position;
    }

    public String getTokenId() {
        return tokenId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isAtHome() {
        return isAtHome;
    }

    public void setAtHome(boolean atHome) {
        isAtHome = atHome;
    }

    @Override
    public String toString() {
        return "Token{" +
                "playerId=" + playerId +
                ", tokenId='" + tokenId + '\'' +
                ", position=" + position +
                '}';
    }
}
