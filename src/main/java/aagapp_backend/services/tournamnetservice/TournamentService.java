package aagapp_backend.services.tournamnetservice;

import aagapp_backend.components.Constant;
import aagapp_backend.components.pricelogic.PriceConstant;
import aagapp_backend.dto.*;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.*;
import aagapp_backend.entity.wallet.VendorWallet;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.TournamentStatus;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.tournament.*;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.firebase.NotoficationFirebase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.naming.LimitExceededException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private TournamentRepository tournamentRepository;
    private TournamentRoundParticipantRepository tournamentRoundParticipantRepository;
    private EntityManager em;
    private NotificationRepository notificationRepository;
    private AagGameRepository aagGameRepository;
    private TaskScheduler taskScheduler;
    private NotoficationFirebase notoficationFirebase;
    private ExceptionHandlingImplement exceptionHandling;
    private TournamentRoomRepository roomRepository;
    private PlayerRepository playerRepository;
    private VendorRepository vendorRepository;
    private WalletRepository walletRepo;
    private ResponseService responseService;
    private CustomCustomerRepository customCustomerRepository;
    private TournamentResultRecordRepository tournamentResultRecordRepository;
    private TournamentRoomWinnerRepository tournamentRoomWinnerRepository;
    private TournamentPlayerRegistrationRepository tournamentPlayerRegistrationRepository;
    @Autowired
    public void setTournamentRepository(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Autowired
    public void setTournamentRoundParticipantRepository(TournamentRoundParticipantRepository tournamentRoundParticipantRepository) {
        this.tournamentRoundParticipantRepository = tournamentRoundParticipantRepository;
    }

    @Autowired
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Autowired
    public void setAagGameRepository(AagGameRepository aagGameRepository) {
        this.aagGameRepository = aagGameRepository;
    }

    @Autowired
    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @Autowired
    public void setNotoficationFirebase(NotoficationFirebase notoficationFirebase) {
        this.notoficationFirebase = notoficationFirebase;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setRoomRepository(TournamentRoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Autowired
    public void setPlayerRepository(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Autowired
    public void setVendorRepository(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Autowired
    public void setWalletRepo(WalletRepository walletRepo) {
        this.walletRepo = walletRepo;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setCustomCustomerRepository(CustomCustomerRepository customCustomerRepository) {
        this.customCustomerRepository = customCustomerRepository;
    }

    @Autowired
    public void setTournamentResultRecordRepository(TournamentResultRecordRepository tournamentResultRecordRepository) {
        this.tournamentResultRecordRepository = tournamentResultRecordRepository;
    }

    @Autowired
    public void setTournamentRoomWinnerRepository(TournamentRoomWinnerRepository tournamentRoomWinnerRepository) {
        this.tournamentRoomWinnerRepository = tournamentRoomWinnerRepository;
    }

    @Autowired
    public void setTournamentPlayerRegistrationRepository(TournamentPlayerRegistrationRepository tournamentPlayerRegistrationRepository) {
        this.tournamentPlayerRegistrationRepository = tournamentPlayerRegistrationRepository;
    }

    @Autowired
    private TournamentRoundWinnerRepository tournamentRoundWinnerRepository;
    private final String baseUrl = "http://13.232.105.87:8082";

    private static final double TAX_PERCENT = 0.28;

    private static final double VENDOR_PERCENT = 0.05;

    private static final double PLATFORM_PERCENT = 0.04;

    private static final double USER_WIN_PERCENT = 0.63;

    private static final double BONUS_PERCENT = 0.20;


    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public ResponseEntity<?> sendNotificationToUserBefore3Min() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime fiveMinutesLater = now.plusMinutes(2);

            List<Tournament> upcomingTournaments = tournamentRepository.findByStatusAndScheduledAtBetween(
                    TournamentStatus.SCHEDULED, now, fiveMinutesLater
            );

            System.out.println(upcomingTournaments.size() + " tournaments to notify");

            if (upcomingTournaments.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "No upcoming tournaments found.", null);
            }

            for (Tournament tournament : upcomingTournaments) {
                notifyRegisteredPlayers(tournament);
            }

            return responseService.generateResponse(HttpStatus.OK, "Notifications sent successfully.", null);

        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void autoStartScheduledTournaments() {

        try{
            /*
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).truncatedTo(ChronoUnit.SECONDS);

            // Convert `now` to UTC for consistency with stored `scheduledAt` (assuming it's stored in Asia/Kolkata)
            ZonedDateTime nowInUTC = now.withZoneSameInstant(ZoneOffset.UTC);
    */
/*            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).truncatedTo(ChronoUnit.SECONDS); // Truncate to seconds

            // Debugging: Log the current time in Asia/Kolkata
            System.out.println("Current Time in Asia/Kolkata: " + now);

            // Convert the current time (now) to UTC for the database comparison
            ZonedDateTime nowInUTC = now.withZoneSameInstant(ZoneOffset.UTC);*/
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).truncatedTo(ChronoUnit.SECONDS);

            // Debugging: Log both times
            List<Tournament> tournamentsToStart = tournamentRepository.findByStatusAndScheduledAtGreaterThanEqual(
                    TournamentStatus.SCHEDULED,
                    now
            );
            for (Tournament tournament : tournamentsToStart) {
                if (tournament.getStatus() == TournamentStatus.SCHEDULED) {

                    startTournament(tournament.getId());


                    System.out.println(tournament.getId() + " has been activated now");
                }
            }
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);

        }
    }

    private void notifyRegisteredPlayers(Tournament tournament) {
        int page = 0;
        int size = 50; // you can adjust this size as needed
        Page<TournamentPlayerRegistration> registrationPage;

        do {
            Pageable pageable = PageRequest.of(page, size);
            registrationPage = tournamentPlayerRegistrationRepository.findRegisteredPlayersByTournamentId(
                    tournament.getId(),
                    TournamentPlayerRegistration.RegistrationStatus.REGISTERED,
                    pageable
            );

            for (TournamentPlayerRegistration registration : registrationPage.getContent()) {
                Player player = registration.getPlayer();

                if (player != null && player.getCustomer() != null) {
                    String fcmToken = player.getCustomer().getFcmToken();
                    if (fcmToken != null) {
                        notoficationFirebase.sendNotification(
                                fcmToken,
                                "Tournament starting soon!",
                                "Tournament '" + tournament.getName() + "' will start in 5 minutes. Please join now!"
                        );
                    } else {
                        System.out.println("No FCM token for player: " + player.getCustomer().getId());
                    }
                } else {
                    System.out.println("Invalid player or customer for registration ID: " + registration.getId());
                }
            }

            page++;
        } while (registrationPage.hasNext());
    }


    @Transactional
    public Tournament publishTournament(TournamentRequest tournamentRequest, Long vendorId) throws LimitExceededException {
        try {

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            Tournament tournament = new Tournament();

            Optional<AagAvailableGames> gameAvailable = aagGameRepository.findById(tournamentRequest.getExistinggameId());
            tournament.setGameUrl(gameAvailable.get().getGameImage());

            ThemeEntity theme = em.find(ThemeEntity.class, tournamentRequest.getThemeId());
            if (theme == null) {
                throw new BusinessException("No theme found with the provided ID" , HttpStatus.BAD_REQUEST);
            }

            Double totalPrize = (double) tournamentRequest.getEntryFee() * tournamentRequest.getParticipants();

            // Set Vendor and Theme to the Game
            tournament.setName(gameAvailable.get().getGameName());
            tournament.setVendorId(vendorId);
            tournament.setTheme(theme);
            tournament.setExistinggameId(tournamentRequest.getExistinggameId());
            tournament.setTotalPrizePool(totalPrize);
            tournament.setParticipants(tournamentRequest.getParticipants());
            tournament.setVendorEntity(vendorEntity);
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
//               tournament.setScheduledAt(nowInKolkata.plusHours(1));
              tournament.setScheduledAt(nowInKolkata.plusMinutes(3));

            }

            // Set created and updated timestamps
            tournament.setCreatedDate(nowInKolkata);

            // Save tournament first to get its ID
            Tournament savedTournament = tournamentRepository.save(tournament);



            // Save the game to get the game ID
//            Tournament savedTournament = tournamentRepository.save(tournament);


            // Generate a shareable link for the game
            String shareableLink = generateShareableLink(tournament.getId());
            tournament.setShareableLink(shareableLink);

            vendorEntity.setPublishedLimit((vendorEntity.getPublishedLimit() == null ? 0 : vendorEntity.getPublishedLimit()) + 1);

            // Return the saved game with the shareable link
            return tournamentRepository.save(tournament);

        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;        }
        catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the game: " + e.getMessage(), e);
        }
    }



    @Transactional
    public TournamentPlayerRegistration registerPlayer(Long tournamentId, Long playerId) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));

            if(tournament.getStatus() == TournamentStatus.ACTIVE) {
                throw new BusinessException("Tournament is already active" , HttpStatus.BAD_REQUEST);

            }

            // Check if the tournament has reached the maximum participant limit
            int currentRegistrations = tournamentPlayerRegistrationRepository
                    .countByTournamentIdAndStatus(tournamentId, TournamentPlayerRegistration.RegistrationStatus.REGISTERED);



            // Find the player by ID
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found" , HttpStatus.BAD_REQUEST));

            // Ensure the player is not already registered for the tournament
            Optional<TournamentPlayerRegistration> existingRegistration = tournamentPlayerRegistrationRepository
                    .findByTournamentIdAndPlayer_PlayerId(tournamentId, playerId);

            if (existingRegistration.isPresent()) {
                throw new BusinessException("Player is already registered for this tournament.", HttpStatus.BAD_REQUEST);
            }

            if (currentRegistrations >= tournament.getParticipants()) {
                throw new BusinessException("The tournament has already reached the maximum number of participants.", HttpStatus.BAD_REQUEST);
            }

            // Register the player
            TournamentPlayerRegistration registration = new TournamentPlayerRegistration();
            registration.setTournament(tournament);
            registration.setPlayer(player);
            registration.setStatus(TournamentPlayerRegistration.RegistrationStatus.REGISTERED);

            // Save the registration
            tournamentPlayerRegistrationRepository.save(registration);

            // Update the current number of players in the tournament
            tournament.setCurrentJoinedPlayers(currentRegistrations + 1);
            tournamentRepository.save(tournament);

            // Check if the tournament is full and update the status if necessary
            if (tournament.getCurrentJoinedPlayers() >= tournament.getParticipants()) {

//                tournament.setStatus(TournamentStatus.FULL);  // Mark the tournament as full
//                System.out.println("tournament fulll");
                tournamentRepository.save(tournament);
            }

            return registration;

        }catch (BusinessException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error registering player for the tournament: " + e.getMessage(), e);


        }
    }

    @Transactional
    public List<Player> getActivePlayers(Long tournamentId) {
        try {
            List<TournamentPlayerRegistration> registrations = tournamentPlayerRegistrationRepository
                    .findByTournamentIdAndStatus(tournamentId, TournamentPlayerRegistration.RegistrationStatus.ACTIVE);

            List<Player> players = registrations.stream()
                    .map(TournamentPlayerRegistration::getPlayer)
                    .collect(Collectors.toList());

            return players;

        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error fetching registered players for tournament " + tournamentId + ": " + e.getMessage(), e);


        }
    }
    public Page<Player> getRegisteredPlayers(Long tournamentId, int page, int size) {
        try{
            Pageable pageable = PageRequest.of(page, size);


            Page<TournamentPlayerRegistration> registrationPage =
                    tournamentPlayerRegistrationRepository.findRegisteredPlayersByTournamentId(
                            tournamentId,
                            TournamentPlayerRegistration.RegistrationStatus.REGISTERED,
                            pageable
                    );

            return registrationPage.map(TournamentPlayerRegistration::getPlayer);
        }

         catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error fetching registered players for tournament " + tournamentId + ": " + e.getMessage(), e);


        }
    }


