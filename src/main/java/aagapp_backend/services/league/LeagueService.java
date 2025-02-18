package aagapp_backend.services.league;
import aagapp_backend.dto.LeagueRequest;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private EntityManager em;

    // Publish a new league
    public League publishLeague(LeagueRequest leagueRequest, Long vendorId) {
        try {
            League league = new League();
            league.setName(leagueRequest.getName());
            league.setDescription(leagueRequest.getDescription());

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (vendorEntity == null) {
                throw new RuntimeException("No records found for vendor with ID: " + vendorId);
            }

            ThemeEntity theme = em.find(ThemeEntity.class, leagueRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }

            league.setVendorId(vendorId);
            league.setTheme(theme);

            // Calculate moves based on the selected fee

            league.setFee(leagueRequest.getFee());
            if(leagueRequest.getMove()!= null){
                league.setMove(leagueRequest.getMove());
            }


            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            // Set scheduledAt: validate if it is at least 4 hours in advance
            if (leagueRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledAtInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledAtInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The league must be scheduled at least 4 hours in advance.");
                }
                league.setScheduledAt(scheduledAtInKolkata);
                league.setEndDate(scheduledAtInKolkata.plusHours(4));

                league.setStatus(LeagueStatus.SCHEDULED);
            } else {
                league.setScheduledAt(nowInKolkata.plusMinutes(15));
                league.setStatus(LeagueStatus.ACTIVE);
                league.setEndDate(nowInKolkata.plusHours(4));

            }

            if (leagueRequest.getRegistrationDeadline() != null) {
                ZonedDateTime registrationDeadlineInKolkata = leagueRequest.getRegistrationDeadline().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                league.setEndDate(registrationDeadlineInKolkata);
            }

            league.setCreatedDate(nowInKolkata);
            league.setUpdatedDate(nowInKolkata);

            if(leagueRequest.getMinPlayersPerTeam()!=null){
                league.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            }

            if(leagueRequest.getMaxPlayersPerTeam()!=null){
                league.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());
            }


            league.setLeagueType(leagueRequest.getLeagueType());

            // Optionally, handle teamIds if they are provided (use as per your business logic)
//            league.setTeamIds(leagueRequest.getTeamIds());

            League savedLeague = leagueRepository.save(league);

            // Generate and set the shareable link (if applicable)
            String shareableLink = generateShareableLink(savedLeague.getId());
            league.setShareableLink(shareableLink);

            // Return the saved league entity
            return leagueRepository.save(savedLeague);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new IllegalArgumentException("Invalid league schedule: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the league: " + e.getMessage(), e);
        }
    }

    private String generateShareableLink(Long gameId) {
        return "https://example.com/leagues/" + gameId;
    }



    public League updateLeague(Long vendorId, Long leagueId, LeagueRequest leagueRequest) {

        String jpql = "SELECT l FROM League l WHERE l.id = :leagueId AND l.vendorEntity.id = :vendorId";
        TypedQuery<League> query = em.createQuery(jpql, League.class);
        query.setParameter("leagueId", leagueId);
        query.setParameter("vendorId", vendorId);

        League league = query.getResultList().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("League ID: " + leagueId + " does not belong to Vendor ID: " + vendorId));

        if (league.getStatus() == LeagueStatus.EXPIRED) {
            throw new IllegalStateException("League ID: " + league.getId() + " has been expired and cannot be updated.");
        } else if (league.getStatus() == LeagueStatus.ACTIVE) {
            throw new IllegalStateException("League ID: " + league.getId() + " is already active and cannot be updated.");
        }

        if (leagueRequest.getName() != null && !leagueRequest.getName().isEmpty()) {
            league.setName(leagueRequest.getName());
        }
        ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime scheduledAtInKolkata = league.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

        if (scheduledAtInKolkata != null) {
            ZonedDateTime oneDayBeforeScheduled = scheduledAtInKolkata.minusDays(1);

            if (nowInKolkata.isBefore(oneDayBeforeScheduled)) {

                // Calculate moves based on the selected fee

                league.setFee(leagueRequest.getFee());
                if(leagueRequest.getMove()!= null){
                    league.setMove(leagueRequest.getMove());
                }


                ZonedDateTime scheduledInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

                league.setScheduledAt(scheduledInKolkata);
                league.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
                league.setEndDate(scheduledInKolkata.plusHours(4));

              return   em.merge(league);
            } else {
                throw new IllegalStateException("Game ID: " + league.getId() + " cannot be updated on the scheduled date or after.");

            }
        } else {
            throw new IllegalStateException("Game ID: " + league.getId() + " does not have a scheduled time.");
        }



    }

    public Page<League> findLeaguesByVendorAndStatus(Long vendorId, String status, Pageable pageable) {
        try {
            LeagueStatus leagueStatus = LeagueStatus.valueOf(status.toUpperCase());

            Page<League> leagues = leagueRepository.findLeaguesByVendorIdAndStatus(vendorId, leagueStatus, pageable);

            // Return an empty page if no leagues are found
            return leagues.isEmpty() ? Page.empty() : leagues;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error retrieving leagues by vendor ID: " + vendorId, e);
        }
    }

    public Page<League> getLeagueByVendorIdAndLeagueId(Long vendorId, Long leagueId, Pageable pageable) {
        try {
            Page<League> leagues = leagueRepository.findByVendorIdAndId(vendorId, leagueId, pageable);

            return leagues.isEmpty() ? Page.empty() : leagues;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while retrieving the league: " + e.getMessage(), e);
        }
    }

    public Page<League> getAllLeagues(Pageable pageable, String status, Long vendorId) {
        try {
            if (status != null && vendorId != null) {
                return leagueRepository.findByStatusAndVendorId(status, vendorId, pageable);
            } else if (status != null) {
                return leagueRepository.findByStatus(status, pageable);
            } else if (vendorId != null) {
                return leagueRepository.findByVendorId(vendorId, pageable);
            } else {
                return leagueRepository.findAll(pageable);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while retrieving all leagues", e);
        }
    }

    public void deleteLeague(Long vendorId, Long leagueId) {
        try {
            Optional<League> leagueOptional = leagueRepository.findById(leagueId);
            if (leagueOptional.isPresent()) {
                League league = leagueOptional.get();

                if (league.getVendorId().equals(vendorId)) {
                    leagueRepository.deleteById(leagueId);
                } else {
                    throw new IllegalArgumentException("League with ID " + leagueId + " does not belong to the vendor with ID " + vendorId);
                }
            } else {
                throw new NoSuchElementException("League with ID " + leagueId + " not found");
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while deleting the league: " + e.getMessage(), e);
        }
    }
}
