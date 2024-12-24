package aagapp_backend.services.league;

import aagapp_backend.dto.LeagueRequest;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.FeeToMove;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private EntityManager em;

    // Publish a new league
   /* public League publishLeague(LeagueRequest leagueRequest, Long vendorId) {
        try {
            Optional<VendorEntity> vendorEntityOptional = leagueRepository.findVendorById(vendorId);
            if (!vendorEntityOptional.isPresent()) {
                throw new NoSuchElementException("No records found for vendor with ID: " + vendorId);
            }

            ThemeEntity theme = em.find(ThemeEntity.class, leagueRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }

            League league = new League();
            league.setName(leagueRequest.getName());
            league.setDescription(leagueRequest.getDescription());
            league.setVendorId(vendorId);

            league.setFeeToMoves(leagueRequest.getFeeToMoves());


            List<FeeToMove> feeToMoves = leagueRequest.getFeeToMoves().stream().map(feeRequest -> {
                FeeToMove feeToMove = new FeeToMove();
                feeToMove.setRupees(feeRequest.getAmount());
                feeToMove.setMoves(feeRequest.getMoves());
                return feeToMove;
            }).collect(Collectors.toList());

            league.setFeeToMoves(feeToMoves);

            // 6. Set the league scheduled time
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            if (leagueRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledAtInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledAtInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The league must be scheduled at least 4 hours in advance.");
                }
                league.setScheduledAt(scheduledAtInKolkata);
            } else {
                league.setScheduledAt(nowInKolkata.plusMinutes(15));
            }

            // 7. Set the end date for the league
            league.setEndDate(league.getScheduledAt().plusDays(7));

            // 8. Associate the theme with the league
            league.setTheme(theme);

            // 9. Set the min and max players per team (default values if not provided)
            league.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam() != null ? leagueRequest.getMinPlayersPerTeam() : 1);
            league.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam() != null ? leagueRequest.getMaxPlayersPerTeam() : 2);

            // 10. Set the league status to "SCHEDULED"
            league.setStatus(LeagueStatus.SCHEDULED);

            // 11. Save the league to the repository
            return leagueRepository.save(league);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new IllegalArgumentException("Invalid league schedule: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the league: " + e.getMessage(), e);
        }
    }*/
    public League publishLeague(LeagueRequest leagueRequest, Long vendorId) {
        try {
            // Create a new League entity
            League league = new League();
            league.setName(leagueRequest.getName());
            league.setDescription(leagueRequest.getDescription());
            league.setFeeToMoves(leagueRequest.getFeeToMoves());

            // Validate and set the vendor entity
            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (vendorEntity == null) {
                throw new RuntimeException("No records found for vendor with ID: " + vendorId);
            }

            // Validate and set the theme
            ThemeEntity theme = em.find(ThemeEntity.class, leagueRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }

            league.setVendorId(vendorId);
            league.setTheme(theme);

            // Calculate the moves based on the selected fee
            if (leagueRequest.getFeeToMoves() != null && !leagueRequest.getFeeToMoves().isEmpty()) {
                Double selectedFee = leagueRequest.getFeeToMoves().get(0).getRupees();
                league.calculateMoves(selectedFee);  // Calculate moves based on the fee
            }

            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            // Set scheduledAt: validate if it is at least 4 hours in advance
            if (leagueRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledAtInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledAtInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The league must be scheduled at least 4 hours in advance.");
                }
                league.setScheduledAt(scheduledAtInKolkata);
                league.setStatus(LeagueStatus.SCHEDULED);  // Set status to scheduled
            } else {
                league.setScheduledAt(nowInKolkata.plusMinutes(15)); // Default to 15 minutes from now
                league.setStatus(LeagueStatus.SCHEDULED);
            }

            if (leagueRequest.getRegistrationDeadline() != null) {
                ZonedDateTime registrationDeadlineInKolkata = leagueRequest.getRegistrationDeadline().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                league.setEndDate(registrationDeadlineInKolkata);
            }

            league.setCreatedDate(nowInKolkata);
            league.setUpdatedDate(nowInKolkata);
            league.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            league.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());

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
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("League not found"));

        try {
            for (Field field : LeagueRequest.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(leagueRequest);

                if (value != null) {
                    Field leagueField = League.class.getDeclaredField(field.getName());
                    leagueField.setAccessible(true);
                    leagueField.set(league, value);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error updating league", e);
        }

        return leagueRepository.save(league);
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