//tournment id and player id find regsiter user list

    public Boolean findTournamentResultRecordByTournamentIdAndPlayerId(Long tournamentId, Long playerId) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));
            List<TournamentPlayerRegistration> registeredPlayers = tournamentPlayerRegistrationRepository.findByTournamentIdAndStatus(
                    tournamentId, TournamentPlayerRegistration.RegistrationStatus.REGISTERED
            );


            registeredPlayers = registeredPlayers.stream()
                    .filter(reg -> reg.getPlayer().getPlayerId().equals(playerId))
                    .collect(Collectors.toList());

            if (registeredPlayers.isEmpty()) {
                return false;
            } else {
                return true;
            }
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error fetching registered players for tournament " + tournamentId + ": " + e.getMessage(), e);


        }

    }

    @Transactional
    public Page<Tournament> getAllTournaments(Pageable pageable, List<TournamentStatus> statuses, Long vendorId) {
        try {
            if (statuses != null && !statuses.isEmpty() && vendorId != null) {
                return tournamentRepository.findByStatusInAndVendorId(statuses, vendorId, pageable);
            } else if (statuses != null && !statuses.isEmpty()) {
                return tournamentRepository.findByStatusIn(statuses, pageable);
            } else if (vendorId != null) {
                return tournamentRepository.findByVendorId(vendorId, pageable);
            } else {
                return tournamentRepository.findAll(pageable);
            }
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching tournaments: " + e.getMessage(), e);
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
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<TournamentRoom> getAllRoomsByTournamentId(Long tournamentId) {
        try {
            return roomRepository.findByTournamentId(tournamentId);
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
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
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
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
    public Tournament startTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));

        if (tournament.getStatus() != TournamentStatus.SCHEDULED) {
            throw new IllegalStateException("Tournament is not in SCHEDULED status");
        }

        List<Player> activePlayers = getActivePlayers(tournamentId);
        if(activePlayers.size() == 0) {
            throw new IllegalStateException("Tournament has no active players" + tournamentId);
        }

        System.out.println("activePlayers: " + activePlayers.size()  + tournamentId + " Tournament ID: " + tournamentId);
        if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);

            BigDecimal entryFeePerUser = BigDecimal.valueOf(tournament.getEntryFee());
            BigDecimal totalCollection = entryFeePerUser;
            System.out.println("entryFeePerUser: " + entryFeePerUser + " totalCollection: " + totalCollection);

            BigDecimal userPrizePool = totalCollection.multiply(PriceConstant.USER_PRIZE_PERCENT);
            tournament.setRoomprize(userPrizePool);
            tournament.setTotalPrizePool(totalCollection.doubleValue());
            tournament.setTotalrounds(0);
            tournament.setStatus(TournamentStatus.COMPLETED);
            tournamentRepository.save(tournament);
            setvendorShare(tournament);

            TournamentResultRecord result = new TournamentResultRecord();
            result.setTournament(tournamentRepository.findById(tournamentId).orElseThrow());
            result.setRoomId(null);
            result.setPlayer(winner);
            result.setScore(0);
            result.setIsWinner(true);
            result.setStatus("WINNER");
            result.setRound(1);
            result.setPlayedAt(LocalDateTime.now());
            tournamentResultRecordRepository.save(result);

            System.out.println("🎉 Only one player. Tournament " + tournament.getId()
                    + " completed. User " + winner.getPlayerId() + " is the winner with prize: " + userPrizePool);
            return tournament;
        }
        System.out.println("Number of active players: " + activePlayers.size());

        if (activePlayers.size() < 2) {
            throw new IllegalStateException("Not enough players to start the tournament (minimum 2 needed)");
        }

        Collections.shuffle(activePlayers); // Randomize for fair matchups

        int numberOfPairs = activePlayers.size() / 2;
        int freePassCount = activePlayers.size() % 2;
        int playerIndex = 0;

        // Create rooms and assign 2 players per room
        for (int i = 0; i < numberOfPairs; i++) {
            TournamentRoom room = new TournamentRoom();
            room.setTournament(tournament);
            room.setMaxParticipants(2);
            room.setCurrentParticipants(0);
            room.setStatus("IN_PROGRESS");
            room.setRound(1);
            roomRepository.save(room);

            String gamePassword = this.createNewGame(baseUrl, tournament.getId(), room.getId(),
                    room.getMaxParticipants(), tournament.getMove(), tournament.getRoomprize());
            room.setGamepassword(gamePassword);

            // Assign two players to the room
            assignPlayerToSpecificRoom(activePlayers.get(playerIndex++), tournamentId, room);
            assignPlayerToSpecificRoom(activePlayers.get(playerIndex++), tournamentId, room);
        }

        // Handle free pass if odd number of players
        if (freePassCount == 1) {
            Player freePassPlayer = activePlayers.get(playerIndex);
            assignFreePassToPlayer(freePassPlayer, tournamentId, 1);
        }

        // Final check to see all rooms and participants
        List<TournamentRoom> rooms = roomRepository.findByTournamentIdAndStatus(tournamentId, "PLAYING");
        int totalPlayers = rooms.stream().mapToInt(TournamentRoom::getCurrentParticipants).sum();

        System.out.println("totalPlayers: " + totalPlayers);

        int totalRounds = (int) Math.ceil(Math.log(totalPlayers + freePassCount) / Math.log(2));
        tournament.setTotalrounds(totalRounds);

        BigDecimal entryFeePerUser = BigDecimal.valueOf(tournament.getEntryFee());
        BigDecimal totalCollection = entryFeePerUser.multiply(BigDecimal.valueOf(totalPlayers + freePassCount));
        System.out.println("entryFeePerUser: " + entryFeePerUser + " totalCollection: " + totalCollection);

        BigDecimal userPrizePool = totalCollection.multiply(PriceConstant.USER_PRIZE_PERCENT);
        tournament.setRoomprize(userPrizePool);
        tournament.setTotalPrizePool(totalCollection.doubleValue());

        tournament.setStatus(TournamentStatus.ACTIVE);
        tournamentRepository.save(tournament);

        String fcmToken = tournament.getVendorEntity().getFcmToken();
        if (fcmToken != null) {
            notoficationFirebase.sendNotification(
                    fcmToken,
                    "\uD83C\uDF89 Your Tournament Has Begun!",
                    "Congratulations! The tournament '" + tournament.getName() + "' you hosted is now live. Monitor the progress and enjoy the event!"
            );

        }

        System.out.println("🏆 Tournament " + tournament.getId() + " is now ACTIVE with "
                + rooms.size() + " rooms, "
                + (totalPlayers + freePassCount) + " players, "
                + totalRounds + " rounds, "
                + "User Prize Pool: " + userPrizePool);
        return tournament;

    }

    public void assignFreePassToPlayer(Player player, Long tournamentId, int round) {
        TournamentResultRecord result = new TournamentResultRecord();
        result.setTournament(tournamentRepository.findById(tournamentId).orElseThrow());
        result.setRoomId(null);
        result.setPlayer(player);
        result.setScore(0);
        result.setIsWinner(true);
        result.setStatus("FREE_PASS");
        result.setRound(round);
        result.setPlayedAt(LocalDateTime.now());
        tournamentResultRecordRepository.save(result);

        System.out.println("🎟️ Player " + player.getPlayerId() + " received a free pass for round " + round);
    }


    public void assignPlayerToSpecificRoom(Player player, Long tournamentId, TournamentRoom room) {
        player.setTournamentRoom(room);
        room.setCurrentParticipants(room.getCurrentParticipants() + 1);
        room.setStatus("PLAYING");
        playerRepository.save(player);
        roomRepository.save(room);
    }



    @Transactional
    public ResponseEntity<?> leaveRoom(Long playerId, Long tournamentId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId , HttpStatus.BAD_REQUEST));

            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found with ID: " + tournamentId , HttpStatus.BAD_REQUEST));

            TournamentRoom tournamentRoom = player.getTournamentRoom();

            if (tournamentRoom == null) {
                return responseService.generateErrorResponse("Player is not in any Tournament Room", HttpStatus.BAD_REQUEST);
            }

            // Remove player from the room
            player.setTournamentRoom(null);
            playerRepository.save(player);

            // If no players remain in the room, mark it as completed
            List<Player> remainingPlayers = playerRepository.findAllByTournamentRoom(tournamentRoom);
            if (remainingPlayers.isEmpty()) {
                tournamentRoom.setStatus("COMPLETED");
                roomRepository.save(tournamentRoom);
            }

            List<TournamentResultRecord> resultRecords = tournamentResultRecordRepository
                    .findTopByPlayerAndTournamentOrderByPlayedAtDesc(player, tournament);

            if (!resultRecords.isEmpty()) {
                TournamentResultRecord latestResult = resultRecords.get(0);
                if ("READY_TO_PLAY".equalsIgnoreCase(latestResult.getStatus())) {
                    latestResult.setStatus("QUIT");
                    tournamentResultRecordRepository.save(latestResult);
                }
            }

            return responseService.generateSuccessResponse("Player left the Tournament Room", tournament.getName(), HttpStatus.OK);

        }catch (BusinessException ex) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, ex);

