package aagapp_backend.controller.game;

import aagapp_backend.entity.game.Game;
import aagapp_backend.services.GameService.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping
    public ResponseEntity<Game> publishGame(@RequestBody Game game) {
        Game publishedGame = gameService.publishGame(game);
        return ResponseEntity.status(HttpStatus.CREATED).body(publishedGame);
    }

    @GetMapping("/getgamebyid{gameId}")
    public ResponseEntity<Game> getGameDetails(@PathVariable Long gameId) {
        Game game = gameService.getGameDetails(gameId);
        return ResponseEntity.ok(game);
    }

    @GetMapping("/vendor/{vendorId}/active")
    public ResponseEntity<List<Game>> getActiveGames(@PathVariable Long vendorId) {
        return ResponseEntity.ok(gameService.getActiveGamesByVendorId(vendorId));
    }

    @GetMapping("/vendor/{vendorId}/scheduled")
    public ResponseEntity<List<Game>> getScheduledGames(@PathVariable Long vendorId) {
        return ResponseEntity.ok(gameService.getScheduledGamesByVendorId(vendorId));
    }

    @PutMapping("/update-statuses")
    public ResponseEntity<Void> updateStatuses() {
        gameService.updateGameStatuses();
        return ResponseEntity.ok().build();
    }
}
