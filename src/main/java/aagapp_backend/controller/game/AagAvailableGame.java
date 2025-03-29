package aagapp_backend.controller.game;
import aagapp_backend.services.AagGameService;
import aagapp_backend.dto.GameRequestDTO;
import aagapp_backend.dto.GameResponseDTO;
import aagapp_backend.exception.GameNotFoundException;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import java.util.List;

@RestController
@RequestMapping("/aaggame")
public class AagAvailableGame {

    @Autowired
    private AagGameService gameService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private ResponseService responseService;

    // Create a new game
    @PostMapping("/create")
    @CacheEvict(value = "gamesCache", allEntries = true) // Clears the cache when a new game is created

    public ResponseEntity<?> createGame(@RequestBody GameRequestDTO gameRequestDTO) {
        try {
            GameResponseDTO gameResponseDTO = gameService.createGame(gameRequestDTO);
            return new ResponseEntity<>(gameResponseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    // Get all games with pagination (optional)
    @GetMapping("/get-all-games")
    @Cacheable(value = "gamesCache", key = "#page + '-' + #size") // Caches the response based on page and size

    public ResponseEntity<?> getAllGames(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
       try{
           List<GameResponseDTO> games = gameService.getAllGames(page, size);
           return responseService.generateSuccessResponse("All Games fetched successfully", games, HttpStatus.OK);

       } catch (Exception e) {
           exceptionHandling.handleException(e);
           return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

       }
    }

    // Get a game by ID
    @GetMapping("get-one-game/{gameId}")
    @Cacheable(value = "gameCache", key = "#gameId") // Caches the response based on the game ID

    public ResponseEntity<?> getGameById(@PathVariable Long gameId) {
        try {
            GameResponseDTO gameResponseDTO = gameService.getGameById(gameId);
            return responseService.generateSuccessResponse("Games fetched successfully", gameResponseDTO, HttpStatus.OK);

        } catch (GameNotFoundException e) {
            return responseService.generateErrorResponse("Not Found",HttpStatus.NOT_FOUND);

        }catch (Exception e){
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    // Update a game
    @PutMapping("/update-game/{gameId}")
    @CacheEvict(value = {"gamesCache", "gameCache"}, key = "#gameId") // Evict both caches for the updated game
    public ResponseEntity<?> updateGame(@PathVariable Long gameId, @RequestBody GameRequestDTO gameRequestDTO) {
        try {
            GameResponseDTO updatedGame = gameService.updateGame(gameId, gameRequestDTO);
            return responseService.generateSuccessResponse("Game Updated successfully",updatedGame,HttpStatus.OK);

        } catch (GameNotFoundException e) {
            return responseService.generateErrorResponse("Not Found",HttpStatus.NOT_FOUND);
        }
        catch (Exception e){
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a game
    @DeleteMapping("/delete-aag-game/{gameId}")
    @CacheEvict(value = {"gamesCache", "gameCache"}, key = "#gameId") // Evict both caches for the updated game
    public ResponseEntity<?> deleteGame(@PathVariable Long gameId) {
        try {
            gameService.deleteGame(gameId);
            return responseService.generateSuccessResponse("Game Deleted successfully",null,HttpStatus.OK);
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (GameNotFoundException e) {
            return responseService.generateErrorResponse("Not Found",HttpStatus.NOT_FOUND);
        }catch (Exception e){
        exceptionHandling.handleException(e);
        return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    }
}
