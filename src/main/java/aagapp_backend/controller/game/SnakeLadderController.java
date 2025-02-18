package aagapp_backend.controller.game;
import aagapp_backend.dto.JoinRoomRequest;
import aagapp_backend.dto.LeaveRoomRequest;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.gameservice.SnakeLadderGameService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.payment.PaymentFeatures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/snake-ladder")
public class SnakeLadderController {


    private GameService gameService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;
    private PlayerRepository playerRepository;
    private GameRoomRepository gameRoomRepository;

    @Autowired
    public void setGameService(@Lazy GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    public void setPlayerRepository(@Lazy PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Autowired
    public void setGameRoomRepository(@Lazy GameRoomRepository gameRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
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


/*    @PostMapping("/join-room")
    public ResponseEntity<?> joinGameRoom(@RequestBody JoinRoomRequest joinRoomRequest) {
        try {
            return gameService.joinRoom(joinRoomRequest.getPlayerId(), joinRoomRequest.getGameId());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in joining game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @PostMapping("/leave-room")
    public ResponseEntity<?> leaveGameRoom(@RequestBody LeaveRoomRequest leaveRoomRequest) {
        try {
            return gameService.leaveRoom(leaveRoomRequest.getPlayerId(), leaveRoomRequest.getGameId());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in leaving game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
