package aagapp_backend.controller.customer.games;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.dto.LeagueResultRecordDTO;
import aagapp_backend.dto.TournamentResultRecordDTO;
import aagapp_backend.dto.game.GameResultRecordDTO;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.tournamnetservice.TournamentService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("customer/games")
public class CustomerGame {

    @Autowired
    private GameService gameService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private TournamentService tournamentService;


    @GetMapping("/get-games-by-user/{userId}")
    public ResponseEntity<?> getGamesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String gamename,
            @RequestParam(required = false) Boolean winner
    ) {
        try {
            if (page < 0) throw new IllegalArgumentException("Page number cannot be negative");
            if (size <= 0 || size > 100) throw new IllegalArgumentException("Size must be between 1 and 100");

            // Convert empty gamename to null
            if (gamename != null && gamename.trim().isEmpty()) {
                gamename = null;
            }

            Pageable pageable = PageRequest.of(page, size);

            Page<GameResultRecordDTO> gamesPage = gameService.findGamesByUserId(userId, pageable, gamename, winner);

            return responseService.generateSuccessResponseWithCount(
                    "Games fetched successfully",
                    gamesPage.getContent(),
                    gamesPage.getTotalElements(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    "Error fetching games by user : " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @GetMapping("/get-league-results-by-user/{userId}")
    public ResponseEntity<?> getLeagueResultsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String leaguename,
            @RequestParam(required = false) Boolean winner
    ) {
        try {
            if (page < 0) throw new IllegalArgumentException("Page number cannot be negative");
            if (size <= 0 || size > 100) throw new IllegalArgumentException("Size must be between 1 and 100");

            if (leaguename != null && leaguename.trim().isEmpty()) {
                leaguename = null;
            }

            Pageable pageable = PageRequest.of(page, size);

            Page<LeagueResultRecordDTO> leaguePage = leagueService.findLeagueResultsByUserId(userId, pageable, leaguename, winner);

            return responseService.generateSuccessResponseWithCount(
                    "League results fetched successfully",
                    leaguePage.getContent(),
                    leaguePage.getTotalElements(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    "Error fetching league results by user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }




    @GetMapping("/get-tournament-results-by-user/{userId}")
    public ResponseEntity<?> getTournamentResultsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tournamentname,
            @RequestParam(required = false) Boolean winner
    ) {
        try {
            if (page < 0) throw new IllegalArgumentException("Page number cannot be negative");
            if (size <= 0 || size > 100) throw new IllegalArgumentException("Size must be between 1 and 100");

            if (tournamentname != null && tournamentname.trim().isEmpty()) {
                tournamentname = null;
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<TournamentResultRecordDTO> resultPage = tournamentService.findTournamentResultsByUserId(userId, pageable, tournamentname, winner);

            return responseService.generateSuccessResponseWithCount(
                    "Tournament results fetched successfully",
                    resultPage.getContent(),
                    resultPage.getTotalElements(),
                    HttpStatus.OK
            );

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    "Error fetching tournament results by user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }



}