return responseService.generateErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player cannot leave the room because: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private TournamentRoom convertTournamentResultToRoom(TournamentResultRecord result) {
        TournamentRoom room = new TournamentRoom();
        room.setMaxParticipants(2); // Assuming a 2-player room, you can adjust this logic if needed
        room.setGamepassword(null); // Set the game password or leave it as null if not applicable
        room.setTournament(result.getTournament());

        // Wrap the single Player into a List
        room.setCurrentPlayers(Collections.singletonList(result.getPlayer())); // Wrap single player in a List

        room.setStatus("Free Pass");
        return room;
    }

    @Transactional
    public TournamentRoom getMyRoomDetails(Long playerId, Long tournamentId) {

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

            TournamentRoom room = player.getTournamentRoom();
            System.out.println("Room: " + room);
            if (room == null ) {
                List<TournamentResultRecord> records = tournamentResultRecordRepository.findAllByPlayerIdAndTournamentIdOrderByIdDesc(playerId, tournamentId);
                if (records.isEmpty()) {
                    throw new BusinessException("No tournament result found for player in this tournament.", HttpStatus.BAD_REQUEST);
                }
                TournamentResultRecord latestRecord = records.get(0);
                return convertTournamentResultToRoom(latestRecord);

            }
            if (room.getTournament() == null || !room.getTournament().getId().equals(tournamentId)) {
                throw new BusinessException("Player is not in a valid room for this tournament.", HttpStatus.BAD_REQUEST);
            }

            return room;

    }

    @Transactional
    public TournamentRoom assignPlayerToRoom(Long playerId, Long tournamentId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new BusinessException("Player not found with id " + playerId , HttpStatus.BAD_REQUEST));

        if (player.getTournamentRoom() != null) {
            throw new BusinessException("Player already assigned to a room." , HttpStatus.BAD_REQUEST);
        }

        // Find an available room using the helper method
        TournamentRoom room = findAvailableRoom(tournamentId);

        // If an available room is found, assign the player to it
        if (room != null) {
            player.setTournamentRoom(room);
            room.getCurrentPlayers().add(player);
            room.setCurrentParticipants(room.getCurrentParticipants() + 1);

            // If the room is the first round, update the tournament's joined players count
            if (room.getRound() == 1) {
                Tournament tournament = tournamentRepository.findById(tournamentId)
                        .orElseThrow(() -> new BusinessException("Tournament not found with id " + tournamentId , HttpStatus.BAD_REQUEST));
                tournament.setCurrentJoinedPlayers(tournament.getCurrentJoinedPlayers() + 1);
            }

            roomRepository.save(room);
            return room;
        }

        // If no available room is found, create a new room
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new BusinessException("Tournament not found with id " + tournamentId , HttpStatus.BAD_REQUEST));

        TournamentRoom newRoom = new TournamentRoom();
        newRoom.setTournament(tournament);
        newRoom.setStatus("OPEN"); // Mark room as open
        newRoom.setCurrentParticipants(1); // Initially, the player is in the room
        newRoom.setMaxParticipants(tournament.getParticipants()); // Set max participants (you can adjust this)
        newRoom = roomRepository.save(newRoom);

        // Assign the player to the newly created room
        player.setTournamentRoom(newRoom);
        newRoom.getCurrentPlayers().add(player);
        newRoom.setCurrentParticipants(newRoom.getCurrentParticipants() + 1);
        roomRepository.save(newRoom);
        System.out.println("✅ New Room Created: " + newRoom.getId());
        return newRoom;

    }

    public TournamentResultRecord addPlayerToNextRound(Long tournamentId, Integer roundNumber, TournamentResultRecord record) {

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));

        Player player = record.getPlayer();
      /*  TournamentRoundParticipant tournamentRoundParticipant = new TournamentRoundParticipant();
        tournamentRoundParticipant.setTournamentId(tournament.getId());
        tournamentRoundParticipant.setPlayerId(player.getPlayerId());
        tournamentRoundParticipant.setRoundNumber(roundNumber + 1);
        tournamentRoundParticipant.setJoinedAt(LocalDateTime.now());
        tournamentRoundParticipant.setStatus("READY_TO_PLAY");
     return    tournamentRoundParticipantRepository.save(tournamentRoundParticipant);*/

        TournamentResultRecord nextRoundRecord = new TournamentResultRecord();
        nextRoundRecord.setTournament(tournament);
        nextRoundRecord.setPlayer(player);
        nextRoundRecord.setRound(roundNumber);
        nextRoundRecord.setStatus("READY_TO_PLAY");
        nextRoundRecord.setScore(0);
        return tournamentResultRecordRepository.save(nextRoundRecord);


