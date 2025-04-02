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
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.enums.PlayerStatus;
import aagapp_backend.enums.TournamentStatus;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    private final Map<Long, Integer> playerRewards = new HashMap<>();



    @Transactional
    public Tournament publishTournament(TournamentRequest tournamentRequest, Long vendorId) throws LimitExceededException {
        try {

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            // Create a new Game entity
            Tournament tournament = new Tournament();

            boolean isAvailable = isGameAvailableById(tournamentRequest.getExistinggameId());

            if (!isAvailable) {
                throw new RuntimeException("game is not available");
            }
            Optional<AagAvailableGames> gameAvailable = aagGameRepository.findById(tournamentRequest.getExistinggameId());
            tournament.setGameUrl(gameAvailable.get().getGameImage());

            ThemeEntity theme = em.find(ThemeEntity.class, tournamentRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }

            // Set Vendor and Theme to the Game
            tournament.setName(tournamentRequest.getName());
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
            tournament.setCreatedAt(nowInKolkata);

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
        int round = 1;
        List<Player> winners = new ArrayList<>();

        while (rooms.size() > 1) {
            for (TournamentRoom room : rooms) {
                if (room.getCurrentParticipants() == room.getMaxParticipants() && room.getCurrentPlayers().size() >= 2) {
                    Player winner = playMatch(room.getCurrentPlayers().get(0), room.getCurrentPlayers().get(1));
                    winners.add(winner);
                    room.setStatus("COMPLETED");
                    room.setRound(round);
                    roomRepository.save(room);
                } else {
                    System.out.println("⚠️ Room " + room.getId() + " does not have enough players.");
                }
            }
            rooms = createNextRoundRooms(winners, round + 1);
            round++;
        }

        if (!winners.isEmpty()) {
            Player finalWinner = winners.get(0);
            assignReward(finalWinner);
            System.out.println("🎉 Final Winner: " + finalWinner.getName());
        } else {
            System.out.println("⚠️ No winners found. Tournament may have incomplete rounds.");
        }
    }

    private Player playMatch(Player p1, Player p2) {
        Player winner = new Random().nextBoolean() ? p1 : p2;
        assignReward(winner);
        return winner;
    }

    private List<TournamentRoom> createNextRoundRooms(List<Player> winners, int round) {
        List<TournamentRoom> newRooms = new ArrayList<>();
        int roomSize = 2;

        for (int i = 0; i < winners.size(); i += roomSize) {
            TournamentRoom room = new TournamentRoom();
            room.setTournamentId(winners.get(i).getTournamentRoom().getTournamentId());
            room.setMaxParticipants(roomSize);
            room.setCurrentParticipants(0);
            room.setStatus("OPEN");
            room.setRound(round);
            roomRepository.save(room);
            newRooms.add(room);
        }

        return newRooms;
    }

    private void assignReward(Player winner) {
        int rewardAmount = calculateReward(winner);
        playerRewards.put(winner.getPlayerId(), playerRewards.getOrDefault(winner.getPlayerId(), 0) + rewardAmount);
        System.out.println("🎁 Reward of " + rewardAmount + " points credited to: " + winner.getName());
    }

    private int calculateReward(Player winner) {
        int baseReward = 100;
        int bonus = (winner.getTournamentRoom() != null && winner.getTournamentRoom().getRound() == 3) ? 50 : 0;
        return baseReward + bonus;
    }

    public void assignPlayerToRoom(JoinLeagueRequest playerId, Long tournamentId) {
        List<TournamentRoom> openRooms = roomRepository.findByTournamentIdAndStatus(tournamentId, "OPEN");
        //find players By id by repository
        Player player = playerRepository.findById(playerId.getPlayerId()).orElse(null);

        if (player == null) {
            System.out.println("���️ Player not found with id " + playerId.getPlayerId());
            return;
        }

        System.out.println("Player with id " + player);

        if (openRooms.isEmpty()) {
            System.out.println("⚠️ No open rooms available for player " + player.getName());
            return;
        }

        for (TournamentRoom room : openRooms) {
            if (room.getCurrentParticipants() < room.getMaxParticipants()) {
                player.setTournamentRoom(room);
                room.getCurrentPlayers().add(player);
                room.setCurrentParticipants(room.getCurrentParticipants() + 1);
                roomRepository.save(room);
                break;
            }
        }
    }


}
