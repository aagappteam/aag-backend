package aagapp_backend.services.league;

import aagapp_backend.dto.LeagueRequest;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.services.exception.ExceptionHandlingService;
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

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

    // Publish a new league
    public League publishLeague(LeagueRequest leagueRequest, Long vendorId) {
        try {
            // Ensure that the VendorEntity exists before proceeding
            Optional<VendorEntity> vendorEntityOptional = leagueRepository.findVendorById(vendorId);

            // Check if VendorEntity is present; if not, throw exception
            if (!vendorEntityOptional.isPresent()) {
                throw new NoSuchElementException("No records found for vendor with ID: " + vendorId);
            }

            League league = new League();
            league.setName(leagueRequest.getName());
            league.setDescription(leagueRequest.getDescription());
            league.setEntryFee(leagueRequest.getEntryFee());
            league.setVendorId(vendorId); // Set the vendor ID

            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            if (leagueRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledAtInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledAtInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The league must be scheduled at least 4 hours in advance.");
                }
                league.setScheduledAt(scheduledAtInKolkata);
            } else {
                league.setScheduledAt(nowInKolkata.plusMinutes(15)); // Default scheduled time
            }

            league.setEndDate(league.getScheduledAt().plusDays(7));

            league.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            league.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());

            league.setStatus(LeagueStatus.SCHEDULED);

            return leagueRepository.save(league);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new IllegalArgumentException("Invalid league schedule: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the league: " + e.getMessage(), e);
        }
    }


    // Update an existing league
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

    // Get leagues by vendor and status
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

    // Get a specific league by vendor ID and league ID
    public Page<League> getLeagueByVendorIdAndLeagueId(Long vendorId, Long leagueId, Pageable pageable) {
        try {
            Page<League> leagues = leagueRepository.findByVendorIdAndId(vendorId, leagueId, pageable);

            // Return an empty page if no leagues are found
            return leagues.isEmpty() ? Page.empty() : leagues;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while retrieving the league: " + e.getMessage(), e);
        }
    }

    // Get all leagues with filters for status and vendorId
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

    // Delete a league
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
