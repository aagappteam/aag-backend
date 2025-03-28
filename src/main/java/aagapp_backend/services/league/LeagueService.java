package aagapp_backend.services.league;

import aagapp_backend.dto.GameRequest;
import aagapp_backend.dto.LeagueRequest;
import aagapp_backend.entity.Challenge;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.game.GameRoom;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.enums.*;
import aagapp_backend.repository.ChallangeRepository;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.league.LeagueRoomRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.naming.LimitExceededException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private LeagueRoomRepository leagueRoomRepository;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private EntityManager em;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private AagGameRepository aagGameRepository;

    @Autowired
    private GameRoomRepository gameRoomRepository;

    @Autowired
    private ChallangeRepository challangeRepository;

    @Transactional
    public Challenge createChallenge(LeagueRequest leagueRequest, Long vendorId) {
        try {

            if (leagueRequest.getOpponentVendorId() != null) {
                // Check if the opponent vendor exists
                VendorEntity opponentVendor = vendorRepository.findById(leagueRequest.getOpponentVendorId())
                        .orElseThrow(() -> new RuntimeException("Opponent Vendor not found"));

                // Check if the opponent vendor's league status is available
                if (opponentVendor.getLeagueStatus() != LeagueStatus.AVAILABLE) {
                    throw new RuntimeException("The opponent vendor's league status is not available.");
                }
            }


            if (leagueRequest.getOpponentVendorId() != null && leagueRequest.getOpponentVendorId().equals(vendorId)) {
                throw new IllegalArgumentException("A vendor cannot challenge themselves");
            }

            Challenge challenge = new Challenge();
            if (leagueRequest.getOpponentVendorId() == null) {
                challenge.setOpponentVendorId(getRandomAvailableVendor(vendorId).getService_provider_id());
            }else{
                challenge.setOpponentVendorId(leagueRequest.getOpponentVendorId());
            }

            // Create the challenge entity
            challenge.setVendorId(vendorId);
            challenge.setExistinggameId(leagueRequest.getExistinggameId());
            challenge.setChallengeStatus(Challenge.ChallengeStatus.PENDING); // Initially set the status to PENDING
            challenge.setMove(leagueRequest.getMove());
            challenge.setFee(leagueRequest.getFee());
            challenge.setThemeId(leagueRequest.getThemeId());
            challenge.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            challenge.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());
            challenge.setScheduledAt(leagueRequest.getScheduledAt());
            challenge.setEndDate(leagueRequest.getEndDate());

            // Save the challenge in the database
            challangeRepository.save(challenge);

            return challenge;

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error creating challenge: " + e.getMessage(), e);
        }
    }



    @Transactional
    public void rejectChallenge(Long challengeId, Long vendorId) {
        try {
            // Fetch the challenge
            Challenge challenge = challangeRepository.findById(challengeId)
                    .orElseThrow(() -> new RuntimeException("Challenge not found"));

            // Check if the opponent is the correct vendor and challenge is pending
            if (!challenge.getOpponentVendorId().equals(vendorId)) {
                throw new IllegalArgumentException("You are not the opponent for this challenge.");
            }

            if (challenge.getChallengeStatus() != Challenge.ChallengeStatus.PENDING) {
                throw new IllegalArgumentException("The challenge is no longer pending.");
            }


            // Set the challenge status to REJECTED
            challenge.setChallengeStatus(Challenge.ChallengeStatus.REJECTED);
            challangeRepository.save(challenge);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error rejecting challenge: " + e.getMessage(), e);
        }
    }



    /*// Publish a new league
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
    }*/


    @Transactional
    public League publishLeague(Challenge leagueRequest, Long vendorId) throws LimitExceededException {
        try {

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            VendorEntity opponentVendor = em.find(VendorEntity.class, leagueRequest.getVendorId());
            // Create a new Game entity
            League league = new League();

            boolean isAvailable = isGameAvailableById(leagueRequest.getExistinggameId());

            if (!isAvailable) {
                throw new RuntimeException("game is not available");
            }
            Optional<AagAvailableGames> gameAvailable = aagGameRepository.findById(leagueRequest.getExistinggameId());
            league.setLeagueUrl(gameAvailable.get().getGameImage());

            ThemeEntity theme = em.find(ThemeEntity.class, leagueRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }

            // Set Vendor and Theme to the Game
            league.setName(opponentVendor.getFirst_name() +"v/s" +vendorEntity.getFirst_name());
            league.setVendorEntity(vendorEntity);
            league.setTheme(theme);
            league.setAagGameId(leagueRequest.getExistinggameId());
            league.setOpponentVendorId(leagueRequest.getOpponentVendorId());
            // Calculate moves based on the selected fee
            league.setFee(leagueRequest.getFee());

            if (leagueRequest.getMove() != null) {
                league.setMove(leagueRequest.getMove());
            }


            // Get current time in Kolkata timezone
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            if (leagueRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The game must be scheduled at least 4 hours in advance.");
                }
                league.setStatus(LeagueStatus.SCHEDULED);
                league.setScheduledAt(scheduledInKolkata);
                league.setEndDate(leagueRequest.getEndDate());
            } else {
                league.setStatus(LeagueStatus.ACTIVE);
                league.setScheduledAt(nowInKolkata.plusMinutes(15));
                league.setEndDate(leagueRequest.getEndDate());
            }

            // Set the minimum and maximum players
            if (leagueRequest.getMinPlayersPerTeam() != null) {
                league.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            }
            if (leagueRequest.getMaxPlayersPerTeam() != null) {
                league.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());
            }
            vendorEntity.setPublishedLimit((vendorEntity.getPublishedLimit() == null ? 0 : vendorEntity.getPublishedLimit()) + 1);


            // Set created and updated timestamps
            league.setCreatedDate(nowInKolkata);
            league.setUpdatedDate(nowInKolkata);

            // Save the game to get the game ID
            League savedLeague = leagueRepository.save(league);

            // Create the first GameRoom (initialized, 2 players max)
            LeagueRoom leagueRoom = createNewEmptyRoom(savedLeague);

            leagueRequest.setChallengeStatus(Challenge.ChallengeStatus.ACCEPTED);

            // Save the game room
            leagueRoomRepository.save(leagueRoom);

            // Generate a shareable link for the game
            String shareableLink = generateShareableLink(savedLeague.getId());
            savedLeague.setShareableLink(shareableLink);

            // Return the saved game with the shareable link
            return leagueRepository.save(savedLeague);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the game: " + e.getMessage(), e);
        }
    }

    // Create a new empty room for a game
    private LeagueRoom createNewEmptyRoom(League league) {
        LeagueRoom newRoom = new LeagueRoom();
        newRoom.setMaxPlayers(league.getMaxPlayersPerTeam());
        newRoom.setCurrentPlayers(new ArrayList<>());
        newRoom.setStatus(LeagueRoomStatus.INITIALIZED);
        newRoom.setCreatedAt(LocalDateTime.now());
        newRoom.setRoomCode(generateRoomCode());
        newRoom.setGame(league);
        return newRoom;
    }

    // Generate a unique room code
    private String generateRoomCode() {
        String roomCode;
        do {
            roomCode = RandomStringUtils.randomAlphanumeric(6);
        } while (gameRoomRepository.findByRoomCode(roomCode) != null);
        return roomCode;
    }

    public boolean isGameAvailableById(Long gameId) {
        // Use the repository to find a game by its name
        Optional<AagAvailableGames> game = aagGameRepository.findById(gameId);
        return game.isPresent(); // Return true if the game is found, false otherwise
    }

    private VendorEntity getRandomAvailableVendor(Long excludeVendorId) {
        // Fetch available vendors excluding the vendor calling the method
        List<VendorEntity> availableVendors = vendorRepository.findByLeagueStatus(LeagueStatus.AVAILABLE);
        availableVendors.removeIf(vendor -> vendor.getService_provider_id().equals(excludeVendorId));

        if (availableVendors.isEmpty()) {
            throw new RuntimeException("No available vendors found Right Now.");
        }

        // Return a random vendor from the remaining list
        return availableVendors.get(new Random().nextInt(availableVendors.size()));
    }


    private String generateShareableLink(Long gameId) {
        return "https://example.com/leagues/" + gameId;
    }


    public Page<League> getAllLeagues(Pageable pageable, LeagueStatus status, Long vendorId) {
        try {
            // Check if 'status' and 'vendorId' are provided and build a query accordingly
           if (status != null && vendorId != null) {
                return leagueRepository.findLeaguesByStatusAndVendorId(status, vendorId, pageable);
            } else
            if (status != null) {
                return leagueRepository.findByStatus(status, pageable);
            } else if (vendorId != null) {
                return leagueRepository.findByVendorServiceProviderId(vendorId, pageable);
            } else {
                return leagueRepository.findAll(pageable);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }


    // Scheduled task to auto reject expired challenges after 10 minutes
    @Scheduled(cron = "0 * * * * *")
    public void autoRejectExpiredChallenges() {
        try {
            // Find all leagues in the "SCHEDULED" state
            List<Challenge> challenges = challangeRepository.findByChallengeStatus(ChallengeStatus.PENDING);

            for (Challenge challenge : challenges) {
                // If the 10-minute window has passed since the challenge was scheduled
                if (ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(challenge.getCreatedAt().plusMinutes(1))) {
                    // Reject the challenge if it's expired
                    challenge.setChallengeStatus(Challenge.ChallengeStatus.REJECTED);
                    challangeRepository.save(challenge);

                }
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while auto-rejecting expired challenges " + e.getMessage());
        }
    }


}