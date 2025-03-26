package aagapp_backend.services.league;

import aagapp_backend.dto.LeagueRequest;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private EntityManager em;

    @Autowired
    private VendorRepository vendorRepository;

    // Publish a new league
    public League publishLeague(LeagueRequest leagueRequest, Long vendorId) {
        try {
            League league = new League();
            league.setName(leagueRequest.getName());
            league.setDescription(leagueRequest.getDescription());
            league.setFee(leagueRequest.getFee());
            league.setLeagueType(leagueRequest.getLeagueType());
            league.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            league.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());
            league.setChallengingVendorTeamName(leagueRequest.getChallengingVendorTeamName());
            league.setStatus(LeagueStatus.PENDING);

            Long vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found")).getService_provider_id();
            league.setChallengingVendorId(vendor);

            // Ensure a vendor cannot challenge themselves
            if (leagueRequest.getOpponentVendorId() != null && leagueRequest.getOpponentVendorId().equals(vendorId)) {
                throw new IllegalArgumentException("A vendor cannot challenge themselves");
            }

            ThemeEntity theme = em.find(ThemeEntity.class, leagueRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }
            league.setTheme(theme);

            // Handle challenge scenario
            if (leagueRequest.getIsChallenge()) {
                Long challengedVendorId = null;

                if (leagueRequest.getOpponentVendorId() == null) {
                    VendorEntity randomVendor = getRandomAvailableVendor(vendorId);
                    challengedVendorId = randomVendor.getService_provider_id(); // Store only the vendor ID
                } else {
                    VendorEntity opponentVendor = vendorRepository.findById(leagueRequest.getOpponentVendorId())
                            .orElseThrow(() -> new RuntimeException("Opponent vendor not found"));
                    challengedVendorId = opponentVendor.getService_provider_id(); // Store only the vendor ID
                }

                league.setOpponentVendorId(challengedVendorId); // Set only the vendor ID
            }



            league.setStatus(LeagueStatus.SCHEDULED);
            league.setScheduledAt(ZonedDateTime.now().plusMinutes(20));

            // Scheduled date validation
            if (leagueRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledAtInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledAtInKolkata.isBefore(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusHours(4))) {
                    throw new IllegalArgumentException("The league must be scheduled at least 4 hours in advance.");
                }
                league.setScheduledAt(scheduledAtInKolkata);
                league.setEndDate(scheduledAtInKolkata.plusHours(4));
            } else {
                ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                league.setScheduledAt(nowInKolkata.plusMinutes(15));
                league.setEndDate(nowInKolkata.plusHours(4));
                league.setStatus(LeagueStatus.ACTIVE);
            }

            // Set registration deadline if provided
            if (leagueRequest.getRegistrationDeadline() != null) {
                league.setEndDate(leagueRequest.getRegistrationDeadline().withZoneSameInstant(ZoneId.of("Asia/Kolkata")));
            }

            league.setCreatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
            league.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

            League savedLeague = leagueRepository.save(league);

            // Generate shareable link if necessary
            String shareableLink = generateShareableLink(savedLeague.getId());
            savedLeague.setShareableLink(shareableLink);

            return savedLeague;
        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new IllegalArgumentException("Invalid league schedule: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the league: " + e.getMessage(), e);
        }
    }

    private VendorEntity getRandomAvailableVendor(Long excludeVendorId) {
        // Fetch available vendors excluding the vendor calling the method
        List<VendorEntity> availableVendors = vendorRepository.findByLeagueStatus(LeagueStatus.AVAILABLE);
        availableVendors.removeIf(vendor -> vendor.getService_provider_id().equals(excludeVendorId));

        if (availableVendors.isEmpty()) {
            throw new RuntimeException("No available vendors found.");
        }

        // Return a random vendor from the remaining list
        return availableVendors.get(new Random().nextInt(availableVendors.size()));
    }



    private String generateShareableLink(Long gameId) {
        return "https://example.com/leagues/" + gameId;
    }


    /*public League updateLeague(Long vendorId, Long leagueId, LeagueRequest leagueRequest) {

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
                if (leagueRequest.getMove() != null) {
                    league.setMove(leagueRequest.getMove());
                }


                ZonedDateTime scheduledInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

                league.setScheduledAt(scheduledInKolkata);
                league.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
                league.setEndDate(scheduledInKolkata.plusHours(4));

                return em.merge(league);
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
    }*/

//    public Page<League> getLeagueByVendorIdAndLeagueId(Long vendorId, Long leagueId, Pageable pageable) {
//        try {
//            Page<League> leagues = leagueRepository.findByVendorIdAndId(vendorId, leagueId, pageable);
//
//            return leagues.isEmpty() ? Page.empty() : leagues;
//        } catch (Exception e) {
//            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
//            throw new RuntimeException("Error occurred while retrieving the league: " + e.getMessage(), e);
//        }
//    }

    public Page<League> getAllLeagues(Pageable pageable, String status, Long vendorId) {
        try {
            // Check if 'status' and 'vendorId' are provided and build a query accordingly
           /*if (status != null && vendorId != null) {
                return leagueRepository.findLeaguesByStatusAndVendorId(status, vendorId, pageable);
            } else*/ if (status != null) {
                return leagueRepository.findByStatus(status, pageable);
            } /*else if (vendorId != null) {
                return leagueRepository.findByVendorServiceProviderId(vendorId, pageable);
            }*/ else {
                return leagueRepository.findAll(pageable); // Return all leagues with pagination if no filters
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }



    public List<League> getAllLiveLeagues() {
        try {
            // Fetch all leagues with status LIVE
            List<League> liveLeagues = leagueRepository.findByStatus(LeagueStatus.LIVE);

            if (liveLeagues.isEmpty()) {
                throw new RuntimeException("No live leagues found.");
            }

            return liveLeagues;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching live leagues: " + e.getMessage(), e);
        }
    }


    /*public void deleteLeague(Long vendorId, Long leagueId) {
        try {
            Optional<League> leagueOptional = leagueRepository.findById(leagueId);
            if (leagueOptional.isPresent()) {
                League league = leagueOptional.get();

                if (league.getVendorEntity().getService_provider_id().equals(vendorId)) {
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
    }*/


    // Scheduled task to auto reject expired challenges after 10 minutes
    @Scheduled(cron = "0 * * * * *")  // Runs every minute
    public void autoRejectExpiredChallenges() {
        try {
            // Find all leagues in the "SCHEDULED" state
            List<League> scheduledLeagues = leagueRepository.findByStatus(LeagueStatus.SCHEDULED);

            for (League league : scheduledLeagues) {
                // If the 10-minute window has passed since the challenge was scheduled
                if (ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(league.getCreatedDate().plusMinutes(1))) {
                    // Reject the challenge if it's expired
                    league.setStatus(LeagueStatus.REJECTED);
                    league.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
                    leagueRepository.save(league);
                    leagueRepository.delete(league);
                }
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while auto-rejecting expired challenges "+ e.getMessage());
        }
    }


}