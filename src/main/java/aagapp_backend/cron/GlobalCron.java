package aagapp_backend.cron;


import aagapp_backend.entity.tournament.TournamentRoom;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.gameservice.GameService;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlobalCron {

    @Autowired
    private GameService gameservice;

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private ResponseService responseService;

    @Scheduled(cron = "0 * * * * *")  // Every minute
    public ResponseEntity<?> checkAndActivateScheduledGames() {
        try{
            int page = 0;
            int pageSize = 100;
            List<Long> vendorIds;
            while (!(vendorIds = gameservice.getActiveVendorIdsInBatch(page, pageSize)).isEmpty()) {



                /*int numberOfRooms = (tournamentRequest.getParticipants() / 2);
                    for (int i = 0; i < numberOfRooms; i++) {
                        TournamentRoom room = new TournamentRoom();
                        room.setTournament(savedTournament);
                        room.setMaxParticipants(2);
                        room.setCurrentParticipants(0);
                        room.setStatus("OPEN");
                        room.setRound(1);
                        roomRepository.save(room);

                        Double total_prize = 3.2;
                        String gamePassword = this.createNewGame(baseUrl, tournament.getId(), room.getId(), room.getMaxParticipants(), tournament.getMove(), total_prize);

                        room.setGamepassword(gamePassword);
                    }*/





                page++;
            }
            return responseService.generateResponse(HttpStatus.OK,"All scheduled games activated", null);
        }catch (ConstraintViolationException e){
            exceptionHandlingService.handleException(e);
          return   responseService.generateErrorResponse("ConstraintViolationException occured: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
        catch (RuntimeException e){
            exceptionHandlingService.handleException( e);
           return responseService.generateErrorResponse("RuntimeException issue occured: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);


        }catch (Exception e){
            exceptionHandlingService.handleException( e);
          return   responseService.generateErrorResponse("Error fetching games by vendor: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

}
