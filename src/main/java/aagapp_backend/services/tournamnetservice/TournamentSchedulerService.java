package aagapp_backend.services.tournamnetservice;

import aagapp_backend.entity.tournament.Tournament;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
public class TournamentSchedulerService {

/*    private final TaskScheduler taskScheduler;
    private final TournamentService tournamentService;

    @Autowired
    public TournamentSchedulerService(TaskScheduler taskScheduler, TournamentService tournamentService) {
        this.taskScheduler = taskScheduler;
        this.tournamentService = tournamentService;
    }

    public void scheduleTournamentStart(Tournament tournament) {
        ZonedDateTime scheduledStart = tournament.getScheduledAt().minusSeconds(5); // Run 5 sec early
        Instant executionTime = scheduledStart.toInstant();

        taskScheduler.schedule(() -> {
            try {
                tournamentService.startTournament(tournament.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Date.from(executionTime));
    }*/
}
