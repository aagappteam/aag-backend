package aagapp_backend.services.league;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.LeagueRequest;

import aagapp_backend.dto.NotificationRequest;
import aagapp_backend.entity.Challenge;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.players.Player;

import aagapp_backend.enums.LeagueRoomStatus;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.enums.PlayerStatus;
import aagapp_backend.repository.ChallangeRepository;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.league.LeagueRoomRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.firebase.NotoficationFirebase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firestore.v1.TransactionOptions;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class LeagueService {
    @Autowired
    private NotoficationFirebase notificationFirebase;


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
    private AagGameRepository aagGameRepository;


    @Autowired
    private ChallangeRepository challangeRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ResponseService responseService;
    private final String baseUrl = "http://13.232.105.87:8082";

    @Transactional
    public Challenge createChallenge(LeagueRequest leagueRequest, Long vendorId) {
        try {

            AagAvailableGames game = aagGameRepository.findById(leagueRequest.getExistinggameId()).orElse(null);

            if (game == null) {
                throw new RuntimeException("Game not found with ID: " + leagueRequest.getExistinggameId());
            }
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
                Long oppnentvendorid = getRandomAvailableVendor(vendorId).getService_provider_id();
                challenge.setOpponentVendorId(oppnentvendorid);
            }else{
                Long oppnentvendorid = leagueRequest.getOpponentVendorId();
                challenge.setOpponentVendorId(oppnentvendorid);
            }



            // Create the challenge entity
            challenge.setVendorId(vendorId);
            challenge.setExistinggameId(leagueRequest.getExistinggameId());
            challenge.setChallengeStatus(Challenge.ChallengeStatus.PENDING); // Initially set the status to PENDING
            challenge.setFee(leagueRequest.getFee());
            if(leagueRequest.getFee()>10){
                challenge.setMove(Constant.TENMOVES);
            } else{
                challenge.setMove(Constant.SIXTEENMOVES);

            }

            challenge.setName(game.getGameName());

//            challenge.setFee(leagueRequest.getFee());
            challenge.setThemeId(leagueRequest.getThemeId());
            challenge.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            challenge.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());
            challenge.setScheduledAt(leagueRequest.getScheduledAt());
            challenge.setEndDate(leagueRequest.getEndDate());

            // Save the challenge in the database
            challangeRepository.save(challenge);

            Long receiverVendorId = challenge.getOpponentVendorId();

            VendorEntity opponentVendor = vendorRepository.findById(receiverVendorId)
                    .orElseThrow(() -> new RuntimeException("Opponent Vendor not found"));
            String fcmToken = opponentVendor.getFcmToken(); // or whatever field name is used

            if (fcmToken != null && !fcmToken.isEmpty()) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String challengeJson = gson.toJson(challenge); // Convert Challenge to JSON string

                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setToken(fcmToken);
                notificationRequest.setTitle("New Challenge Received!");
                notificationRequest.setBody(challengeJson);
                notificationRequest.setTopic("League Challenge"); // Optional, just for tagging

                try {
                    notificationFirebase.sendMessageToToken(notificationRequest);
                } catch (Exception e) {
                    System.out.println("Error sending notification: " + e.getMessage());
                }
            }


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

    @Transactional
    public Challenge getChallengeById(Long challengeId) {
        return challangeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found with ID: " + challengeId));
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
            throw new RuntimeException("No challenges found for opponentVendorIds: " + opponentVendorId +
                    (status != null ? " with status: " + status : ""));
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
            throw new RuntimeException("No challenges found for vendorIds: " + vendorIds +
                    (status != null ? " with status: " + status : ""));
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
                throw new RuntimeException("game is not available");
            }
            Optional<AagAvailableGames> gameAvailable = aagGameRepository.findById(leagueRequest.getExistinggameId());
            league.setLeagueUrl(gameAvailable.get().getGameImage());
            league.setGameName(gameAvailable.get().getGameName());

            ThemeEntity theme = em.find(ThemeEntity.class, leagueRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }

            // Set Vendor and Theme to the Game
            league.setName(opponentVendor.getFirst_name() +" v/s " +vendorEntity.getFirst_name());
            league.setVendorEntity(opponentVendor);
            league.setChallengingVendorId(opponentVendor.getService_provider_id());
            league.setChallengingVendorName(vendorEntity.getFirst_name()+" "+vendorEntity.getLast_name());
            league.setChallengingVendorProfilePic(vendorEntity.getProfilePic());
            league.setOpponentVendorName(opponentVendor.getFirst_name()+" "+opponentVendor.getLast_name());
            league.setOpponentVendorProfilePic(opponentVendor.getProfilePic());
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
            vendorEntity.setPublishedLimit((vendorEntity.getPublishedLimit() == null ? 0 : vendorEntity.getPublishedLimit()) + 1);
            opponentVendor.setPublishedLimit((opponentVendor.getPublishedLimit() == null ? 0 : opponentVendor.getPublishedLimit()) + 1);
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

        // Save the room first so it gets an ID
        newRoom = leagueRoomRepository.save(newRoom);

        Double total_prize = 3.2;
        String gamePassword = this.createNewGame(baseUrl, league.getId(), newRoom.getId(), newRoom.getMaxPlayers(), league.getMove(), total_prize);

        newRoom.setGamepassword(gamePassword);

        return newRoom;
    }

    public String createNewGame(String baseUrl, Long gameId, Long roomId, Integer players, Integer move, Double prize) {
        try {
            // Construct the URL for the POST request, including query parameters
            String url = baseUrl + "/CreateNewGame?gameid=" + gameId + "&roomid=" + roomId + "&players=" + players + "&prize=" + prize + "&moves=" + move;

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
            }  else {
                return leagueRepository.findAll(pageable);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }

    public Page<League> getAllActiveLeaguesByVendor(Pageable pageable, Long vendorId) {
        try {
            // Check if 'status' and 'vendorId' are provided and build a query accordingly
            LeagueStatus status= LeagueStatus.ACTIVE;
            if (status != null && vendorId != null) {
                return leagueRepository.findLeaguesByStatusAndVendorId(status, vendorId, pageable);
            }else {
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
    public ResponseEntity<?> joinRoom(Long playerId, Long leagueId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId));


            League game = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));


            if (player.getLeagueRoom() != null) {
                return responseService.generateErrorResponse("Player already in room with this id: " + player.getPlayerId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            LeagueRoom leagueRoom = findAvailableGameRoom(game);

            // 4. Attempt to add the player to the room
            boolean playerJoined = addPlayerToRoom(leagueRoom, player);
            if (!playerJoined) {
                return responseService.generateErrorResponse("Room is Already full with this id: " + leagueRoom.getRoomCode(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 5. If the room is full, change status to ONGOING and create a new room
            if (leagueRoom.getCurrentPlayers().size() == leagueRoom.getMaxPlayers()) {
                leagueRoom.setStatus(LeagueRoomStatus.ONGOING);
                leagueRoomRepository.save(leagueRoom);
                LeagueRoom newRoom = createNewEmptyRoom(game);
                leagueRoomRepository.save(newRoom); // Save the new room
            }

            player.setPlayerStatus(PlayerStatus.PLAYING);
            playerRepository.save(player);

            return responseService.generateSuccessResponse("Player join in the Game Room ", leagueRoom, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player can not joined in the room because " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<?> leaveRoom(Long playerId, Long leagueId) {
        try{
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player not found with ID: " + playerId));

            League league = leagueRepository.findById(leagueId)
                    .orElseThrow(() -> new RuntimeException("League not found with ID: " + leagueId));

            LeagueRoom leagueRoom = findAvailableGameRoom(league);

            if (player.getLeagueRoom() == null) {
                return responseService.generateErrorResponse("Player is not in league with this id: " + player.getPlayerId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            player.setLeagueRoom(null);
            player.setPlayerStatus(PlayerStatus.READY_TO_PLAY);
            player.setLeagueRoom(null);
            playerRepository.save(player);

            return responseService.generateSuccessResponse("Player left the Game Room ", leagueRoom.getRoomCode(), HttpStatus.OK);
        } catch (Exception e){
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player can not left the room because " + e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    public LeagueRoom findAvailableGameRoom(League league) {
        // Find a game room that has available space and matches the specified game
        List<LeagueRoom> availableRooms = leagueRoomRepository.findByGameAndStatus(league, LeagueRoomStatus.INITIALIZED);
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


    public boolean addPlayerToRoom(LeagueRoom leagueRoom, Player player) {
        // Check if the game room has space for the player
        if (leagueRoom.getCurrentPlayers().size() < leagueRoom.getMaxPlayers()) {
            leagueRoom.getCurrentPlayers().add(player);

            player.setLeagueRoom(leagueRoom);

            player.setPlayerStatus(PlayerStatus.PLAYING);

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


}