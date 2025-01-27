package aagapp_backend.controller.game;

import aagapp_backend.handler.GameWebSocketHandler;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.gameservice.LudoGameService;
import aagapp_backend.services.ludo.PCGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("ludo-game")

public class LudoGame {

    @Autowired
    private LudoGameService gameService;


    private final PCGService pcgService;

    public LudoGame(PCGService pcgService) {
        this.pcgService = pcgService;
    }

    @GetMapping("/roll-dice")
    public int rollDice(@RequestParam(required = false) Long seed) {
        if (seed == null) {
            seed = System.currentTimeMillis();
        }

        PCGService service = new PCGService();
        return service.nextInt(); // Generate and return random number between 1 and 6
    }


    @MessageMapping("/rollDice")
    @SendTo("/topic/game")
    public String rollDice(GameWebSocketHandler.GameMessage message) {
        int diceValue = gameService.rollDice();
        return "Player " + message.getPlayerId() + " rolled a " + diceValue;
    }

    @MessageMapping("/moveToken")
    @SendTo("/topic/game")
    public String moveToken(GameWebSocketHandler.GameMessage message) {
        return gameService.moveToken(
                message.getRoomId(),
                message.getGameId(),
                message.getPlayerId(),
                message.getTokenId(),
                message.getNewPosition()
        );
    }
}


