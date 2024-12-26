package aagapp_backend.controller.game;
import aagapp_backend.dto.GameRequest;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.game.GameSession;
import aagapp_backend.entity.players.Player;
import aagapp_backend.services.GameService.GameService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PaymentFeatures;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import javax.naming.LimitExceededException;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/games")
public class GameController {

    private GameService gameService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;

    @Autowired
    public void setGameService(@Lazy GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setPaymentFeatures(@Lazy PaymentFeatures paymentFeatures) {
        this.paymentFeatures = paymentFeatures;
    }

    @GetMapping("/get-all-games")
    public ResponseEntity<?> getAllGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "vendorId", required = false) Long vendorId
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Game> gamesPage = gameService.getAllGames(status, vendorId, pageable);
            return responseService.generateSuccessResponse("Games fetched successfully", gamesPage, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching games", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-games-by-vendor/{vendorId}")
    public ResponseEntity<?> getGamesByVendorId(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status
    ) {
        try {

            if (page < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Size must be between 1 and 100");
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Game> gamesPage = gameService.findGamesByVendor(vendorId, status, pageable);
            return responseService.generateSuccessResponse("Games fetched successfully", gamesPage, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);

            return responseService.generateErrorResponse("Error fetching games by vendor : " + e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-active-games-by-vendor/{vendorId}")
    public ResponseEntity<?> getActiveGamesByVendorId(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {

            if (page < 0) {
                throw new IllegalArgumentException("Page number cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Size must be between 1 and 100");
            }


            Pageable pageable = PageRequest.of(page, size);
            Page<Game> gamesPage = gameService.findGamesScheduledForToday(vendorId, pageable);

            return responseService.generateSuccessResponse(
                    "Game fetched successfully", gamesPage, HttpStatus.CREATED
            );
        } catch (Exception e) {
            exceptionHandling.handleException(e);

            return responseService.generateErrorResponse("Error fetching games by vendor : " + e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/publishGame/{vendorId}")
    public ResponseEntity<?> publishGame(@PathVariable Long vendorId, @RequestBody GameRequest gameRequest) {
        try {


            ResponseEntity<?> paymentEntity = paymentFeatures.canPublishGame(vendorId);

            if (paymentEntity.getStatusCode() != HttpStatus.OK) {
                return paymentEntity;
            }


            Game publishedGame = gameService.publishGame(gameRequest, vendorId);

            if (gameRequest.getScheduledAt() != null) {
                return responseService.generateSuccessResponse(
                        "Game scheduled successfully", publishedGame, HttpStatus.CREATED
                );
            } else {
                return responseService.generateSuccessResponse(
                        "Game published successfully", publishedGame, HttpStatus.CREATED
                );
            }
        } catch (LimitExceededException e) {
            return responseService.generateErrorResponse("Exceeded maximum allowed games ", HttpStatus.TOO_MANY_REQUESTS);

        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse("Required entity not found " + e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);

            return responseService.generateErrorResponse("Invalid game data " + e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error publishing game" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{vendorId}/{gameId}")
    public ResponseEntity<?> updateStatuses(
            @PathVariable Long vendorId,
            @PathVariable Long gameId,
            @RequestBody GameRequest gameRequest
    ) {
        try {
            ResponseEntity<?> response = gameService.updateGame(vendorId, gameId, gameRequest);

            return ResponseService.generateSuccessResponse("Game updated successfully", gameRequest, HttpStatus.OK);

        } catch (IllegalStateException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating game details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    //create ludo room
//    @PostMapping("/create-ludo-room")
//    public ResponseEntity<?> createGameRoom(@Valid @RequestBody Player player, BindingResult result) {
//        // If validation fails, return a detailed error message
//        if (result.hasErrors()) {
//            StringBuilder errorMessages = new StringBuilder("Validation failed: ");
//            for (FieldError error : result.getFieldErrors()) {
//                errorMessages.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ");
//            }
//            return ResponseEntity.badRequest().body(errorMessages.toString());
//        }
//
//        try {
//            // Proceed to create the game room if validation is successful
//            GameRoom gameRoom = gameService.createGameRoom(player);
//            return new ResponseEntity<>(gameRoom, HttpStatus.CREATED);
//        } catch (Exception e) {
//            // Handle any other errors (e.g., player not found)
//            exceptionHandling.handleException(e);
//            return responseService.generateErrorResponse("Error creating game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Join an existing game room
//    @PostMapping("/join-ludo-room")
//    public ResponseEntity<?> joinGameRoom(@Valid @RequestBody Player player, BindingResult result) {
//        // If validation fails, return a detailed error message
//        if (result.hasErrors()) {
//            StringBuilder errorMessages = new StringBuilder("Validation failed: ");
//            for (FieldError error : result.getFieldErrors()) {
//                errorMessages.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ");
//            }
//            return ResponseEntity.badRequest().body(errorMessages.toString());
//        }
//
//        try {
//            GameRoom gameRoom = gameService.joinGameRoom(player);
//            return new ResponseEntity<>(gameRoom, HttpStatus.OK);
//        } catch (IllegalStateException e) {
//            // Handle case where no rooms are available
//            exceptionHandling.handleException(e);
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            // Handle invalid player details
//            exceptionHandling.handleException(e);
//            return responseService.generateErrorResponse("Something went wrong:  " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//
//    // Get game session state by room code
//    @GetMapping("/{roomCode}")
//    public ResponseEntity<?> getGameSession(@PathVariable String roomCode) {
//        try {
//            GameSession gameSession = gameService.getGameSession(roomCode);
//            return new ResponseEntity<>(gameSession, HttpStatus.OK);
//        } catch (EntityNotFoundException e) {
//            // Handle case where room code is not found
//            exceptionHandling.handleException(e);
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
//        }catch (Exception e){
//            exceptionHandling.handleException(e);
//            return responseService.generateErrorResponse("Something went wrong: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

//    // Update the game session (player's move)
//    @PutMapping("/session/{roomCode}")
//    public ResponseEntity<?> updateGameSession(
//            @PathVariable String roomCode,
//            @RequestBody @Valid GameStateDTO gameStateDTO) {
//        try {
//            GameSession gameSession = gameService.updateGameSession(roomCode, gameStateDTO.getGameState());
//            return new ResponseEntity<>(gameSession, HttpStatus.OK);
//        } catch (EntityNotFoundException e) {
//            // Handle case where room code or game session is not found
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
//        } catch (IllegalArgumentException e) {
//            // Handle invalid game state
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }




}
