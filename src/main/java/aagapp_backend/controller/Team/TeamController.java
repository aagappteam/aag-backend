package aagapp_backend.controller.Team;
import aagapp_backend.entity.Team;
import aagapp_backend.repository.team.TeamRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.TeamService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

   /* @GetMapping("/getAllTeams")
    public ResponseEntity<?> getAllTeams() {
        try {
            List<Team> teams = teamRepository.findAll();
            return responseService.generateSuccessResponse("Teams found", teams, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching teams", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @GetMapping("/getTeamById")
    public ResponseEntity<?> getTeamById(@RequestParam Long id) {
        try {
            Team team = teamRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Team not found with ID: " + id));
            return responseService.generateSuccessResponse("Team found", team, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error fetching team", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/createTeam")
    public ResponseEntity<?> createTeam(@Validated @RequestBody Team team) {
        try {
            Team createdTeam = teamRepository.save(team);
            return responseService.generateSuccessResponse("Team created successfully", createdTeam, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error creating team", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/updateTeam")
    public ResponseEntity<?> updateTeam(@RequestParam Long id, @Valid @RequestBody Team teamDetails) {
        try {
            Team existingTeam = teamRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Team not found with ID: " + id));

            Field[] fields = Team.class.getDeclaredFields();

            // Iterate over each field and update if the corresponding value is not null in teamDetails
            for (Field field : fields) {
                field.setAccessible(true);  // Allow access to private fields

                // Get the value of the field in teamDetails
                Object newValue = field.get(teamDetails);

                if (newValue != null) {
                    // If the field value is not null, update the existing team
                    field.set(existingTeam, newValue);
                }
            }

            existingTeam.setUpdatedDate(ZonedDateTime.now());

            Team updatedTeam = teamRepository.save(existingTeam);

            return responseService.generateSuccessResponse("Team updated successfully", updatedTeam, HttpStatus.OK);

        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating team", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/deleteTeam")
    public ResponseEntity<?> deleteTeam(@RequestParam Long id) {
        try {
            if (!teamRepository.existsById(id)) {
                return responseService.generateErrorResponse("Team not found with ID: " + id, HttpStatus.NOT_FOUND);
            }

            teamRepository.deleteById(id);
            return responseService.generateSuccessResponse("Team has been deleted", null, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error deleting team", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
