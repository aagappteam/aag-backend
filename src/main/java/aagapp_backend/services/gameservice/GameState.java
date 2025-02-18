package aagapp_backend.services.gameservice;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.game.Token;
import aagapp_backend.entity.players.Player;
import aagapp_backend.enums.GameRoomStatus;
import aagapp_backend.enums.GameStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class GameState {
    private Long gameId;
    private Long currentPlayerId;
    private Map<Long, List<Token>> playerTokens; // Player ID -> List of Tokens
    private int boardSize = 100; // Example board size
    private Map<Long, Integer> finalHomePositions; // Player ID -> Final Home Position
    private Map<Long, Integer> playerTokensAtHome; // Player ID -> Number of tokens at home
    private Set<Long> winners; // Store the winners
//    private Enum gameStatus = GameRoomStatus.ONGOING;
private List<Player> players; //  Store players for easy lookup

    private GameRoomStatus gameStatus = GameRoomStatus.ONGOING;

    @Autowired
    private LudoGameService ludogameservice;

    public GameState(Long gameId, GameRoom gameRoom) {
        this.gameId = gameId;
        this.playerTokens = new HashMap<>();
        this.finalHomePositions = new HashMap<>();
        this.playerTokensAtHome = new HashMap<>();
        this.winners = new HashSet<>();
        this.gameStatus = GameRoomStatus.ONGOING;
        this.players = new ArrayList<>(gameRoom.getCurrentPlayers());

        for (Player player : gameRoom.getCurrentPlayers()) {
            Long playerId = player.getPlayerId();
            playerTokens.put(playerId, initializeTokens(player));
            finalHomePositions.put(playerId, boardSize);
            playerTokensAtHome.put(playerId, 0);

        }

        this.currentPlayerId = gameRoom.getCurrentPlayers().get(0).getPlayerId(); // Start with the first player
    }

    // Updated initializeTokens method
    private List<Token> initializeTokens(Player player) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 1; i <= Constant.TOKEN_SIZE; i++) {
            String uniqueTokenId = player.getPlayerId() + "_Token" + i;
            tokens.add(new Token(player.getPlayerId(), uniqueTokenId, 0));
        }
        return tokens;
    }


    public Long getGameId() {
        return gameId;
    }

    public Long getCurrentPlayerId() {
        return currentPlayerId;
    }
    


    public void setCurrentPlayerId(Long nextPlayerId) {
        this.currentPlayerId = nextPlayerId;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public Token getPlayerToken(Long playerId, String tokenId) {
        return playerTokens.get(playerId).stream()
                .filter(token -> token.getTokenId().equals(tokenId))
                .findFirst()
                .orElse(null);
    }


    public void addWinner(Long playerId) {
        if (!winners.contains(playerId)) {
            winners.add(playerId);
        }
    }

    public List<Token> getPlayerTokens(Long playerId) {
        return playerTokens.get(playerId);
    }

    public int getFinalHomePosition(Long playerId) {
        return finalHomePositions.getOrDefault(playerId, boardSize);
    }

    public boolean hasPlayerWon(Long playerId) {
        int tokensAtHomeToWin = 4;
        return playerTokensAtHome.getOrDefault(playerId, 0) >= tokensAtHomeToWin;

    }


    public void markTokenHome(Token token) {
        List<Token> tokens = playerTokens.get(token.getPlayerId());
        if (tokens != null) {
            token.setPosition(-1); // Mark token as "at home" (use -1 as the home position indicator)
            int currentHomeCount = playerTokensAtHome.getOrDefault(token.getPlayerId(), 0);
            playerTokensAtHome.put(token.getPlayerId(), currentHomeCount + 1);

            // Check if the player has won
            if (hasPlayerWon(token.getPlayerId())) {
                winners.add(token.getPlayerId());
            }
        }
    }

    public void sendTokenHome(Token token) {
        token.setPosition(0); // Send token back to start
    }

    public Token getTokenAtPosition(int position) {
        for (List<Token> tokens : playerTokens.values()) {
            for (Token token : tokens) {
                if (token.getPosition() == position) {
                    return token;
                }
            }
        }
        return null;
    }

    public Long getNextPlayerId() {
        List<Long> playerIds = new ArrayList<>(playerTokens.keySet());
        int currentIndex = playerIds.indexOf(currentPlayerId);
        return playerIds.get((currentIndex + 1) % playerIds.size());
    }

    public Set<Long> getWinners() {
        return winners;
    }

    public Enum getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameRoomStatus gameStatus) {
        this.gameStatus = gameStatus;
    }



    public Player getPlayerById(Long playerId) {
        return players.stream()
                .filter(player -> player.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

}