/*        TournamentResultRecord nextRoundRecord = new TournamentResultRecord();
        nextRoundRecord.setTournament(tournament);
        nextRoundRecord.setPlayer(player);
        nextRoundRecord.setRound(roundNumber + 1);
        nextRoundRecord.setStatus("READY_TO_PLAY");
        nextRoundRecord.setScore(0);
      return   tournamentResultRecordRepository.save(nextRoundRecord);*/

       /* Optional<TournamentRoom> existingRoomOpt = roomRepository
                .findFirstByTournamentIdAndRoundAndStatusAndCurrentParticipantsLessThan(
                        tournamentId, roundNumber + 1, "OPEN", 2);

        TournamentRoom room;
        if (existingRoomOpt.isPresent()) {
            room = existingRoomOpt.get();
        } else {
            room = new TournamentRoom();
            room.setTournament(tournament);
            room.setRound(roundNumber + 1);
            room.setMaxParticipants(2);
            room.setCurrentParticipants(0);
            room.setStatus("OPEN");
            roomRepository.save(room);
        }

        nextRoundRecord.setRoomId(room.getId());
        room.setCurrentParticipants(room.getCurrentParticipants() + 1);
        tournamentResultRecordRepository.save(nextRoundRecord);
        roomRepository.save(room);

        // If room full → create game and update room
        if (room.getCurrentParticipants() == room.getMaxParticipants()) {
            String gamePassword = createNewGame(
                    baseUrl,
                    tournament.getId(),
                    room.getId(),
                    room.getMaxParticipants(),
                    tournament.getMove(),
                    3.2
            );
            room.setGamepassword(gamePassword);
            room.setStatus("IN_PROGRESS");
            roomRepository.save(room);
        }
*/
    }

    private TournamentRoom findAvailableRoom(Long tournamentId) {
        List<TournamentRoom> rooms = roomRepository.findByTournamentIdAndStatus(tournamentId, "OPEN");
        for (TournamentRoom room : rooms) {
            if (room.getCurrentParticipants() < room.getMaxParticipants()) {
                return room;
            }
        }
        return null;
    }


    public List<TournamentResultRecord> getPlayersByTournamentAndRound(Long tournamentId, Integer roundNumber,Boolean iswinner) {
        try {
            // Retrieve the list of players by tournamentId and roundNumber
            if(iswinner){
                return tournamentResultRecordRepository.findByTournamentIdAndRoundAndIsWinnerTrue(tournamentId, roundNumber);

            }else{
                return tournamentResultRecordRepository.findByTournamentIdAndRound(tournamentId, roundNumber);

            }
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while retrieving players for tournament " + tournamentId + " and round " + roundNumber, e);
        }
    }

    public void deleteTournamentRoundWinner(Long tournamentId, Long playerId) {
        // Call the custom delete method
        tournamentRoundWinnerRepository.deleteByTournamentIdAndPlayerId(tournamentId, playerId);
    }




    public String createNewGame(String baseUrl, Long gameId, Long roomId, Integer players, Integer move, BigDecimal prize) {
        try {
            // Construct the URL for the POST request, including query parameters
            String url = baseUrl + "/CreateNewGame?gametype=TOURNAMENT"
                    + "&gameid=" + gameId
                    + "&roomid=" + roomId
                    + "&players=" + players
                    + "&prize=" + prize
                    + "&moves=" + move;

            System.out.println("url: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            String responseBody = response.getBody();
            System.out.println("responseBody: " + responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            String gamePassword = jsonResponse.get("GamePassword").asText();
            System.out.println("gamePassword: " + gamePassword);

            return gamePassword;

        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while creating the game on the server: " + e.getMessage(), e);
        }
    }



    private PlayerDtoWinner determineWinner(List<PlayerDtoWinner> players) {

        return players.stream()

                .max(Comparator.comparingInt(PlayerDtoWinner::getScore))  // Find the player with the highest score

                .orElseThrow(() -> new BusinessException("No players found", HttpStatus.BAD_REQUEST));

    }

    private void updateWinnerWallet(Long customerId) {

        CustomCustomer customCustomer = em.find(CustomCustomer.class,customerId);


        Wallet wallet = walletRepo.findByCustomCustomer(customCustomer);

        BigDecimal winAmount = BigDecimal.valueOf(10);
        wallet.setWinningAmount(wallet.getWinningAmount().add(winAmount));
        walletRepo.save(wallet);
    }


    public void createNextRoundRooms(Tournament tournament, int currentRound) {
        // Fetch winners and free pass players from the previous round
        List<TournamentResultRecord> resultRecords = tournamentResultRecordRepository
                .findByTournamentIdAndRound(tournament.getId(), currentRound);
        // Filter winners and free pass players
        List<Player> winnerPlayers = resultRecords.stream()
                .filter(TournamentResultRecord::getIsWinner)
                .map(TournamentResultRecord::getPlayer)
                .collect(Collectors.toList());

        List<Player> freePassPlayers = resultRecords.stream()
                .filter(result -> result.getScore() == 0)
                .map(TournamentResultRecord::getPlayer)
                .collect(Collectors.toList());

        // Combine winners and free pass players for the next round
        List<Player> nextRoundPlayers = new ArrayList<>(winnerPlayers);
        nextRoundPlayers.addAll(freePassPlayers);

        // Handle no players case
        if (nextRoundPlayers.isEmpty()) {
            throw new BusinessException("No players found for the next round." , HttpStatus.BAD_REQUEST);
        }

        // Shuffle players to randomize matchups
        Collections.shuffle(nextRoundPlayers);

        int nextRound = currentRound + 1;
        Queue<Player> queue = new LinkedList<>(nextRoundPlayers);

        int playerCount = nextRoundPlayers.size();
        int bracketSize = nextPowerOfTwo(playerCount);  // Ensure it's a power of 2
        int byes = bracketSize - playerCount;

        // Create rooms for the next round
        int roomCount = 0;
        while (!queue.isEmpty()) {
            TournamentRoom room = new TournamentRoom();
            room.setTournament(tournament);
            room.setRound(nextRound);
            room.setMaxParticipants(2);
            room.setCurrentParticipants(0);
            room.setStatus("OPEN"); // Room is open for matchmaking

            // Take two players from the queue
            Player player1 = queue.poll();
            Player player2 = null;

            if (!queue.isEmpty()) {
                player2 = queue.poll();
            } else {
                byes--;
            }

            if (player1 != null) room.getCurrentPlayers().add(player1);
            if (player2 != null) room.getCurrentPlayers().add(player2);

            roomRepository.save(room); // Save the room to DB
            roomCount++;

        }

        if (roomCount == nextRoundPlayers.size() / 2) {
            createNextRoundRooms(tournament, nextRound);
        }
    }

    public void finishTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));
        tournament.setStatus(TournamentStatus.COMPLETED);
        setvendorShare(tournament);
        tournamentRepository.save(tournament);
    }

    private void setvendorShare(Tournament tournament) {
        BigDecimal totalWinningAmount = BigDecimal.valueOf(tournament.getTotalPrizePool());
        BigDecimal vendorShareAmount = totalWinningAmount.multiply(PriceConstant.VENDOR_REVENUE_PERCENT);
        VendorWallet wallet = tournament.getVendorEntity().getWallet();
        wallet.setWinningAmount(vendorShareAmount);

    }
    @Transactional
    public void processMatchResults(GameResult gameResult) {
            List<PlayerDtoWinner> players = gameResult.getPlayers();

            System.out.println("Players: " + gameResult.getGameId());

            // Check if players list is null or empty
            if (players == null || players.size() < 2) {
                System.out.println("Players list is null or doesn't contain enough players");
                throw new BusinessException("Players list is null or doesn't contain enough players" , HttpStatus.BAD_REQUEST);
                }

            PlayerDtoWinner player1 = players.get(0);
            PlayerDtoWinner player2 = players.get(1);

            PlayerDtoWinner winner = null;
            PlayerDtoWinner loser = null;

            Tournament tournament = tournamentRepository.findById(gameResult.getGameId())
                    .orElseThrow(() -> new BusinessException("Game not found" , HttpStatus.BAD_REQUEST));

            // Handle tie or determine winner and loser based on score comparison
            if (player1.getScore() > player2.getScore()) {
                winner = player1;
                loser = player2;
            } else if (player2.getScore() > player1.getScore()) {
                winner = player2;
                loser = player1;
            } else {
                System.out.println("It's a tie!");
                return;
            }

            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), winner, true); // Winner with isWinner=true
            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), loser, false); // Loser with isWinner=false

            int currentRound = tournament.getRound();
           startNextRound( tournament.getId(),  currentRound);

    }

    @Scheduled(fixedRate = 30000)
    public void checkAndStartNextRoundsForAllTournaments() {
/*        int page = 0;
        int size = 10;

        Page<Tournament> tournamentPage;
        do {
            tournamentPage = tournamentRepository.findByStatus(TournamentStatus.ACTIVE, PageRequest.of(page, size));
            if (tournamentPage == null || tournamentPage.isEmpty()) {
                return;
            }

            for (Tournament tournament : tournamentPage.getContent()) {
                int currentRound = tournament.getRound();

                startNextRound(tournament.getId(), currentRound);
                em.flush();
                em.clear();
            }

            page++;
        } while (tournamentPage.hasNext());*/

        List<Tournament> activeTournaments = tournamentRepository.findByStatus(TournamentStatus.ACTIVE);
        for (Tournament tournament : activeTournaments) {
            startNextRound(tournament.getId(), tournament.getRound());
        }

    }

    public void startNextRound(Long tournamentId, int currentRound) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));

            if (isRoundCompleted(tournamentId, currentRound)) {
                int nextRound = currentRound + 1;
                distributeRoundPrize(tournament, currentRound);

                if (nextRound <= tournament.getTotalrounds()) {
                    // Check the number of players available for the next round
                    List<TournamentResultRecord> readyPlayers = tournamentResultRecordRepository
                            .findByTournamentIdAndRoundAndStatus(tournamentId, nextRound, "READY_TO_PLAY");

                    if (readyPlayers.size() <= 1) {
                        System.out.println("🏁 Only one player remains — finishing tournament.");
                        finishTournament(tournamentId);
                        return;
                    }

                    // Update tournament round and proceed to the next round
                    tournament.setRound(nextRound);
                    tournamentRepository.save(tournament);

                    System.out.println("✅ Round " + currentRound + " completed. Scheduling round " + nextRound);
                    processNextRoundMatches(tournamentId, nextRound);

                } else {
                    finishTournament(tournamentId);
                    System.out.println("🎯 Tournament completed!");
                }
            } else {
                // Log if the round is not completed yet
                System.out.println("⏳ Round " + currentRound + " is not completed yet.");
            }
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);

        }
        catch (IllegalStateException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }





    @Transactional
    public void distributeRoundPrize(Tournament tournament, int round) {
        BigDecimal totalPrize = tournament.getRoomprize();
        int totalRounds = tournament.getTotalrounds();
        BigDecimal roundPrize = totalPrize.divide(BigDecimal.valueOf(totalRounds), 2, RoundingMode.HALF_UP);

        List<TournamentResultRecord> winners = tournamentResultRecordRepository
                .findByTournamentIdAndRoundAndIsWinnerTrue(tournament.getId(), round);

        int winnersCount = winners.size();
        if (winnersCount == 0) return;

        BigDecimal prizePerWinner = roundPrize.divide(BigDecimal.valueOf(winnersCount), 2, RoundingMode.HALF_UP);

        for (TournamentResultRecord winner : winners) {
            Notification notification = new Notification();
            notification.setAmount(prizePerWinner.doubleValue());
            notification.setDetails("You won $" + prizePerWinner + " in Round " + round);
            notification.setDescription("Round Prize");
            notification.setRole("Customer");
            notification.setCustomerId(winner.getPlayer().getCustomer().getId());
            notificationRepository.save(notification);
            winner.setAmmount(prizePerWinner);
            tournamentResultRecordRepository.save(winner);
        }
    }

    @Transactional
    public void storeMatchResult(Long tournamentId, Long roomId, PlayerDtoWinner player, boolean isWinner) {
        TournamentRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException("Room not found" , HttpStatus.BAD_REQUEST));

        Player playerEntity = playerRepository.findById(player.getPlayerId())
                .orElseThrow(() -> new BusinessException("Player not found" , HttpStatus.BAD_REQUEST));

        int round = room.getTournament().getRound();

        Optional<TournamentResultRecord> existingResultOpt =
                tournamentResultRecordRepository.findByTournamentIdAndPlayerIdAndRound(
                        tournamentId, player.getPlayerId(), round);

        TournamentResultRecord result = existingResultOpt.orElseGet(TournamentResultRecord::new);

        result.setTournament(room.getTournament());
        result.setRoomId(roomId);
        result.setPlayer(playerEntity);
        result.setScore(player.getScore());
        result.setIsWinner(isWinner);
        result.setPlayedAt(LocalDateTime.now());
        result.setRound(round);
        result.setStatus(isWinner ? "WINNER" : "ELIMINATED");

        tournamentResultRecordRepository.save(result);

        room.setStatus("COMPLETED");
        roomRepository.save(room);
    }

    public boolean isRoundCompleted(Long tournamentId, int roundNumber) {

        System.out.println("Round Number: " + roundNumber  + " Tournament ID: " + tournamentId);
        // Count only rooms that actually had participants
        long validRoomsCount = roomRepository.countByTournamentIdAndRoundAndCurrentParticipantsGreaterThan(
                tournamentId, roundNumber, 0);

        System.out.println("Valid rooms count: " + validRoomsCount);
        if (validRoomsCount == 0) {
            long freePassCount = tournamentResultRecordRepository
                    .countByTournamentIdAndRoundAndStatus(tournamentId, roundNumber, "FREE_PASS");
            return freePassCount > 0;
        }


        long completedRoomsCount = roomRepository.countByTournamentIdAndRoundAndStatusAndCurrentParticipantsGreaterThan(
                tournamentId, roundNumber, "COMPLETED", 0);
        System.out.println("completedRoomsCount rooms count: " + completedRoomsCount);

        return completedRoomsCount == validRoomsCount;
    }


