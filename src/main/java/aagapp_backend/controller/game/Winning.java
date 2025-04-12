package aagapp_backend.controller.game;

import aagapp_backend.dto.*;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.leaderboard.LeaderboardGame;
import aagapp_backend.services.pricedistribute.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/winning")

public class Winning {


@Autowired
private GameRoomRepository gameRoomRepository;
    @Autowired
    private MatchService matchService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    private ResponseService responseService;

@Autowired
    private  LeaderboardGame leaderboardService;


    @PostMapping("/processGameResult")
    public ResponseEntity<?> processGameResult(@RequestBody GameResult gameResult) {
        try {
            // Process game result (you may want to keep this as is, or include some additional logic here)
            List<PlayerDto> playersDetails =   matchService.processMatch(gameResult);


            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("players", playersDetails);
            response.put("message", "Game results processed successfully.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/game/{gameId}")
    public ResponseEntity<?> getLeaderboard(@PathVariable Long gameId) {
      try{
          GameLeaderboardResponseDTO leaderboard = leaderboardService.getLeaderboard(gameId);
          return responseService.generateResponse(HttpStatus.OK, "Leaderboard fetched successfully", leaderboard);
      }catch (Exception e) {
          exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
          return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }


}
