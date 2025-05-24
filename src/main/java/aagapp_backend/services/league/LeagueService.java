package aagapp_backend.services.league;

import aagapp_backend.components.Constant;
import aagapp_backend.components.ZonedDateTimeAdapter;
import aagapp_backend.dto.*;

import aagapp_backend.entity.*;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.league.*;


import aagapp_backend.entity.team.LeagueTeam;

import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.players.Player;

import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.LeagueRoomStatus;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.repository.ChallangeRepository;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.league.*;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.CommonService;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.firebase.NotoficationFirebase;
import aagapp_backend.services.pricedistribute.MatchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.naming.LimitExceededException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.CacheRequest;
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    @Autowired
    private NotoficationFirebase notificationFirebase;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private LeagueRoomRepository leagueRoomRepository;

    @Autowired
    private MatchService matchService;

    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private EntityManager em;


    @Autowired
    private VendorRepository vendorRepository;


    @Autowired
    private AagGameRepository aagGameRepository;

    @Autowired
    private WalletRepository walletRepo;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;


    @Autowired
    private ChallangeRepository challangeRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private LeagueResultRecordRepository leagueResultRecordRepository;

    @Autowired
    private LeagueTeamRepository leagueTeamRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private LeaguePassRepository leaguePassRepository;

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;


    @Autowired
    private ResponseService responseService;


    @Transactional
    public Challenge createChallenge(LeagueRequest leagueRequest, Long vendorId) {
        try {

            AagAvailableGames game = aagGameRepository.findById(leagueRequest.getExistinggameId()).orElse(null);
            if (leagueRequest.getFee() <= 0)
                throw new BusinessException("Fee must be positive.", HttpStatus.BAD_REQUEST);
            if (leagueRequest.getFee() > Constant.MAX_FEE)
                throw new BusinessException("Fee exceeds maximum allowed limit.", HttpStatus.BAD_REQUEST);

/*            if (leagueRequest.getMinPlayersPerTeam() > leagueRequest.getMaxPlayersPerTeam()) {
                throw new BusinessException("Min players per team cannot be more than max players.", HttpStatus.BAD_REQUEST);
            }*/
            if (leagueRequest.getScheduledAt() != null &&
                    leagueRequest.getScheduledAt().isBefore(ZonedDateTime.now().plusHours(4))) {
                throw new BusinessException("Scheduled time must be at least 4 hours in the future.", HttpStatus.BAD_REQUEST);
            }

            Challenge challenge = new Challenge();
            if (game == null) {
                throw new BusinessException("Game not found with ID: " + leagueRequest.getExistinggameId(), HttpStatus.BAD_REQUEST);
            }
            if (leagueRequest.getOpponentVendorId() != null) {
                // Check if the opponent vendor exists
                VendorEntity opponentVendor = vendorRepository.findById(leagueRequest.getOpponentVendorId())
                        .orElseThrow(() -> new BusinessException("Opponent Vendor not found", HttpStatus.BAD_REQUEST));
                challenge.setOpponentVendorName(opponentVendor.getFirst_name() + " " + opponentVendor.getLast_name());
                challenge.setOpponentVendorProfilePic(opponentVendor.getProfilePic());

                // Check if the opponent vendor's league status is available
                if (opponentVendor.getLeagueStatus() != LeagueStatus.AVAILABLE) {
                    throw new BusinessException("The opponent vendor's league status is not available.", HttpStatus.BAD_REQUEST);
                }
            }

            VendorEntity vendor = vendorRepository.findById(vendorId)
                    .orElseThrow(() -> new BusinessException("Vendor not found with this id: " + vendorId, HttpStatus.BAD_REQUEST));

            if (leagueRequest.getOpponentVendorId() != null && leagueRequest.getOpponentVendorId().equals(vendorId)) {
                throw new BusinessException("A vendor cannot challenge themselves", HttpStatus.BAD_REQUEST);
            }

            if (vendor.getLeagueStatus() != LeagueStatus.AVAILABLE) {
                throw new BusinessException("You are not available to challenge others.", HttpStatus.BAD_REQUEST);
            }

            if (leagueRequest.getOpponentVendorId() == null) {
                Long oppnentvendorid = getRandomAvailableVendor(vendorId).getService_provider_id();
                challenge.setOpponentVendorId(oppnentvendorid);
            } else {
                Long oppnentvendorid = leagueRequest.getOpponentVendorId();
                challenge.setOpponentVendorId(oppnentvendorid);
            }

            // Create the challenge entity
            challenge.setVendorId(vendorId);
            challenge.setExistinggameId(leagueRequest.getExistinggameId());
            challenge.setChallengeStatus(Challenge.ChallengeStatus.PENDING); // Initially set the status to PENDING
            challenge.setFee(leagueRequest.getFee());
            if (leagueRequest.getFee() > 10) {
                challenge.setMove(Constant.TENMOVES);
            } else {
                challenge.setMove(Constant.SIXTEENMOVES);

            }

            challenge.setName(game.getGameName());
            challenge.setThemeId(leagueRequest.getThemeId());
            challenge.setMinPlayersPerTeam(1);
            challenge.setMaxPlayersPerTeam(2);
            if (leagueRequest.getScheduledAt() != null) {
                challenge.setScheduledAt(leagueRequest.getScheduledAt());
            }

            challenge.setVendorName(vendor.getFirst_name() + " " + vendor.getLast_name());
            challenge.setVendorProfilePic(vendor.getProfilePic());
            challenge.setName(game.getGameName());
            challenge.setGameImage(game.getGameImage());
            // Save the challenge in the database
            challangeRepository.save(challenge);

            Long receiverVendorId = challenge.getOpponentVendorId();

            VendorEntity opponentVendor = vendorRepository.findById(receiverVendorId)
                    .orElseThrow(() -> new BusinessException("Opponent Vendor not found", HttpStatus.BAD_REQUEST));
            String fcmToken = opponentVendor.getFcmToken();

            if (fcmToken != null && !fcmToken.isEmpty()) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
                        .setPrettyPrinting()
                        .create();

                String challengeJson = gson.toJson(challenge); // Convert Challenge to JSON string

                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setToken(fcmToken);
                notificationRequest.setTitle("League Challenge Received from " + vendor.getFirst_name() + "! ");
                notificationRequest.setBody(challengeJson);
                notificationRequest.setTopic("League Challenge"); // Optional, just for tagging


                try {
//                    notificationFirebase.sendMessageToToken(notificationRequest);
                    String title = "League Challenge Received!";
                    String body = vendor.getFirst_name() + " has challenged you to a " +
                            game.getGameName() + " league. Open the Challenge section to accept it!";



                    notificationFirebase.sendNotification(fcmToken, title, body);

                } catch (Exception e) {
                    throw new BusinessException("Error sending notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            if (opponentVendor != null) {
                Notification notification = new Notification();
                notification.setRole("Vendor");

                notification.setVendorId(opponentVendor.getService_provider_id());

                notification.setDescription("League challenge");
                notification.setDetails(vendor.getFirst_name() + " has challenged for a League");
                notificationRepository.save(notification);

            }


            return challenge;

        } catch (BusinessException e) {
            exceptionHandling.handleException(e.getStatus(), e);
            throw e;

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new BusinessException("Error creating challenge: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public void rejectChallenge(Long challengeId, Long vendorId) {
        try {
            // Fetch the challenge
            Challenge challenge = challangeRepository.findById(challengeId)
                    .orElseThrow(() -> new BusinessException("Challenge not found", HttpStatus.BAD_REQUEST));

            // Check if the opponent is the correct vendor and challenge is pending
            if (!challenge.getOpponentVendorId().equals(vendorId)) {
                throw new BusinessException("You are not the opponent for this challenge.", HttpStatus.UNAUTHORIZED);
            }

            if (challenge.getChallengeStatus() != Challenge.ChallengeStatus.PENDING) {
                throw new BusinessException("The challenge is no longer pending.", HttpStatus.BAD_REQUEST);
            }


            // Set the challenge status to REJECTED
            challenge.setChallengeStatus(Challenge.ChallengeStatus.REJECTED);
            challangeRepository.save(challenge);

            VendorEntity opponentVendor = vendorRepository.findById(challengeId)
                    .orElseThrow(() -> new BusinessException("Opponent Vendor not found", HttpStatus.BAD_REQUEST));
            String fcmToken = opponentVendor.getFcmToken(); // or whatever field name is used

            if (fcmToken != null && !fcmToken.isEmpty()) {


/*                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setToken(fcmToken);
                notificationRequest.setTitle("Challenge Declined");
                notificationRequest.setBody("Unfortunately, your opponent has declined your league challenge.");
                notificationRequest.setTopic("League Challenge Rejected");
                notificationRequest.setTopic("League Challenge Rejected"); // Optional, just for tagging*/

                try {
//                    notificationFirebase.sendMessageToToken(notificationRequest);

                    String title = "Challenge Declined";
                    String body = "Unfortunately, your opponent has declined your league challenge.";

                    notificationFirebase.sendNotification(fcmToken, title, body);
                } catch (Exception e) {
                    System.out.println("Error sending notification: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error rejecting challenge: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Challenge getChallengeById(Long challengeId) {
        return challangeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException("Challenge not found with ID: " + challengeId, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Page<Challenge> getChallengesByOpponentVendorIds(List<Long> opponentVendorId, Challenge.ChallengeStatus status, Pageable pageable) {
        Page<Challenge> challenges;

        if (status != null) {
            challenges = challangeRepository.findByOpponentVendorIdInAndStatus(opponentVendorId, status, pageable);
        } else {
            challenges = challangeRepository.findByOpponentVendorIdIn(opponentVendorId, pageable);
        }

        if (challenges.isEmpty()) {
            throw new BusinessException("No challenges found for opponentVendorIds: " + opponentVendorId +
                    (status != null ? " with status: " + status : ""), HttpStatus.NOT_FOUND);
        }

        return challenges;
    }


    @Transactional
    public Page<Challenge> getByVendorIdInAndStatus(List<Long> vendorIds, Challenge.ChallengeStatus status, Pageable pageable) {
        Page<Challenge> challenges;

        if (status != null) {
            challenges = challangeRepository.findByVendorIdInAndStatus(vendorIds, status, pageable);
        } else {
            challenges = challangeRepository.findByVendorIdIn(vendorIds, pageable);
        }

        if (challenges.isEmpty()) {
            throw new BusinessException("No challenges found for vendorIds: " + vendorIds +
                    (status != null ? " with status: " + status : ""), HttpStatus.NOT_FOUND);
        }

        return challenges;
    }


    @Transactional
    public League publishLeague(Challenge leagueRequest, Long vendorId) throws LimitExceededException {
        try {

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            VendorEntity opponentVendor = em.find(VendorEntity.class, leagueRequest.getVendorId());
            // Create a new Game entity
            League league = new League();

            boolean isAvailable = isGameAvailableById(leagueRequest.getExistinggameId());

            if (!isAvailable) {
                throw new BusinessException("game is not available" + leagueRequest.getExistinggameId(), HttpStatus.BAD_REQUEST);
            }
            Optional<AagAvailableGames> gameAvailable = aagGameRepository.findById(leagueRequest.getExistinggameId());
/*
            league.setLeagueUrl(gameAvailable.get().getGameImage());
*/

/*
 AagAvailableGames game = gameAvailable.get();
        // Try to get gameimageUrl from any of the themes
                    String themeImageUrl = game.getThemes().stream()
                            .map(ThemeEntity::getGameimageUrl)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);

        // Fallback to gameImage if no theme image URL
                    String leagueUrl = (themeImageUrl != null) ? themeImageUrl : game.getGameImage();*/


            AagAvailableGames gameEntity = gameAvailable.orElseThrow(() ->
                    new BusinessException("Game not found with ID: " + leagueRequest.getExistinggameId(), HttpStatus.NOT_FOUND)
            );


            league.setLeagueUrl(commonService.resolveGameImageUrl(gameEntity, leagueRequest.getThemeId()));
            league.setGameName(gameAvailable.get().getGameName());

            ThemeEntity theme = em.find(ThemeEntity.class, leagueRequest.getThemeId());
            if (theme == null) {
                throw new BusinessException("No theme found with the provided ID", HttpStatus.BAD_REQUEST);
            }

            // Set Vendor and Theme to the Game
            league.setName(opponentVendor.getFirst_name() + " v/s " + vendorEntity.getFirst_name());
            league.setVendorEntity(opponentVendor);
            league.setChallengingVendorId(opponentVendor.getService_provider_id());
//            league.setTeamChallengingVendorName("Team " + opponentVendor.getFirst_name() + " " + opponentVendor.getLast_name());
//            league.setTeamOpponentVendorName("Team " + vendorEntity.getFirst_name() + " " + vendorEntity.getLast_name());
            league.setChallengingVendorName(opponentVendor.getFirst_name() + " " + opponentVendor.getLast_name());
            league.setChallengingVendorProfilePic(opponentVendor.getProfilePic());
            league.setOpponentVendorName(vendorEntity.getFirst_name() + " " + vendorEntity.getLast_name());
            league.setOpponentVendorProfilePic(vendorEntity.getProfilePic());
            league.setTheme(theme);
            league.setAagGameId(leagueRequest.getExistinggameId());
            league.setOpponentVendorId(leagueRequest.getOpponentVendorId());
            // Calculate moves based on the selected fee
            league.setFee(leagueRequest.getFee());

            if (leagueRequest.getMove() != null) {
                league.setMove(leagueRequest.getMove());
            }

            // Get current time in Kolkata timezone
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(15);
            if (leagueRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new BusinessException("The game must be scheduled at least 4 hours in advance.", HttpStatus.BAD_REQUEST);
                }
                league.setStatus(LeagueStatus.SCHEDULED);
                league.setScheduledAt(leagueRequest.getScheduledAt());
                league.setEndDate(league.getScheduledAt().plusHours(Constant.LEAGUE_SESSION_TIME));
            } else {
                league.setStatus(LeagueStatus.SCHEDULED);
                league.setScheduledAt(nowInKolkata);
                league.setEndDate(league.getScheduledAt().plusHours(Constant.LEAGUE_SESSION_TIME));
            }
            league.setMinPlayersPerTeam(1);
            league.setMaxPlayersPerTeam(2);


            // Set created and updated timestamps
            league.setCreatedDate(nowInKolkata);
            league.setUpdatedDate(nowInKolkata);


            // Save the game to get the game ID
            League savedLeague = leagueRepository.save(league);

            // 👇 Add LeagueTeam creation here

            LeagueTeam challengingTeam = new LeagueTeam();
            challengingTeam.setTeamName("Team " + opponentVendor.getFirst_name() + " " + opponentVendor.getLast_name());
            challengingTeam.setVendor(opponentVendor);
            challengingTeam.setProfilePic(opponentVendor.getProfilePic());
            challengingTeam.setLeague(savedLeague);
            LeagueTeam opponentTeam = new LeagueTeam();
            opponentTeam.setTeamName("Team " + vendorEntity.getFirst_name() + " " + vendorEntity.getLast_name());
            opponentTeam.setVendor(vendorEntity);
            opponentTeam.setProfilePic(vendorEntity.getProfilePic());
            opponentTeam.setLeague(savedLeague);

            leagueTeamRepository.saveAll(List.of(challengingTeam, opponentTeam));

            List<LeagueTeam> teams = leagueTeamRepository.findByLeague(savedLeague);
            savedLeague.setTeams(teams);

            // 🔄 Optional: Save their IDs in League entity for fast reference
            //            savedLeague.setChallengingTeamId(challengingTeam.getId());
            //            savedLeague.setOpponentTeamId(opponentTeam.getId());


            // Create the first GameRoom (initialized, 2 players max)
            LeagueRoom leagueRoom = createNewEmptyRoom(savedLeague);

            leagueRequest.setChallengeStatus(Challenge.ChallengeStatus.ACCEPTED);

            // Save the game room
            leagueRoomRepository.save(leagueRoom);

            // Generate a shareable link for the game
            String shareableLink = generateShareableLink(savedLeague.getId());
            savedLeague.setShareableLink(shareableLink);
            vendorEntity.setPublishedLimit((vendorEntity.getPublishedLimit() == null ? 0 : vendorEntity.getPublishedLimit()) + 1);
            opponentVendor.setPublishedLimit((opponentVendor.getPublishedLimit() == null ? 0 : opponentVendor.getPublishedLimit()) + 1);
            // Return the saved game with the shareable link

            String fcmToken = opponentVendor.getFcmToken(); // or whatever field name is used

            if (fcmToken != null && !fcmToken.isEmpty()) {

              /*  NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setToken(fcmToken);
                notificationRequest.setTitle("Challenge Accepted!");
                notificationRequest.setBody(vendorEntity.getFirst_name() + " is ready. Your league will be active in 15 minutes!");
                notificationRequest.setTopic("League Challenge Accepted"); // Optional, just for tagging*/

                try {
//                    notificationFirebase.sendMessageToToken(notificationRequest);

                    String title = "Challenge Accepted!";
                    String body = vendorEntity.getFirst_name() + " is ready. Your league will be active in 15 minutes!";

                    notificationFirebase.sendNotification(fcmToken, title, body);
                } catch (Exception e) {
//                    System.out.println("Error sending notification: " + e.getMessage());
                    throw new BusinessException("Error sending notification: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
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
        newRoom.setLeague(league);

        // Save the room first so it gets an ID
        leagueRoomRepository.save(newRoom);
        return newRoom;
    }

    public String createNewGame(String baseUrl, Long gameId, Long roomId, Integer players, Integer move, BigDecimal prize) {
        try {
            // Construct the URL for the POST request, including query parameters

            String url = baseUrl + "/CreateNewGame?gametype=LEAGUE"
                    + "&gameid=" + gameId
                    + "&roomid=" + roomId
                    + "&players=" + players
                    + "&prize=" + prize
                    + "&moves=" + move;
            System.out.println("url: " + url);
            // Create headers (optional, but good practice to include Content-Type for clarity)
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            // Create the HttpEntity with headers (no body needed for query parameters)
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Send POST request to the external service
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse the response body (assuming it's a JSON response)
            String responseBody = response.getBody();
            System.out.println("responseBody: " + responseBody);

            // Assuming the response is JSON and contains "GamePassword"
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            String gamePassword = jsonResponse.get("GamePassword").asText();
            System.out.println("gamePassword: " + gamePassword);

            return gamePassword;

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while creating the game on the server: " + e.getMessage(), e);
        }
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
            throw new BusinessException("No available vendors found Right Now.", HttpStatus.NOT_FOUND);
        }

        // Return a random vendor from the remaining list
        return availableVendors.get(new Random().nextInt(availableVendors.size()));
    }


    private String generateShareableLink(Long gameId) {
        return "https://example.com/leagues/" + gameId;
    }


    public Page<League> getAllLeagues(Pageable pageable, LeagueStatus status, Long vendorId) {
        try {
            // Fetch the leagues from the repository based on the parameters
            Page<League> leagues;

            if (status != null && vendorId != null) {
                leagues = leagueRepository.findLeaguesByStatusAndVendorId(status, vendorId, pageable);
            } else if (status != null) {
                leagues = leagueRepository.findByStatus(status, pageable);
            } else if (vendorId != null) {
                leagues = leagueRepository.findByVendorServiceProviderId(vendorId, pageable);
            } else {
                leagues = leagueRepository.findAll(pageable);
            }

            // Map the fetched leagues to LeagueResponse DTOs
            return leagues;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }


    public Page<League> getAllActiveLeaguesByVendor(Pageable pageable, Long vendorId) {
        try {
            // Check if 'status' and 'vendorId' are provided and build a query accordingly
            LeagueStatus status = LeagueStatus.ACTIVE;
            if (status != null && vendorId != null) {
                return leagueRepository.findLeaguesByStatusAndVendorId(status, vendorId, pageable);
            } else {
                return leagueRepository.findAll(pageable);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }


    public Page<League> getAllLeaguesByVendorId(Pageable pageable, Long vendorId) {
        try {
            if (vendorId != null) {
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
            List<Challenge> challenges = challangeRepository.findByChallengeStatus(Challenge.ChallengeStatus.PENDING);

            for (Challenge challenge : challenges) {
                // If the 10-minute window has passed since the challenge was scheduled
                if (ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).isAfter(challenge.getCreatedAt().plusMinutes(10))) {
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


    @Transactional
    public ResponseEntity<?> takeEntryForLeague(Long playerId, Long leagueId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new BusinessException("League not found with ID: " + leagueId, HttpStatus.BAD_REQUEST));

        Wallet wallet = player.getCustomer().getWallet();
        Double unplayedBalance = wallet.getUnplayedBalance();
        BigDecimal winningAmount = wallet.getWinningAmount();
        Double leagueFee = league.getFee();

        // Check if player already has a league pass
        LeaguePass existingPass = leaguePassRepository.findByPlayerAndLeague(player, league).orElse(null);
        if (existingPass != null) {
            return responseService.generateErrorResponse("Player already entered the league. You can take extra passes to contribute to the league", HttpStatus.BAD_REQUEST);
        }

        // Check if total balance (unplayed + winning) is sufficient
        double totalAvailable = unplayedBalance + winningAmount.doubleValue();
        if (leagueFee > totalAvailable) {
            return responseService.generateErrorResponse("Insufficient wallet balance", HttpStatus.BAD_REQUEST);
        }

        // Deduct from unplayed first, then from winning
        if (leagueFee <= unplayedBalance) {
            wallet.setUnplayedBalance(unplayedBalance - leagueFee);
        } else {
            double remainingAmount = leagueFee - unplayedBalance;
            wallet.setUnplayedBalance(0.0);
            wallet.setWinningAmount(winningAmount.subtract(BigDecimal.valueOf(remainingAmount)));
        }

        walletRepo.save(wallet);

        // Create new LeaguePass with 3 passes
        LeaguePass pass = new LeaguePass();
        pass.setPlayer(player);
        pass.setLeague(league);
        pass.setPassCount(3);
        leaguePassRepository.save(pass);

        // Create Notification
        CustomCustomer customer = customCustomerService.getCustomerById(playerId);
        Notification notification = new Notification();
        notification.setCustomerId(customer.getId());
        notification.setDescription("Wallet balance deducted");
        notification.setAmount(leagueFee);
        notification.setDetails("Rs. " + leagueFee + " deducted for league entry: " + league.getName());
        notificationRepository.save(notification);

        // Prepare response
        Map<String, Object> data = new HashMap<>();
        data.put("walletBalance", wallet.getUnplayedBalance());
        data.put("winningAmount", wallet.getWinningAmount());
        data.put("leaguePasses", pass.getPassCount());
        data.put("playerName", player.getPlayerName());
        data.put("leagueId", league.getId());
        data.put("leagueName", league.getName());

        return responseService.generateSuccessResponse("League entry successful. 3 passes added.", data, HttpStatus.OK);
    }


    @Transactional
    public ResponseEntity<?> selectLeagueTeam(Long playerId, Long leagueId, Long teamId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new BusinessException("League not found with ID: " + leagueId, HttpStatus.BAD_REQUEST));

        LeagueTeam team = leagueTeamRepository.findById(teamId)
                .orElseThrow(() -> new BusinessException("Team not found with ID: " + teamId, HttpStatus.BAD_REQUEST));


        if (!team.getLeague().getId().equals(league.getId())) {
            return responseService.generateErrorResponse("Selected team does not belong to the specified league", HttpStatus.BAD_REQUEST);
        }

        LeaguePass existingPass = leaguePassRepository.findByPlayerAndLeague(player, league).orElse(null);
        if (existingPass == null) {
            return responseService.generateErrorResponse("Player has not entered the league or has no entries", HttpStatus.BAD_REQUEST);
        }

        if (existingPass.getSelectedTeamId() != null) {
            return responseService.generateErrorResponse("Player already selected a team", HttpStatus.BAD_REQUEST);
        }

        existingPass.setSelectedTeamId(teamId);
        team.setTeamPlayersCount(team.getTeamPlayersCount() + 1);
        player.setTeam(team);
        leaguePassRepository.save(existingPass);

        return responseService.generateSuccessResponse("Team selected successfully", "player selected team" + team.getTeamName(), HttpStatus.OK);
    }


    @Transactional
    public ResponseEntity<?> takePassForLeague(Long playerId, Long leagueId) {
        // Step 1: Get Player and League
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new BusinessException("League not found with ID: " + leagueId, HttpStatus.BAD_REQUEST));

        // Step 2: Get wallet and check balance
        Wallet wallet = player.getCustomer().getWallet();
        Double unplayedBalance = wallet.getUnplayedBalance();
        BigDecimal winningAmount = wallet.getWinningAmount();
        Double passCost = Constant.LEAGUE_PASSES_FEE;

        double totalAvailable = unplayedBalance + winningAmount.doubleValue();
        if (passCost > totalAvailable) {
            return responseService.generateErrorResponse("Insufficient wallet balance", HttpStatus.BAD_REQUEST);
        }

        // Step 3: Check if player has already entered the league
        LeaguePass pass = leaguePassRepository.findByPlayerAndLeague(player, league).orElse(null);
        if (pass == null) {
            return responseService.generateErrorResponse("Player has not entered the league yet. Please enter the league first.", HttpStatus.BAD_REQUEST);
        }

        // Step 4: Deduct from unplayed and winning amount
        if (passCost <= unplayedBalance) {
            wallet.setUnplayedBalance(unplayedBalance - passCost);
        } else {
            double remaining = passCost - unplayedBalance;
            wallet.setUnplayedBalance(0.0);
            wallet.setWinningAmount(winningAmount.subtract(BigDecimal.valueOf(remaining)));
        }

        walletRepo.save(wallet);

        // Step 5: Add 3 more passes
        pass.setPassCount(pass.getPassCount() + 3);
        leaguePassRepository.save(pass);

        // Step 6: Create Notification
        CustomCustomer customer = customCustomerService.getCustomerById(playerId);
        Notification notification = new Notification();
        notification.setCustomerId(customer.getId());
        notification.setDescription("Wallet balance deducted");
        notification.setAmount(passCost);
        notification.setDetails("Rs. " + passCost + " deducted to take extra passes for league: " + league.getName());
        notificationRepository.save(notification);

        // Step 7: Prepare response
        Map<String, Object> data = new HashMap<>();
        data.put("walletBalance", wallet.getUnplayedBalance());
        data.put("winningAmount", wallet.getWinningAmount());
        data.put("leaguePasses", pass.getPassCount());
        data.put("playerName", player.getPlayerName());
        data.put("leagueId", league.getId());
        data.put("leagueName", league.getName());

        return responseService.generateSuccessResponse("3 more League passes added successfully", data, HttpStatus.OK);
    }



    @Transactional
    public ResponseEntity<?> joinRoom(Long playerId, Long leagueId, Long teamId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new BusinessException("League not found with ID: " + leagueId, HttpStatus.BAD_REQUEST));

            LeaguePass leaguePass = leaguePassRepository.findByPlayerAndLeague(player, league)
                    .orElseThrow(() -> new BusinessException("No league passes found for this player in the selected league", HttpStatus.BAD_REQUEST));

            if (!teamId.equals(leaguePass.getSelectedTeamId())) {
                return responseService.generateErrorResponse("You have not selected a team or you want to join with another team", HttpStatus.BAD_REQUEST);
            }

            if (leaguePass.getPassCount() == 0) {
                return responseService.generateErrorResponse(
                        "You have used all your passes. Buy more to continue.",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (player.getLeagueRoom() != null) {

                leaveRoom(player.getPlayerId(), player.getLeagueRoom().getLeague().getId());

               /* return responseService.generateErrorResponse(
                        "Player already in room with ID: " + player.getPlayerId(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );*/
            }

            // ✅ Get the team by teamId
            LeagueTeam team = leagueTeamRepository.findById(teamId)
                    .orElseThrow(() -> new BusinessException("Team not found with ID: " + teamId, HttpStatus.BAD_REQUEST));

            // ✅ Validate team belongs to the league
            if (!team.getLeague().getId().equals(leagueId)) {
                return responseService.generateErrorResponse(
                        "Team does not belong to this league.",
                        HttpStatus.BAD_REQUEST
                );
            }


            LeagueRoom leagueRoom = findAvailableGameRoom(league);

            // ✅ Use team.getTeamName() instead of passing string teamName
            boolean playerJoined = addPlayerToRoom(leagueRoom, player, team.getTeamName());
            if (!playerJoined) {
                return responseService.generateErrorResponse(
                        "Room is already full with ID: " + leagueRoom.getRoomCode(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            // After player successfully joins the room
            leaguePass.setPassCount(leaguePass.getPassCount() - 1);
            leaguePassRepository.save(leaguePass);


            if (leagueRoom.getCurrentPlayers().size() == leagueRoom.getMaxPlayers()) {

                BigDecimal toalprize = matchService.getWinningAmountLeague(leagueRoom);

//                String gamePassword = this.createNewGame(Constant.baseUrl, league.getId(), leagueRoom.getId(), leagueRoom.getMaxPlayers(), league.getMove(), toalprize);

                String gameName = league.getGameName().toLowerCase();
                String gamePassword = null;

                if (gameName.equals("ludo")) {
                    gamePassword = this.createNewGame(Constant.ludobaseurl, league.getId(), leagueRoom.getId(), leagueRoom.getMaxPlayers(), league.getMove(), toalprize);


                } else if (gameName.equals("snake & ladder")) {
                    gamePassword = this.createNewGame(Constant.snakebaseUrl, league.getId(), leagueRoom.getId(), leagueRoom.getMaxPlayers(), league.getMove(), toalprize);

                } else {
                    throw new BusinessException("Unsupported game: " + gameName, HttpStatus.BAD_REQUEST);
                }

                leagueRoom.setGamepassword(gamePassword);

                leagueRoom.setStatus(LeagueRoomStatus.ONGOING);
                leagueRoomRepository.save(leagueRoom);


                LeagueRoom newRoom = createNewEmptyRoom(league);
                leagueRoomRepository.save(newRoom);
            }

            playerRepository.save(player);

            return responseService.generateSuccessResponse(
                    "Player joined the Game Room",
                    leagueRoom,
                    HttpStatus.OK
            );

        } catch (BusinessException e) {
            exceptionHandling.handleException(e.getStatus(), e);
            throw e;

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse(
                    "Player cannot join the room because: " + e.getMessage(),
                    HttpStatus.NOT_FOUND
            );
        }
    }


    public ResponseEntity<?> getRoomById(Long roomId) {
        try {
            LeagueRoom gameRoom = leagueRoomRepository.findById(roomId).orElse(null);
            if (gameRoom == null) {
                return ResponseEntity.notFound().build();
            }

            return responseService.generateSuccessResponse("Room fetched successfully", gameRoom, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in getting game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public ResponseEntity<?> getLeaguePasses(Long playerId, Long leagueId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new BusinessException("League not found with ID: " + leagueId, HttpStatus.BAD_REQUEST));

            LeaguePass pass = leaguePassRepository.findByPlayerAndLeague(player, league).orElse(null);

            Map<String, Object> data = new HashMap<>();
            data.put("playerId", player.getPlayerId());
            data.put("leagueId", league.getId());
            data.put("leagueName", league.getName());
            data.put("selectedTeamId", pass != null ? pass.getSelectedTeamId() : null);
            data.put("passCount", pass != null ? pass.getPassCount() : 0);

            return responseService.generateSuccessResponse("League passes fetched successfully", data, HttpStatus.OK);


        } catch (BusinessException e) {
            exceptionHandling.handleException(e.getStatus(), e);
            throw e;

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error in getting league passes: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> leaveRoom(Long playerId, Long leagueId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new BusinessException("League not found with ID: " + leagueId, HttpStatus.BAD_REQUEST));

            LeagueRoom leagueRoom = player.getLeagueRoom();

            if (player.getLeagueRoom() == null || leagueRoom == null || !leagueRoom.getLeague().getId().equals(league.getId())) {
                return responseService.generateErrorResponse("Player is not in league with this id: " + player.getPlayerId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            player.setLeagueRoom(null);
//            player.setTeam(null);
            playerRepository.save(player);

            List<Player> remainingPlayers = playerRepository.findAllByLeagueRoom(leagueRoom);
            if (remainingPlayers.isEmpty()) {
                leagueRoom.setStatus(LeagueRoomStatus.COMPLETED);
                leagueRoomRepository.save(leagueRoom);
            }
            LeagueRoom updated = leagueRoomRepository.save(leagueRoom);

            return responseService.generateSuccessResponse("Player left the League Room ", updated, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player can not left the room because " + e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }


    @Transactional
    public ResponseEntity<?> leaveLeague(Long playerId, Long leagueId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId));

            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));

            LeagueRoom leagueRoom = player.getLeagueRoom();

            if (player.getLeagueRoom() == null || leagueRoom == null || !leagueRoom.getLeague().getId().equals(league.getId())) {
                return responseService.generateErrorResponse("Player is not in league with this id: " + player.getPlayerId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            player.setLeagueRoom(null);
            /*  player.setTeam(null);*/
//            leagueRoom.setActivePlayersCount(leagueRoom.getCurrentPlayers().size());
            playerRepository.save(player);
            LeagueRoom updated = leagueRoomRepository.save(leagueRoom);

            return responseService.generateSuccessResponse("Player left the League Room ", updated, HttpStatus.OK);
        } catch (BusinessException e) {
            exceptionHandling.handleException(e.getStatus(), e);
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player can not left the room because " + e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }


    public List<LeagueTeam> getTeamsByLeagueId(Long leagueId) {
        return leagueTeamRepository.findByLeagueId(leagueId);
    }

    public LeagueRoom findAvailableGameRoom(League league) {
        // Find a game room that has available space and matches the specified game
        List<LeagueRoom> availableRooms = leagueRoomRepository.findByLeagueAndStatus(league, LeagueRoomStatus.INITIALIZED);
        // Loop through the available rooms and return the first one with space
        for (LeagueRoom room : availableRooms) {
            if (room.getCurrentPlayers().size() < room.getMaxPlayers()) {
                return room;
            }
        }

        // If no available room is found, return null or create a new room
        LeagueRoom newRoom = createNewEmptyRoom(league);
        leagueRoomRepository.save(newRoom); // Save the new room
        return newRoom;
    }


    public boolean addPlayerToRoom(LeagueRoom leagueRoom, Player player, String teamName) {
        // Check if the game room has space for the player
        if (leagueRoom.getCurrentPlayers().size() < leagueRoom.getMaxPlayers()) {
            leagueRoom.getCurrentPlayers().add(player);
            player.setLeagueRoom(leagueRoom);
//            player.setTeamName(teamName);
//            player.setLeaguePasses(player.getLeaguePasses() - 1);
            leagueRoomRepository.save(leagueRoom);
            playerRepository.save(player);

            // Return true as the player was successfully added
            return true;
        } else {
            return false;
        }
    }

    // Generate a unique room code
    private String generateRoomCode() {
        String roomCode;
        do {
            roomCode = RandomStringUtils.randomAlphanumeric(6);
        } while (leagueRoomRepository.findByRoomCode(roomCode) != null);
        return roomCode;
    }


    /*@Transactional
    public void processMatchRecentOld(LeagueMatchProcess leagueMatchProcess) {
        Optional<LeagueRoom> leagueRoomOpt = leagueRoomRepository.findById(leagueMatchProcess.getRoomId());
        if (leagueRoomOpt.isEmpty()) {
            throw new RuntimeException("League room not found with ID: " + leagueMatchProcess.getRoomId());
        }
        LeagueRoom leagueRoom = leagueRoomOpt.get();

        Optional<League> legueOpt = leagueRepository.findById(leagueRoom.getLeague().getId());
        if (legueOpt.isEmpty()) {
            throw new RuntimeException("League not found with ID: " + leagueRoom.getLeague().getId());
        }
        League league = legueOpt.get();

        // Fetch players from the room
        List<Player> roomPlayers = playerRepository.findByRoomId(leagueMatchProcess.getRoomId());
        if (roomPlayers.isEmpty()) {
            return;
//            throw new RuntimeException("No players found in room " + leagueMatchProcess.getRoomId());
        }

        // Debug log
        System.out.println("[INFO] Players in the room: " + roomPlayers.stream()
                .map(p -> "PlayerId=" + p.getPlayerId())
                .collect(Collectors.joining(", ")));

        // Replace playerId == 0 in leagueMatchProcess.getPlayers() with valid room playerId
        List<LeaguePlayerDtoWinner> playersToProcess = leagueMatchProcess.getPlayers().stream()
                .map(p -> {
                    if (p.getPlayerId() == 0) {
                        Player availablePlayer = roomPlayers.stream()
                                .filter(rp -> leagueMatchProcess.getPlayers().stream()
                                        .noneMatch(existingPlayer -> existingPlayer.getPlayerId() == rp.getPlayerId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No valid player found in room to assign"));

                        // Assign the playerId from available room player
                        p.setPlayerId(availablePlayer.getPlayerId());
                        System.out.println("[INFO] Assigned playerId " + availablePlayer.getPlayerId() + " to a player with missing ID");
                    }
                    return p;
                })
                .collect(Collectors.toList());

        // Now use playersToProcess (updated list)
        LeaguePlayerDtoWinner winner = determineWinner(playersToProcess); // Determine winner
        Long winnerTeamId = playerRepository.findById(winner.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + winner.getPlayerId()))
                .getTeam().getId();

        LeagueTeam winnerLeagueTeam = leagueTeamRepository.findById(winnerTeamId)
                .orElseThrow(() -> new RuntimeException("LeagueTeam not found with ID: " + winnerTeamId));

        // Identify the loser
        LeaguePlayerDtoWinner loser = playersToProcess.stream()
                .filter(p -> !p.getPlayerId().equals(winner.getPlayerId()))
                .findFirst().orElseThrow(() -> new RuntimeException("No loser found"));

        Long looserTeamId = playerRepository.findById(loser.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + loser.getPlayerId()))
                .getTeam().getId();

        LeagueTeam looserLeagueTeam = leagueTeamRepository.findById(looserTeamId)
                .orElseThrow(() -> new RuntimeException("LeagueTeam not found with ID: " + looserTeamId));

        Player winnerPlayer = playerRepository.findById(winner.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + winner.getPlayerId()));

        LeagueResultRecord winnerRecord = new LeagueResultRecord();
        winnerRecord.setRoomId(leagueRoom.getId());
        winnerRecord.setLeague(league);
        winnerRecord.setPlayer(winnerPlayer);
        winnerRecord.setLeagueTeam(winnerLeagueTeam);
        winnerRecord.setTotalScore(winnerRecord.getTotalScore() + winner.getScore());
        winnerLeagueTeam.setTotalScore(winnerLeagueTeam.getTotalScore() + winner.getScore());
        winnerRecord.setIsWinner(true);
        winnerRecord.setPlayedAt(LocalDateTime.now());

        Player loserPlayer = playerRepository.findById(loser.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + loser.getPlayerId()));

        LeagueResultRecord loserRecord = new LeagueResultRecord();
        loserRecord.setRoomId(leagueRoom.getId());
        loserRecord.setLeague(league);
        loserRecord.setPlayer(loserPlayer);
        loserRecord.setLeagueTeam(looserLeagueTeam);
        loserRecord.setTotalScore(loserRecord.getTotalScore() + loser.getScore());
        looserLeagueTeam.setTotalScore(looserLeagueTeam.getTotalScore() + loser.getScore());
        loserRecord.setIsWinner(false);
        loserRecord.setPlayedAt(LocalDateTime.now());

        leagueResultRecordRepository.saveAll(List.of(winnerRecord, loserRecord));

        leaveRoom(winner.getPlayerId(), league.getId());
        leaveRoom(loser.getPlayerId(), league.getId());

//        return getAllPlayersDetails(leagueMatchProcess, leagueRoomOpt, league, winner, loser);
    }*/


/*    @Transactional
    public void processMatch(LeagueMatchProcess leagueMatchProcess) {
        Optional<LeagueRoom> leagueRoomOpt = leagueRoomRepository.findById(leagueMatchProcess.getRoomId());
        if (leagueRoomOpt.isEmpty()) {
            throw new RuntimeException("League room not found with ID: " + leagueMatchProcess.getRoomId());
        }
        LeagueRoom leagueRoom = leagueRoomOpt.get();

        Optional<League> leagueOpt = leagueRepository.findById(leagueRoom.getLeague().getId());
        if (leagueOpt.isEmpty()) {
            throw new RuntimeException("League not found with ID: " + leagueRoom.getLeague().getId());
        }
        League league = leagueOpt.get();

        // Fetch players from the room
        List<Player> roomPlayers = playerRepository.findByRoomId(leagueMatchProcess.getRoomId());
        if (roomPlayers.isEmpty()) {
            System.out.println("[WARN] No players found in room. Skipping match process.");
            return;
        }


        Set<Long> roomPlayerIds = roomPlayers.stream()
                .map(Player::getPlayerId)
                .collect(Collectors.toSet());


        List<LeaguePlayerDtoWinner> playersToProcess = leagueMatchProcess.getPlayers().stream()
                .filter(p -> p.getPlayerId() != 0 && roomPlayerIds.contains(p.getPlayerId()))
                .collect(Collectors.toList());

        playersToProcess.forEach(p -> System.out.println("PlayerId: " + p.getPlayerId() + ", Score: " + p.getScore()));

        if (playersToProcess.size() < 2) {
            System.out.println("[WARN] Not enough valid players to process match. Skipping.");
            return;
        }

        // Determine winner
        LeaguePlayerDtoWinner winner = determineWinner(playersToProcess);

        Long winnerTeamId = playerRepository.findById(winner.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + winner.getPlayerId()))
                .getTeam().getId();

        LeagueTeam winnerLeagueTeam = leagueTeamRepository.findById(winnerTeamId)
                .orElseThrow(() -> new RuntimeException("LeagueTeam not found with ID: " + winnerTeamId));

        // Identify the loser (anyone not the winner)
        LeaguePlayerDtoWinner loser = playersToProcess.stream()
                .filter(p -> !p.getPlayerId().equals(winner.getPlayerId()))
                .findFirst().orElseThrow(() -> new RuntimeException("No loser found"));

        Long loserTeamId = playerRepository.findById(loser.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + loser.getPlayerId()))
                .getTeam().getId();

        LeagueTeam loserLeagueTeam = leagueTeamRepository.findById(loserTeamId)
                .orElseThrow(() -> new RuntimeException("LeagueTeam not found with ID: " + loserTeamId));

        Player winnerPlayer = playerRepository.findById(winner.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + winner.getPlayerId()));

        Player loserPlayer = playerRepository.findById(loser.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + loser.getPlayerId()));

        // Save winner record
        LeagueResultRecord winnerRecord = new LeagueResultRecord();
        winnerRecord.setRoomId(leagueRoom.getId());
        winnerRecord.setLeague(league);
        winnerRecord.setPlayer(winnerPlayer);
        winnerRecord.setLeagueTeam(winnerLeagueTeam);
        winnerRecord.setTotalScore(winnerLeagueTeam.getTotalScore() + winner.getScore());
        winnerLeagueTeam.setTotalScore(winnerLeagueTeam.getTotalScore() + winner.getScore());
        winnerRecord.setIsWinner(true);
        winnerRecord.setPlayedAt(LocalDateTime.now());

        // Save loser record
        LeagueResultRecord loserRecord = new LeagueResultRecord();
        loserRecord.setRoomId(leagueRoom.getId());
        loserRecord.setLeague(league);
        loserRecord.setPlayer(loserPlayer);
        loserRecord.setLeagueTeam(loserLeagueTeam);
        loserRecord.setTotalScore(loserLeagueTeam.getTotalScore() + loser.getScore());
        loserLeagueTeam.setTotalScore(loserLeagueTeam.getTotalScore() + loser.getScore());
        loserRecord.setIsWinner(false);
        loserRecord.setPlayedAt(LocalDateTime.now());

        leagueResultRecordRepository.saveAll(List.of(winnerRecord, loserRecord));

        // Leave room
        leaveRoom(winner.getPlayerId(), league.getId());
        leaveRoom(loser.getPlayerId(), league.getId());
    }*/
/*
@Transactional
public void processMatch(LeagueMatchProcess leagueMatchProcess) {
    Optional<LeagueRoom> leagueRoomOpt = leagueRoomRepository.findById(leagueMatchProcess.getRoomId());
    if (leagueRoomOpt.isEmpty()) {
        throw new RuntimeException("League room not found with ID: " + leagueMatchProcess.getRoomId());
    }
    LeagueRoom leagueRoom = leagueRoomOpt.get();

    Optional<League> legueOpt = leagueRepository.findById(leagueRoom.getLeague().getId());
    if (legueOpt.isEmpty()) {
        throw new RuntimeException("League not found with ID: " + leagueRoom.getLeague().getId());
    }
    League league = legueOpt.get();

    */
/*//*
/ Fetch players from the room
    List<Player> roomPlayers = playerRepository.findByRoomId(leagueMatchProcess.getRoomId());
    if (roomPlayers.isEmpty()) {
        System.out.println("[WARN] No players found in room " + leagueMatchProcess.getRoomId());
        return;
    }

    System.out.println("[INFO] Players in the room: " + roomPlayers.stream()
            .map(p -> "PlayerId=" + p.getPlayerId())
            .collect(Collectors.joining(", ")));
*//*

    // Filter players: remove playerId = 0 and players not in room
    List<LeaguePlayerDtoWinner> validPlayers = leagueMatchProcess.getPlayers().stream()
            .filter(p -> p.getPlayerId() != 0) // exclude playerId = 0
            .filter(p -> roomPlayers.stream().anyMatch(rp -> rp.getPlayerId().equals(p.getPlayerId()))) // must be in room
            .collect(Collectors.toList());

    if (validPlayers.isEmpty()) {
        System.out.println("[WARN] No valid players found in input. Skipping match processing.");
        return;
    }

    System.out.println("[INFO] Valid players for processing: " + validPlayers.stream()
            .map(p -> "PlayerId=" + p.getPlayerId())
            .collect(Collectors.joining(", ")));

    LeaguePlayerDtoWinner winner;
    if (validPlayers.size() == 1) {
        winner = validPlayers.get(0);
        System.out.println("[INFO] Only 1 valid player. Declared PlayerId=" + winner.getPlayerId() + " as winner.");
    } else {
        winner = determineWinner(validPlayers);
        System.out.println("[INFO] Winner determined by score. PlayerId=" + winner.getPlayerId());
    }

    Player winnerPlayer = playerRepository.findById(winner.getPlayerId())
            .orElseThrow(() -> new RuntimeException("Player not found with ID: " + winner.getPlayerId()));

    Long winnerTeamId = winnerPlayer.getTeam().getId();
    LeagueTeam winnerLeagueTeam = leagueTeamRepository.findById(winnerTeamId)
            .orElseThrow(() -> new RuntimeException("LeagueTeam not found with ID: " + winnerTeamId));

    LeagueResultRecord winnerRecord = new LeagueResultRecord();
    winnerRecord.setRoomId(leagueRoom.getId());
    winnerRecord.setLeague(league);
    winnerRecord.setPlayer(winnerPlayer);
    winnerRecord.setLeagueTeam(winnerLeagueTeam);
    winnerRecord.setTotalScore(winnerRecord.getTotalScore() + winner.getScore());
    winnerLeagueTeam.setTotalScore(winnerLeagueTeam.getTotalScore() + winner.getScore());
    winnerRecord.setIsWinner(true);
    winnerRecord.setPlayedAt(LocalDateTime.now());

    leagueResultRecordRepository.save(winnerRecord);
    leaveRoom(winner.getPlayerId(), league.getId());

    // Process loser only if >1 valid players
    if (validPlayers.size() >= 2) {
        LeaguePlayerDtoWinner loser = validPlayers.stream()
                .filter(p -> !p.getPlayerId().equals(winner.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No loser found"));

        Player loserPlayer = playerRepository.findById(loser.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found with ID: " + loser.getPlayerId()));

        Long loserTeamId = loserPlayer.getTeam().getId();
        LeagueTeam loserLeagueTeam = leagueTeamRepository.findById(loserTeamId)
                .orElseThrow(() -> new RuntimeException("LeagueTeam not found with ID: " + loserTeamId));

        LeagueResultRecord loserRecord = new LeagueResultRecord();
        loserRecord.setRoomId(leagueRoom.getId());
        loserRecord.setLeague(league);
        loserRecord.setPlayer(loserPlayer);
        loserRecord.setLeagueTeam(loserLeagueTeam);
        loserRecord.setTotalScore(loserRecord.getTotalScore() + loser.getScore());
        loserLeagueTeam.setTotalScore(loserLeagueTeam.getTotalScore() + loser.getScore());
        loserRecord.setIsWinner(false);
        loserRecord.setPlayedAt(LocalDateTime.now());

        leagueResultRecordRepository.save(loserRecord);
        leaveRoom(loser.getPlayerId(), league.getId());

        System.out.println("[INFO] Loser record created for PlayerId=" + loser.getPlayerId());
    } else {
        System.out.println("[INFO] Only 1 valid player. No loser record created.");
    }
}
*/


    @Transactional
    public List<PlayerDto> processMatch(LeagueMatchProcess leagueMatchProcess) {
        Optional<LeagueRoom> leagueRoomOpt = leagueRoomRepository.findById(leagueMatchProcess.getRoomId());
        if (leagueRoomOpt.isEmpty()) {
            throw new RuntimeException("League room not found with ID: " + leagueMatchProcess.getRoomId());
        }
        LeagueRoom leagueRoom = leagueRoomOpt.get();

        Optional<League> leagueOpt = leagueRepository.findById(leagueRoom.getLeague().getId());
        if (leagueOpt.isEmpty()) {
            throw new RuntimeException("League not found with ID: " + leagueRoom.getLeague().getId());
        }
        League league = leagueOpt.get();

        System.out.println("room id....." + leagueMatchProcess.getRoomId());
        List<Player> roomPlayers = playerRepository.findByLeagueRoom_Id(leagueMatchProcess.getRoomId());
        if (roomPlayers.isEmpty()) {
            throw new RuntimeException("No players found in room " + leagueMatchProcess.getRoomId());
        }

        Set<Long> roomPlayerIds = roomPlayers.stream()
                .map(Player::getPlayerId)
                .collect(Collectors.toSet());

        List<LeaguePlayerDtoWinner> validPlayers = leagueMatchProcess.getPlayers().stream()
                .filter(p -> p.getPlayerId() != 0)
                .filter(p -> roomPlayerIds.contains(p.getPlayerId()))
                .collect(Collectors.toList());

        if (validPlayers.isEmpty()) {
            throw new BusinessException("No valid players found in input. Skipping match processing.", HttpStatus.BAD_REQUEST);
        }


        // Always determine winner from valid players
        LeaguePlayerDtoWinner winner = determineWinner(validPlayers);
        Long winnerPlayerId = winner.getPlayerId();

        Player winnerPlayer = playerRepository.findById(winnerPlayerId)
                .orElseThrow(() -> new BusinessException("Player not found with ID: " + winnerPlayerId, HttpStatus.NOT_FOUND));
        Long winnerTeamId = winnerPlayer.getTeam().getId();

        LeagueTeam winnerLeagueTeam = leagueTeamRepository.findById(winnerTeamId)
                .orElseThrow(() -> new BusinessException("LeagueTeam not found with ID: " + winnerTeamId, HttpStatus.NOT_FOUND));

        // Winner record
        LeagueResultRecord winnerRecord = new LeagueResultRecord();
        winnerRecord.setRoomId(leagueRoom.getId());
        winnerRecord.setLeague(league);
        winnerRecord.setPlayer(winnerPlayer);
        winnerRecord.setLeagueTeam(winnerLeagueTeam);
        winnerRecord.setTotalScore(winner.getScore());
        winnerLeagueTeam.setTotalScore(winnerLeagueTeam.getTotalScore() + winner.getScore());
        winnerRecord.setIsWinner(true);
        winnerRecord.setPlayedAt(LocalDateTime.now());

        List<LeagueResultRecord> resultRecords = new ArrayList<>();
        resultRecords.add(winnerRecord);

        LeaguePlayerDtoWinner loser = null;

        // Only if more than one player, process loser
        if (validPlayers.size() > 1) {
            loser = validPlayers.stream()
                    .filter(p -> !p.getPlayerId().equals(winnerPlayerId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("No loser found", HttpStatus.BAD_REQUEST));

            Long loserPlayerId = loser.getPlayerId();
            Player loserPlayer = playerRepository.findById(loserPlayerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + loserPlayerId, HttpStatus.BAD_REQUEST));
            Long loserTeamId = loserPlayer.getTeam().getId();

            LeagueTeam loserLeagueTeam = leagueTeamRepository.findById(loserTeamId)
                    .orElseThrow(() -> new BusinessException("LeagueTeam not found with ID: " + loserTeamId, HttpStatus.NOT_FOUND));

            LeagueResultRecord loserRecord = new LeagueResultRecord();
            loserRecord.setRoomId(leagueRoom.getId());
            loserRecord.setLeague(league);
            loserRecord.setPlayer(loserPlayer);
            loserRecord.setLeagueTeam(loserLeagueTeam);
            loserRecord.setTotalScore(loser.getScore());
            loserLeagueTeam.setTotalScore(loserLeagueTeam.getTotalScore() + loser.getScore());
            loserRecord.setIsWinner(false);
            loserRecord.setPlayedAt(LocalDateTime.now());

            resultRecords.add(loserRecord);
        }

        leagueResultRecordRepository.saveAll(resultRecords);

        // Remove winner
        leaveRoom(winnerPlayerId, league.getId());

        // Remove loser if exists
        if (loser != null) {
            leaveRoom(loser.getPlayerId(), league.getId());
        }

        return getAllPlayersDetails(leagueMatchProcess, leagueRoomOpt, league, winner, loser);
    }


    private LeaguePlayerDtoWinner determineWinner(List<LeaguePlayerDtoWinner> players) {
        return players.stream()
                .max(Comparator.comparingInt(LeaguePlayerDtoWinner::getScore))  // Find the player with the highest score
                .orElseThrow(() -> new BusinessException("No players found", HttpStatus.NOT_FOUND));
    }


    // Get all players' details (including amount for the winner)

    public List<PlayerDto> getAllPlayersDetails(LeagueMatchProcess gameResult, Optional<LeagueRoom> gameRoomOpt, League game, LeaguePlayerDtoWinner winner, LeaguePlayerDtoWinner loser) {

        List<PlayerDto> playersDetails = new ArrayList<>();


        for (LeaguePlayerDtoWinner player : gameResult.getPlayers()) {

            PlayerDto playerDto = new PlayerDto();

            // Set player details based on whether they are the winner or loser

            if (player.getPlayerId().equals(winner.getPlayerId())) {
                playerDto.setPlayerId(player.getPlayerId());
                playerDto.setScore(player.getScore());
                playerDto.setPictureUrl(fetchPlayerPicture(player.getPlayerId()));

            } else {
                playerDto.setPlayerId(player.getPlayerId());
                playerDto.setScore(player.getScore());
                playerDto.setPictureUrl(fetchPlayerPicture(player.getPlayerId()));

            }


            playersDetails.add(playerDto);

        }


        return playersDetails;

    }


    // Fetch player's picture URL based on player ID

    private String fetchPlayerPicture(Long playerId) {

        Optional<CustomCustomer> customCustomer = customCustomerRepository.findById(playerId);

        return customCustomer.map(CustomCustomer::getProfilePic).orElse(null); // Return profile picture URL or null if not found

    }


    // 1. Get player + team + score
    public Map<String, Object> getPlayerStats(Long leagueId, Long playerId) {
        Object[] result = leagueResultRecordRepository.getPlayerTeamAndScoreInLeague(leagueId, playerId);

        if (result == null) return null;

        Map<String, Object> map = new HashMap<>();
        map.put("teamName", result[0]);
        map.put("totalScore", result[1]);
        return map;
    }

    @Transactional
    public Map<String, Object> getTeamScoresByLeague(Long leagueId, Long playerId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Step 1: Get all teams in the league
            List<LeagueTeam> allTeams = leagueTeamRepository.findByLeagueId(leagueId);

            // Step 2: Get team scores only for teams that have score records
            List<Object[]> results = leagueResultRecordRepository.getTeamTotalScoresByLeague(leagueId);
            Map<String, Long> scoreMap = new HashMap<>();
            for (Object[] row : results) {
                String teamName = (String) row[0];
                Long totalScore = row[1] != null ? (Long) row[1] : 0L;
                scoreMap.put(teamName, totalScore);
            }

            // Step 3: Build teamScores list including default scores for teams without scores
            List<Map<String, Object>> teamScores = new ArrayList<>();
            for (LeagueTeam team : allTeams) {
                Long teamId = team.getId();
                String teamName = team.getTeamName();
                Long totalScore = scoreMap.getOrDefault(teamName, 0L);

                Map<String, Object> teamData = new HashMap<>();
                teamData.put("teamId", teamId);
                teamData.put("teamName", teamName);
                teamData.put("totalScore", totalScore);
                teamData.put("profilePic", team.getProfilePic() != null ? team.getProfilePic() :
                        "https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg");
                teamData.put("playerCount", team.getTeamPlayersCount());

                teamScores.add(teamData);
            }

            response.put("teamScores", teamScores);

            // Step 4: Player data
            if (playerId != null) {
                Integer playerScore = leagueResultRecordRepository.getPlayerTotalScoreInLeague(leagueId, playerId);
                int passCount = leaguePassRepository.getPlayerPassCountByLeague(leagueId, playerId);
                Player player = playerRepository.findById(playerId).orElse(null);
                // 👇 Call the reusable method to get player's prize
                BigDecimal playerPrize = getPlayerWinningPrize(leagueId, playerId);

                Map<String, Object> playerData = new HashMap<>();
                playerData.put("playerScore", playerScore != null ? playerScore : 0);
                playerData.put("profilePic", player != null && player.getPlayerProfilePic() != null ?
                        player.getPlayerProfilePic() :
                        "https://aag-data.s3.ap-south-1.amazonaws.com/default-data/profileImage.jpeg");
                playerData.put("pass", passCount);
                playerData.put("winningPrize", playerPrize);
                response.put("player", playerData);
            }

        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw e;
        }

        return response;
    }


    public BigDecimal getPlayerWinningPrize(Long leagueId, Long playerId) {
        try {
            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new BusinessException("League not found", HttpStatus.BAD_REQUEST));

            List<LeagueTeam> teams = leagueTeamRepository.findByLeague(league);
            if (teams.size() != 2)
                throw new BusinessException("Exactly two teams required.", HttpStatus.BAD_REQUEST);

            LeagueTeam winningTeam = teams.get(0).getTotalScore() >= teams.get(1).getTotalScore()
                    ? teams.get(0) : teams.get(1);

            /*if (!leagueResultRecordRepository.existsByLeagueIdAndLeagueTeamIdAndPlayerId(leagueId, winningTeam.getId(), playerId)) {
                return BigDecimal.ZERO;
            }
*/
            List<LeagueResultRecord> records = leagueResultRecordRepository
                    .findByLeagueIdAndLeagueTeamId(leagueId, winningTeam.getId());

            // Calculate total scores per player
            Map<Long, Integer> playerScores = new HashMap<>();
            for (LeagueResultRecord r : records) {
                Long pid = r.getPlayer().getPlayerId();
                playerScores.put(pid, playerScores.getOrDefault(pid, 0) + r.getTotalScore());
            }

            int totalTeamScore = winningTeam.getTotalScore();
            BigDecimal prizePool = Constant.LEAGUE_PRIZE_POOL;

            // Sort players by score
            List<Map.Entry<Long, Integer>> sorted = playerScores.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .collect(Collectors.toList());

            int topN = 10;
            BigDecimal distributed = BigDecimal.ZERO;
            Map<Long, BigDecimal> prizeMap = new HashMap<>();

            List<Map.Entry<Long, Integer>> top = sorted.stream().limit(topN).toList();
            List<Map.Entry<Long, Integer>> rest = sorted.stream().skip(topN).toList();

            for (Map.Entry<Long, Integer> entry : top) {
                BigDecimal percent = BigDecimal.valueOf(entry.getValue())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalTeamScore), 2, RoundingMode.HALF_UP);

                BigDecimal prize = prizePool.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                prizeMap.put(entry.getKey(), prize);
                distributed = distributed.add(prize);
            }

            if (!rest.isEmpty()) {
                BigDecimal remaining = prizePool.subtract(distributed).setScale(2, RoundingMode.HALF_UP);
                BigDecimal equalShare = remaining.divide(BigDecimal.valueOf(rest.size()), 2, RoundingMode.HALF_UP);
                for (Map.Entry<Long, Integer> entry : rest) {
                    prizeMap.put(entry.getKey(), equalShare);
                }
            }

            return prizeMap.getOrDefault(playerId, BigDecimal.ZERO);

        } catch (Exception e) {
            exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return BigDecimal.ZERO;
        }
    }


    public ResponseEntity<?> getLeagueTeamDetails(Long leagueId, Long currentUserId, int page, int size){
        try {
            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new BusinessException("League not found", HttpStatus.BAD_REQUEST));

            List<LeagueTeam> teams = leagueTeamRepository.findByLeague(league);
            if (teams.size() != 2) throw new BusinessException("Exactly two teams required.", HttpStatus.BAD_REQUEST);

            LeagueTeam winner = teams.get(0).getTotalScore() >= teams.get(1).getTotalScore() ? teams.get(0) : teams.get(1);

            List<TeamDetailsDTO> teamDetails = new ArrayList<>();

            for (LeagueTeam team : teams) {
                List<LeagueResultRecord> records = leagueResultRecordRepository.findByLeagueIdAndLeagueTeamId(league.getId(), team.getId());

                Map<Long, List<LeagueResultRecord>> groupedByPlayer = records.stream()
                        .collect(Collectors.groupingBy(r -> r.getPlayer().getPlayerId()));

                List<PlayerLeagueScoreDTO> allPlayers = groupedByPlayer.values().stream().map(playerRecords -> {
                            LeagueResultRecord firstRecord = playerRecords.get(0);
                            Player player = firstRecord.getPlayer();
                            int totalScore = playerRecords.stream().mapToInt(LeagueResultRecord::getTotalScore).sum();
                            boolean isCurrentUser = player.getCustomer().getId().equals(currentUserId);

                            ResponseEntity<?> response = getLeaguePasses(player.getPlayerId(), leagueId);
                            SuccessResponse successResponse = (SuccessResponse) response.getBody();
                            Map<String, Object> data = (Map<String, Object>) successResponse.getData();
                            int passCount = (int) data.get("passCount");

                            return new PlayerLeagueScoreDTO(
                                    player.getPlayerName(),
                                    player.getPlayerProfilePic(),
                                    isCurrentUser,
                                    totalScore,
                                    passCount,
                                    team.equals(winner)
                            );
                        }).sorted(Comparator.comparingInt(PlayerLeagueScoreDTO::getScore).reversed()) // sort descending
                        .collect(Collectors.toList());

                // Pagination logic
                int start = Math.min(page * size, allPlayers.size());
                int end = Math.min(start + size, allPlayers.size());
                List<PlayerLeagueScoreDTO> paginatedPlayers = allPlayers.subList(start, end);

                teamDetails.add(new TeamDetailsDTO(
                        team.getTeamName(),
                        team.getProfilePic(),
                        team.getTotalScore(),
                        paginatedPlayers
                ));
            }



            LeagueTeamDetailsResponse leagueTeamDetailsResponse = new LeagueTeamDetailsResponse(
                    league.getName(),
                    winner.getTeamName(),
                    teamDetails
            );

            return responseService.generateSuccessResponse("League team details fetched successfully.", leagueTeamDetailsResponse, HttpStatus.OK);
        } catch (BusinessException e) {
            exceptionHandling.handleException(e.getStatus(), e);
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Failed to fetch league team details because " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }


    @Transactional
    public ResponseEntity<?> distributePrizePool(Long leagueId) {
        try {
            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new BusinessException("League not found", HttpStatus.BAD_REQUEST));

            List<LeagueTeam> teams = leagueTeamRepository.findByLeague(league);
            if (teams.size() != 2)
                throw new BusinessException("Exactly two teams required.", HttpStatus.BAD_REQUEST);

            // Determine winning team
            LeagueTeam winner = teams.get(0).getTotalScore() >= teams.get(1).getTotalScore()
                    ? teams.get(0) : teams.get(1);

            // Fetch all result records for winning team
            List<LeagueResultRecord> records = leagueResultRecordRepository.findByLeagueIdAndLeagueTeamId(
                    league.getId(), winner.getId());

            // Group records by player and calculate total scores
            Map<Long, List<LeagueResultRecord>> groupedByPlayer = records.stream()
                    .collect(Collectors.groupingBy(r -> r.getPlayer().getPlayerId()));

            List<PlayerLeagueScoreDDTO> players = groupedByPlayer.values().stream().map(playerRecords -> {
                LeagueResultRecord firstRecord = playerRecords.get(0);
                Player player = firstRecord.getPlayer();
                int totalScore = playerRecords.stream().mapToInt(LeagueResultRecord::getTotalScore).sum();

                return new PlayerLeagueScoreDDTO(
                        player.getPlayerName(),
                        player.getPlayerProfilePic(),
                        false,
                        totalScore,
                        2,
                        true,
                        null
                );
            }).collect(Collectors.toList());

            int totalScore = winner.getTotalScore();
            BigDecimal prizePool = Constant.LEAGUE_PRIZE_POOL;

            List<PlayerPrizeDTO> prizeDistribution = calculatePrizeDistribution(players, totalScore, prizePool);

            return responseService.generateSuccessResponse(
                    "Prize pool distributed successfully.",
                    prizeDistribution,
                    HttpStatus.OK
            );

        } catch (BusinessException e) {
            exceptionHandling.handleException(e.getStatus(), e);
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Failed to distribute prize: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public List<PlayerPrizeDTO> calculatePrizeDistribution(List<PlayerLeagueScoreDDTO> players, int totalTeamScore, BigDecimal totalPrizePool) {
        try {
            players.sort(Comparator.comparingInt(PlayerLeagueScoreDDTO::getScore).reversed());

            int topN = 10;
            int playerCount = players.size();

            List<PlayerPrizeDTO> prizeDistribution = new ArrayList<>();
            BigDecimal distributedPrize = BigDecimal.ZERO;

            for (int i = 0; i < Math.min(topN, playerCount); i++) {
                PlayerLeagueScoreDDTO player = players.get(i);

                BigDecimal contributionPercentage = BigDecimal.valueOf(player.getScore())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalTeamScore), 2, RoundingMode.HALF_UP);

                BigDecimal prizeAmount = totalPrizePool
                        .multiply(contributionPercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                distributedPrize = distributedPrize.add(prizeAmount);

                prizeDistribution.add(new PlayerPrizeDTO(
                        player.getName(),
                        player.getScore(),
                        contributionPercentage.doubleValue(),
                        prizeAmount
                ));
            }

            BigDecimal remainingPrize = totalPrizePool.subtract(distributedPrize).setScale(2, RoundingMode.HALF_UP);

            if (playerCount > topN) {
                int remainingPlayersCount = playerCount - topN;
                BigDecimal equalShare = remainingPrize
                        .divide(BigDecimal.valueOf(remainingPlayersCount), 2, RoundingMode.HALF_UP);

                for (int i = topN; i < playerCount; i++) {
                    PlayerLeagueScoreDDTO player = players.get(i);
                    prizeDistribution.add(new PlayerPrizeDTO(
                            player.getName(),
                            player.getScore(),
                            0.0,
                            equalShare
                    ));
                }
            }

            return prizeDistribution;

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return new ArrayList<>();
        }
    }



    @Transactional
    public void distributePrizePoolSilently(Long leagueId) {
        try {
            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new BusinessException("League not found", HttpStatus.BAD_REQUEST));

            List<LeagueTeam> teams = leagueTeamRepository.findByLeague(league);
            if (teams.size() != 2) {
                throw new BusinessException("Exactly two teams required.", HttpStatus.BAD_REQUEST);
            }

            LeagueTeam winner = teams.get(0).getTotalScore() >= teams.get(1).getTotalScore()
                    ? teams.get(0) : teams.get(1);

            List<LeagueResultRecord> records = leagueResultRecordRepository
                    .findByLeagueIdAndLeagueTeamId(league.getId(), winner.getId());

            // Aggregate scores per player
            Map<Player, Integer> playerScores = new HashMap<>();
            for (LeagueResultRecord record : records) {
                Player player = record.getPlayer();
                playerScores.put(player, playerScores.getOrDefault(player, 0) + record.getTotalScore());
            }

            int totalTeamScore = winner.getTotalScore();
            BigDecimal totalPrizePool = Constant.LEAGUE_PRIZE_POOL;
            BigDecimal distributedPrize = BigDecimal.ZERO;

            // Sort players by score descending
            List<Map.Entry<Player, Integer>> sortedPlayers = playerScores.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .collect(Collectors.toList());

            int topN = 10;
            List<Map.Entry<Player, Integer>> topPlayers = sortedPlayers.stream().limit(topN).toList();
            List<Map.Entry<Player, Integer>> remainingPlayers = sortedPlayers.stream().skip(topN).toList();

            // Top N contribution-based prize
            for (Map.Entry<Player, Integer> entry : topPlayers) {
                Player player = entry.getKey();
                int score = entry.getValue();

                BigDecimal contributionPercent = BigDecimal.valueOf(score)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalTeamScore), 2, RoundingMode.HALF_UP);

                BigDecimal prize = totalPrizePool
                        .multiply(contributionPercent)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                distributedPrize = distributedPrize.add(prize);

                Wallet wallet = player.getCustomer().getWallet();
                wallet.setWinningAmount(wallet.getWinningAmount().add(prize));
                walletRepo.save(wallet);
                Notification notification = new Notification();
                notification.setCustomerId(player.getPlayerId());
                notification.setDescription("Wallet balance credited");
                notification.setAmount(prize.doubleValue());
                notification.setDetails("Rs. " + prize.doubleValue() + " won in " + league.getName());
                notification.setRole("Customer");
                notificationRepository.save(notification);
            }

            // Remaining prize equal distribution
            BigDecimal remainingPrize = totalPrizePool.subtract(distributedPrize).setScale(2, RoundingMode.HALF_UP);

            if (!remainingPlayers.isEmpty() && remainingPrize.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal equalShare = remainingPrize
                        .divide(BigDecimal.valueOf(remainingPlayers.size()), 2, RoundingMode.HALF_UP);

                for (Map.Entry<Player, Integer> entry : remainingPlayers) {
                    Player player = entry.getKey();
                    Wallet wallet = player.getCustomer().getWallet();
                    if (wallet != null) {
                        wallet.setWinningAmount(wallet.getWinningAmount().add(equalShare));
                        walletRepo.save(wallet);
                    }

                    Notification notification = new Notification();
                    notification.setCustomerId(player.getPlayerId());
                    notification.setDescription("Wallet balance credited");
                    notification.setAmount(equalShare.doubleValue());
                    notification.setDetails("Rs. " + equalShare.doubleValue() + " won in " + league.getName());
                    notification.setRole("Customer");
                    notificationRepository.save(notification);
                }
            }

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }


}