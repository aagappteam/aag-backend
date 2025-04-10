package aagapp_backend.controller.game;

import aagapp_backend.dto.GameResult;
import aagapp_backend.dto.PlayerDto;
import aagapp_backend.dto.VendorGameResponse;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
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

    // Process match endpoint
    /*@PostMapping("/processGameResult")
    public ResponseEntity<?> processGameResult(@RequestBody GameResult gameResult) {
        try {
            // Call the service method to process the match and get the response
            String result = matchService.processMatch(gameResult);

            // Assuming winner information is part of the response
            PlayerDto winner = matchService.getWinnerDetails(gameResult);

            // Construct the response with user data
            Map<String, Object> response = new HashMap<>();
            if (winner != null) {
                response.put("name", winner.getName());
                response.put("score", winner.getScore());
                response.put("amount", winner.getAmount());
                response.put("picture", winner.getPictureUrl());  // Assuming picture URL is stored in PlayerDto
            }

            response.put("message", "Game results processed successfully.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            exceptionHandlingImplement.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error fetching games by vendor: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @PostMapping("/processGameResult")
    public ResponseEntity<?> processGameResult(@RequestBody GameResult gameResult) {
        try {
            // Process game result (you may want to keep this as is, or include some additional logic here)
            List<PlayerDto> playersDetails =   matchService.processMatch(gameResult);

            // Get details of all players after the game
/*
            List<PlayerDto> playersDetails = matchService.getAllPlayersDetails(gameResult);
*/

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



/*    @PostMapping("/process")
    public String processGameResult(@RequestBody GameResult gameResult) {
        try {
            // Retrieve vendorId based on gameId and roomId
//            Long vendorId = getVendorIdFromGameAndRoom(gameResult.getGameId(), gameResult.getRoomId());
            Optional<GameRoom>  gameRoom =     gameRoomRepository.findByGameId(gameResult.getGameId());
            if(gameRoom.isPresent()){
                Long vendorId = gameRoom.get().getVendorId();
            }


            // Add vendorId to the game result and process match
            gameResult.setVendorId(vendorId);  // Set vendorId in the gameResult

            // Process the match
            matchService.processMatch(gameResult);

            return "Game results processed successfully.";
        } catch (Exception e) {
            return "Error processing game results: " + e.getMessage();
        }
    }*/

}
