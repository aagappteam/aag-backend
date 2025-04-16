package aagapp_backend.services.tournamnetservice;

import aagapp_backend.components.Constant;
import aagapp_backend.dto.JoinLeagueRequest;
import aagapp_backend.dto.TournamentRequest;
import aagapp_backend.entity.Challenge;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.tournament.TournamentRoom;
import aagapp_backend.entity.tournament.TournamentRoundWinner;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.enums.PlayerStatus;
import aagapp_backend.enums.TournamentStatus;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.repository.tournament.TournamentRoundWinnerRepository;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.naming.LimitExceededException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private AagGameRepository aagGameRepository;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private TournamentRoomRepository roomRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TournamentRoundWinnerRepository tournamentRoundWinnerRepository;
    private final String baseUrl = "http://13.232.105.87:8082";


    @Transactional
    public Tournament publishTournament(TournamentRequest tournamentRequest, Long vendorId) throws LimitExceededException {
        try {

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            // Create a new Game entity
            Tournament tournament = new Tournament();

            Optional<AagAvailableGames> gameAvailable = aagGameRepository.findById(tournamentRequest.getExistinggameId());
            tournament.setGameUrl(gameAvailable.get().getGameImage());

            ThemeEntity theme = em.find(ThemeEntity.class, tournamentRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }

            // Set Vendor and Theme to the Game
            tournament.setName(gameAvailable.get().getGameName());
            tournament.setVendorId(vendorId);
            tournament.setTheme(theme);
            tournament.setExistinggameId(tournamentRequest.getExistinggameId());
            tournament.setTotalPrizePool(tournamentRequest.getTotalPrizePool());
            tournament.setParticipants(tournamentRequest.getParticipants());
            // Calculate moves based on the selected fee
            tournament.setEntryFee(tournamentRequest.getEntryFee());

            if (tournamentRequest.getEntryFee() > 10) {
                tournament.setMove(Constant.TENMOVES);
            } else {
                tournament.setMove(Constant.SIXTEENMOVES);

            }


            // Get current time in Kolkata timezone
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            if (tournamentRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledInKolkata = tournamentRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                if (scheduledInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The game must be scheduled at least 4 hours in advance.");
                }
                tournament.setStatus(TournamentStatus.SCHEDULED);
                tournament.setScheduledAt(scheduledInKolkata);

            } else {
                tournament.setStatus(TournamentStatus.SCHEDULED);
                tournament.setScheduledAt(nowInKolkata.plusMinutes(15));
            }

            /*// Set the minimum and maximum players
            if (tournamentRequest.getMinPlayersPerTeam() != null) {
                league.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            }
            if (leagueRequest.getMaxPlayersPerTeam() != null) {
                league.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());
            }*/
            vendorEntity.setPublishedLimit((vendorEntity.getPublishedLimit() == null ? 0 : vendorEntity.getPublishedLimit()) + 1);

            // Set created and updated timestamps
            tournament.setCreatedDate(nowInKolkata);

            // Save tournament first to get its ID
            Tournament savedTournament = tournamentRepository.save(tournament);

            // Create initial rooms
            int numberOfRooms = (tournamentRequest.getParticipants() / 2);
            for (int i = 0; i < numberOfRooms; i++) {
                TournamentRoom room = new TournamentRoom();
                room.setTournamentId(savedTournament.getId());
                room.setMaxParticipants(2);
                room.setCurrentParticipants(0);
                room.setStatus("OPEN");
                room.setRound(1);
                roomRepository.save(room);

                Double total_prize = 3.2;
                String gamePassword = this.createNewGame(baseUrl, tournament.getId(), room.getId(), room.getMaxParticipants(), tournament.getMove(), total_prize);

                room.setGamepassword(gamePassword);
            }

            // Save the game to get the game ID
//            Tournament savedTournament = tournamentRepository.save(tournament);

            // Create the first GameRoom (initialized, 2 players max)
//            LeagueRoom leagueRoom = createNewEmptyRoom(savedLeague);

//            leagueRequest.setChallengeStatus(Challenge.ChallengeStatus.ACCEPTED);

            // Save the game room
//            leagueRoomRepository.save(leagueRoom);

            // Generate a shareable link for the game
            String shareableLink = generateShareableLink(tournament.getId());
            tournament.setShareableLink(shareableLink);

            // Return the saved game with the shareable link
            return tournamentRepository.save(tournament);

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the game: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Page<Tournament> getAllTournaments(Pageable pageable, TournamentStatus status, Long vendorId) {
        try {
            // Check if 'status' and 'vendorId' are provided and build a query accordingly
            if (status != null && vendorId != null) {
                return tournamentRepository.findTournamentByStatusAndVendorId(status, vendorId, pageable);
            } else if (status != null) {
                return tournamentRepository.findByStatus(status, pageable);
            } else if (vendorId != null) {
                return tournamentRepository.findByVendorId(vendorId, pageable);
            } else {
                return tournamentRepository.findAll(pageable);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Page<Tournament> getAllActiveTournamentsByVendor(Pageable pageable, Long vendorId) {
        try {
            // Check if 'status' and 'vendorId' are provided and build a query accordingly
            TournamentStatus status = TournamentStatus.ACTIVE;
            if (status != null && vendorId != null) {
                return tournamentRepository.findTournamentByStatusAndVendorId(status, vendorId, pageable);
            } else {
                return tournamentRepository.findAll(pageable);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<TournamentRoom> getAllRoomsByTournamentId(Long tournamentId) {
        try {
            return roomRepository.findByTournamentId(tournamentId);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching rooms: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Page<Tournament> findTournamentsByVendor(Pageable pageable, Long vendorId) {
        try {
            if (vendorId != null) {
                return tournamentRepository.findByVendorId(vendorId, pageable);
            } else {
                return tournamentRepository.findAll(pageable);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }

    public boolean isGameAvailableById(Long gameId) {
        // Use the repository to find a game by its name
        Optional<AagAvailableGames> game = aagGameRepository.findById(gameId);
        return game.isPresent(); // Return true if the game is found, false otherwise
    }

    private String generateShareableLink(Long gameId) {
        return "https://example.com/tournament/" + gameId;
    }


    @Transactional
    public void startTournament(Long tournamentId) {
        List<TournamentRoom> rooms = roomRepository.findByTournamentIdAndStatus(tournamentId, "OPEN");

        // Remove empty rooms before starting the tournament
        rooms.removeIf(room -> {
            if (room.getCurrentParticipants() == 0) {
                roomRepository.delete(room);  // Delete empty room
                System.out.println("❌ Room " + room.getId() + " deleted as it has no players.");
                return true;
            }
            return false;
        });

        // No match playing logic, just preparing for rounds
        System.out.println("🏆 Tournament Started with " + rooms.size() + " rooms.");

    }


    @Transactional
    public TournamentRoom assignPlayerToRoom(Long playerId, Long tournamentId) {
        try {
            List<TournamentRoom> openRooms = roomRepository.findByTournamentIdAndStatus(tournamentId, "OPEN");
            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (openRooms.isEmpty()) {
                throw new RuntimeException("No open rooms available for tournament " + tournamentId);
            }

            Player player = playerRepository.findById(playerId).orElse(null);
            if (player == null) {
                throw new RuntimeException("Player not found with id " + playerId);
            }

            for (TournamentRoom room : openRooms) {

                if (room.getCurrentParticipants() < room.getMaxParticipants()) {
                    if (player.getTournamentRoom() != null && player.getTournamentRoom().getId().equals(room.getId())) {
                        throw new RuntimeException("Player is already in the requested room.");
                    }

                    if (player.getTournamentRoom() != null) {
                        throw new RuntimeException("Player already in another room.");
                    }

                    if (room.getRound() == 1) {
                        tournament.setCurrentJoinedPlayers(tournament.getCurrentJoinedPlayers() + 1);
                    }

                    player.setTournamentRoom(room);
                    room.getCurrentPlayers().add(player);
                    room.setCurrentParticipants(room.getCurrentParticipants() + 1);
                    roomRepository.save(room);
                    return room;
                } else {
                    throw new RuntimeException("Tournament Rooms are full. Cannot add more players");
                }
            }

            throw new RuntimeException("All rooms are full for tournament " + tournamentId);

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while assigning the player to the room.", e);
        }
    }


    public TournamentRoundWinner addPlayerToRound(Long tournamentId, Long playerId, Integer roundNumber) {
        try {
            // Check if the player exists in the database
            Player player = playerRepository.findById(playerId).orElse(null);
            if (player == null) {
                throw new RuntimeException("Player not found with id " + playerId);
            }

            // Check if the player is already in the round for the same tournament
            Optional<TournamentRoundWinner> existingWinner = tournamentRoundWinnerRepository
                    .findByTournamentIdAndPlayerIdAndRoundNumber(tournamentId, playerId, roundNumber);

            if (existingWinner.isPresent()) {
                // If the player is already added to the round, throw an exception
                throw new RuntimeException("Player with ID " + playerId + " is already added to the tournament " + tournamentId
                        + " for round " + roundNumber);
            }

            // Set player's tournament room to null (if any)
            player.setTournamentRoom(null);
            playerRepository.save(player);

            // Create and save the TournamentRoundWinner
            TournamentRoundWinner tournamentRoundWinner = new TournamentRoundWinner();
            tournamentRoundWinner.setTournamentId(tournamentId);
            tournamentRoundWinner.setPlayerId(playerId);
            tournamentRoundWinner.setRoundNumber(roundNumber);

            // Save the new round winner and return it
            return tournamentRoundWinnerRepository.save(tournamentRoundWinner);

            // TODO: Add rewards logic if needed

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while adding the player to the round.", e);
        }
    }

    public List<TournamentRoundWinner> getPlayersByTournamentAndRound(Long tournamentId, Integer roundNumber) {
        try {
            // Retrieve the list of players by tournamentId and roundNumber
            return tournamentRoundWinnerRepository.findByTournamentIdAndRoundNumber(tournamentId, roundNumber);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while retrieving players for tournament " + tournamentId + " and round " + roundNumber, e);
        }
    }


    public List<TournamentRoom> createRoomsForPlayersNextRoundAndAssign(Long tournamentId, Integer roundNumber) {
        try {
            List<TournamentRoom> createdRooms = new ArrayList<>();
            // Calculate the number of rooms needed (each room holds 2 players)
            int roomSize = 2;

            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            // Get the list of winners (players) in this tournament and round
            List<TournamentRoundWinner> winners = tournamentRoundWinnerRepository.findByTournamentIdAndRoundNumber(tournamentId, roundNumber);

            if (winners.isEmpty()) {
                throw new RuntimeException("No winners found for tournament " + tournamentId + " and round " + roundNumber);
            }

            // Create rooms for players in pairs
            for (int i = 0; i < winners.size(); i += roomSize) {
                TournamentRoom room = new TournamentRoom();
                room.setTournamentId(tournamentId);
                room.setRound(roundNumber);
                room.setMaxParticipants(roomSize);
                room.setCurrentParticipants(0);
                room.setStatus("OPEN");
                roomRepository.save(room);
                createdRooms.add(room);
                Double total_prize = 3.2;
                String gamePassword = this.createNewGame(baseUrl, tournament.getId(), room.getId(), room.getMaxParticipants(), tournament.getMove(), total_prize);

                room.setGamepassword(gamePassword);
            }

            // Assign players to the created rooms
            for (TournamentRoundWinner winner : winners) {
                assignPlayerToRoom(winner.getPlayerId(), tournamentId);
//                deleteTournamentRoundWinner(tournamentId, winner.getPlayerId());
            }
            return createdRooms;  // Return the list of created rooms

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while creating rooms and assigning players for tournament " + tournamentId + " and round " + roundNumber, e);
        }
    }

    public void deleteTournamentRoundWinner(Long tournamentId, Long playerId) {
        // Call the custom delete method
        tournamentRoundWinnerRepository.deleteByTournamentIdAndPlayerId(tournamentId, playerId);
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
}
