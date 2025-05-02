package aagapp_backend.controller.league;

import aagapp_backend.dto.*;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.leaderboard.LeaderBoardLeague;
import aagapp_backend.services.leaderboard.LeaderboardGame;
import aagapp_backend.services.league.LeagueService;
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
@RequestMapping("/leagues/winning")
public class LeagueWinning {

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private LeaderBoardLeague leaderBoardLeague;


    @PostMapping("/processGameResult")
    public ResponseEntity<?> processGameResult(@RequestBody LeagueMatchProcess gameResult) {
        try {
            // Process game result (you may want to keep this as is, or include some additional logic here)
            List<PlayerDto> playersDetails = leagueService.processMatch(gameResult);


            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("players", playersDetails);
            response.put("message", "League results processed successfully.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/game/{leagueId}")
    public ResponseEntity<?> getLeaderboard(@PathVariable Long leagueId, @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        try {

            Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());
            GameLeaderboardResponseDTO leaderboard = leaderBoardLeague.getLeaderboard(leagueId, pageable);
            return responseService.generateResponse(HttpStatus.OK, "Leaderboard fetched successfully", leaderboard);
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam Long leagueId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Boolean winner  // Optional param
    ) {
        try {
            List<LeaderboardDto> leaderboard = leaderBoardLeague.getLeaderboard(leagueId, roomId, winner);
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