/*    public boolean isRoundCompleted(Long tournamentId, int roundNumber) {
        long totalRoomsCount = roomRepository.countByTournamentIdAndRound(tournamentId, roundNumber);

        if (totalRoomsCount == 0) {
            long freePassCount = tournamentResultRecordRepository.countByTournamentIdAndRoundAndStatus(
                    tournamentId, roundNumber, "FREE_PASS"
            );

            return freePassCount > 0;
        }

        // Regular case — check if all rooms are completed
        long completedRoomsCount = roomRepository.countByTournamentIdAndRoundAndStatus(
                tournamentId, roundNumber, "COMPLETED"
        );

        return completedRoomsCount == totalRoomsCount;
    }*/



    public int nextPowerOfTwo(int n) {
        int powerOfTwo = 1;
        while (powerOfTwo < n) {
            powerOfTwo *= 2;
        }
        return powerOfTwo;
    }

    @Transactional
    public void processNextRoundMatches(Long tournamentId, Integer roundNumber) {
                try{
                    Tournament tournament = tournamentRepository.findById(tournamentId)
                            .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));

                    List<TournamentResultRecord> participants = tournamentResultRecordRepository
                            .findByTournamentIdAndRoundAndStatus(tournamentId, roundNumber, "READY_TO_PLAY");

                    if (participants.size() < 2) {
                        throw new IllegalStateException("Not enough players to start the round (minimum 2 needed)");
                    }

                    Collections.shuffle(participants);

                    int numberOfPairs = participants.size() / 2;
                    int freePassCount = participants.size() % 2;
                    int playerIndex = 0;

                    for (int i = 0; i < numberOfPairs; i++) {
                        TournamentResultRecord p1 = participants.get(playerIndex);
                        TournamentResultRecord p2 = participants.get(playerIndex + 1);

                        Player player1 = p1.getPlayer();
                        Player player2 = p2.getPlayer();

                        TournamentRoom room = new TournamentRoom();
                        room.setTournament(tournament);
                        room.setRound(roundNumber);
                        room.setMaxParticipants(2);
                        room.setCurrentParticipants(2);
                        room.setStatus("OPEN");
                        roomRepository.save(room);



                        assignPlayerToSpecificRoom(player1, tournamentId, room);
                        assignPlayerToSpecificRoom(player2, tournamentId, room);

                        String gamePassword = this.createNewGame(baseUrl, tournament.getId(), room.getId(),
                                room.getMaxParticipants(), tournament.getMove(), tournament.getRoomprize());
                        room.setGamepassword(gamePassword);
                        room.setStatus("IN_PROGRESS");
                        roomRepository.save(room);
                        tournamentResultRecordRepository.save(p1);
                        tournamentResultRecordRepository.save(p2);

                        playerIndex += 2;
                    }

                    // Handle free pass
                    if (freePassCount == 1) {
                        TournamentResultRecord freePassParticipant = participants.get(playerIndex);
                        Player freePassPlayer = freePassParticipant.getPlayer();

                        assignFreePassToPlayer(freePassPlayer, tournamentId, roundNumber);

                        // Mark player as passed to next round
                        freePassParticipant.setStatus("FREE_PASS");
                        tournamentResultRecordRepository.save(freePassParticipant);
                    }
                }catch (Exception e){
                    exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
                    throw new RuntimeException(e);
                }
    }


    public List<TournamentResultRecord> getPlayersForNextRound(Long tournamentId, int roundNumber) {
        // Fetch players who are winners or have a free pass for the previous round
        return tournamentResultRecordRepository.findByTournamentIdAndRoundAndStatusIn(
                tournamentId,
                roundNumber,
                Arrays.asList("WINNER", "FREE_PASS")
        );
    }


    public List<Map<String, Object>> generateTournamentRounds(Long tournamentId) {
        try{
            Tournament tournament= tournamentRepository.findById(tournamentId).orElseThrow(()-> new BusinessException("Tournament not found", HttpStatus.BAD_REQUEST));

            int totalPlayers = tournament.getParticipants();

            List<Map<String, Object>> rounds = new ArrayList<>();
            int roundNumber = 1;
            int currentPlayers = totalPlayers;

            while (currentPlayers >= 2) {
                int winners = currentPlayers / 2;
                Map<String, Object> roundInfo = new HashMap<>();

                roundInfo.put("round", roundNumber + " Round");
                roundInfo.put("progress", currentPlayers == 2 ? "WINNER" : "Completed");
                roundInfo.put("numberOfWinners", currentPlayers == 2 ? "1" : String.valueOf(winners));
                roundInfo.put("totalPlayers", String.valueOf(currentPlayers));
                roundInfo.put("prize", getDynamicPrize(currentPlayers, tournament));

                rounds.add(0, roundInfo);

                currentPlayers = winners;
                roundNumber++;
            }

            return rounds;
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        }catch (Exception e){
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);

            throw new RuntimeException(e);
        }
    }
    private BigDecimal getDynamicPrize(int currentPlayers, Tournament tournament) {
        int winners = currentPlayers / 2;
        BigDecimal roomPrize = tournament.getRoomprize();
        if (roomPrize == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(winners).multiply(roomPrize);
    }



}
