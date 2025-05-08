package aagapp_backend.controller.tournament;

import aagapp_backend.dto.GameLeaderboardResponseDTO;
import aagapp_backend.dto.GameResult;
import aagapp_backend.dto.LeaderboardDto;
import aagapp_backend.dto.PlayerDto;
import aagapp_backend.dto.leaderboard.TournamentLeaderboardDto;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.leaderboard.LeaderBoardTournament;
import aagapp_backend.services.tournamnetservice.TournamentService;
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
@RequestMapping("/tournamentwinning")
public class TournamentWinning {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private LeaderBoardTournament leaderBoardTournament;

    @PostMapping("/processGameResult")
    public ResponseEntity<?> processGameResult(@RequestBody GameResult gameResult) {
        try {
            // Process game result (you may want to keep this as is, or include some additional logic here)
           /* List<PlayerDto> playersDetails = tournamentService.processMatch(gameResult);


            // Prepare the response
            Map<String, Object> response = new HashMap<>();
            response.put("players", playersDetails);
            response.put("message", "Tournament results processed successfully.");

            return ResponseEntity.ok(response);*/
            tournamentService.processMatchResults(gameResult);
            return ResponseEntity.ok("Match result updated");
        } catch (RuntimeException e) {
            exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    for vendor this is api
    @GetMapping("/game/{tournamentId}")
    public ResponseEntity<?> getLeaderboard(@PathVariable Long tournamentId, @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        try {

            Pageable pageable = PageRequest.of(page, size, Sort.by("score").descending());
            GameLeaderboardResponseDTO leaderboard = leaderBoardTournament.getLeaderboardforvendor(tournamentId, pageable);
            return responseService.generateResponse(HttpStatus.OK, "Leaderboard fetched successfully", leaderboard);
        } catch (Exception e) {
            exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error processing game: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam Long tournamentId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Boolean winner  // Optional param
    ) {
        try {
            List<TournamentLeaderboardDto> leaderboard = leaderBoardTournament.getLeaderboard(tournamentId, roomId, winner);
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
