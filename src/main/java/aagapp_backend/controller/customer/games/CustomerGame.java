package aagapp_backend.controller.customer.games;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.dto.LeagueResultRecordDTO;
import aagapp_backend.dto.TournamentResultRecordDTO;
import aagapp_backend.dto.game.GameResultRecordDTO;
import aagapp_backend.dto.game.PlayerSummaryCompactDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.GameResultRecordRepository;
import aagapp_backend.repository.league.LeagueResultRecordRepository;
import aagapp_backend.repository.tournament.TournamentResultRecordRepository;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

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

    @Autowired
    private  GameResultRecordRepository gameRepo;
    @Autowired
    private  LeagueResultRecordRepository leagueRepo;
    @Autowired
    private TournamentResultRecordRepository tournamentRepo;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

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



    @GetMapping("/summery/{playerId}")
    public ResponseEntity<?> getPlayerSummaryCompact(@PathVariable Long playerId) {
        try {
            Long totalGames = gameRepo.countByPlayer_PlayerId(playerId);
            Long totalLeagues = leagueRepo.countByPlayer_PlayerId(playerId);
            Long totalTournaments = tournamentRepo.countByPlayer_PlayerId(playerId);

            Long totalMatchesPlayed = totalGames + totalLeagues + totalTournaments;

            BigDecimal gameWinning = gameRepo.getTotalWinningAmountByPlayer(playerId);
            BigDecimal tournamentWinning = tournamentRepo.getTotalWinningAmountByPlayer(playerId);
            BigDecimal leagueWinning = leagueService.getTotalWinningsOfPlayer(playerId);
            BigDecimal totalWinningAmount = gameWinning.add(tournamentWinning).add(leagueWinning);

            PlayerSummaryCompactDTO response = new PlayerSummaryCompactDTO(
                    totalMatchesPlayed,
                    totalWinningAmount
            );
//
            return responseService.generateSuccessResponse(
                    "Player summary fetched successfully",
                    response,
                    HttpStatus.OK
            );
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(
                    "Error fetching player summary: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @PostMapping("/weekly-booster/activate/{userId}")
    public ResponseEntity<?> activateWeeklyBooster(@PathVariable Long userId) {
        CustomCustomer user = customCustomerRepository.findById(userId).orElse(null);
        if (user == null) {
            return responseService.generateErrorResponse(
                    "User not found",
                    HttpStatus.NOT_FOUND
            );
        }

        if (user.isWeeklyBoosterActive()) {
            return responseService.generateErrorResponse(
                    "Weekly booster is already active",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (user.getWeeklyBoostersLeft() <= 0) {
            return responseService.generateSuccessResponse("You have used all weekly booster.", user.getWeeklyBoostersLeft(), HttpStatus.OK);
        }

        user.setIsWeeklyBoosterActive(true);
        user.setBoosterActivatedAt(new Date());
        user.setWeeklyBoostersLeft(user.getWeeklyBoostersLeft() - 1);
        customCustomerRepository.save(user);

        return responseService.generateSuccessResponse(
                "Weekly booster activated successfully",user.getWeeklyBoostersLeft(),
                HttpStatus.OK
        );
    }



}
