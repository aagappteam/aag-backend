package aagapp_backend.controller.gamecomplete;

import aagapp_backend.dto.*;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.leaderboard.LeaderboardGame;
import aagapp_backend.services.pricedistribute.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


//    process game result after game completion
    @PostMapping("/processGameResult")
    public ResponseEntity<?> processGameResult(@RequestBody GameResult gameResult) {
        try {
            // Process game result (you may want to keep this as is, or include some additional logic here)
               matchService.processGameResult(gameResult);

            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("players", gameResult);
            response.put("message", "Game results processed successfully.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//    if vendor wants to see leaderboard data for a specific game
    @GetMapping("/game/{gameId}")
    public ResponseEntity<?> getLeaderboard(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());
            GameLeaderboardResponseDTO leaderboard = leaderboardService.getLeaderboard(gameId, pageable);
            return responseService.generateResponse(HttpStatus.OK, "Leaderboard fetched successfully", leaderboard);
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    after game completion fetch leaderboard data and return it to client
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam Long gameId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Boolean winner  // Optional param
    ) {
        try {
            List<LeaderboardDto> leaderboard = matchService.getLeaderboard(gameId, roomId, winner);
            return responseService.generateResponse(HttpStatus.OK, "Leaderboard fetched successfully", leaderboard);
        }catch (RuntimeException e){
            return responseService.generateErrorResponse(
                    "No game results found for this room and game ",
                    HttpStatus.BAD_REQUEST
            );
        }
        catch (Exception e) {
            return responseService.generateErrorResponse(
                    "Error fetching leaderboard: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

}
