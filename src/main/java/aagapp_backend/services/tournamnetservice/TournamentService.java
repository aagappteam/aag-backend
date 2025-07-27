package aagapp_backend.services.tournamnetservice;

import aagapp_backend.components.Constant;
import aagapp_backend.components.pricelogic.PriceConstant;
import aagapp_backend.dto.*;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.AagAvailableGames;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.*;
import aagapp_backend.entity.wallet.VendorWallet;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.enums.ActivityType;
import aagapp_backend.enums.TournamentStatus;
import aagapp_backend.enums.VendorStatus;
import aagapp_backend.exception.GameNotFoundException;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.game.ThemeRepository;
import aagapp_backend.repository.tournament.*;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.repository.wallet.VendorWalletRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.CommonService;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.firebase.NotoficationFirebase;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.social.FollowerNotificationService;
import aagapp_backend.spec.TournamentSpecification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.naming.LimitExceededException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    @Autowired
    private VendorWalletRepository walletRepo;

    @Autowired
    private FollowerNotificationService followerNotificationService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ThemeRepository themeRepository;


        @Autowired
        private CustomCustomerRepository customCustomerRepository;
    private TournamentRepository tournamentRepository;
    private EntityManager em;
    private NotificationRepository notificationRepository;
    private AagGameRepository aagGameRepository;
    private NotoficationFirebase notoficationFirebase;
    private ExceptionHandlingImplement exceptionHandling;
    private TournamentRoomRepository roomRepository;
    private PlayerRepository playerRepository;
    private VendorRepository vendorRepository;
    private ResponseService responseService;
    private TournamentResultRecordRepository tournamentResultRecordRepository;
    private TournamentPlayerRegistrationRepository tournamentPlayerRegistrationRepository;
    @Autowired
    public void setTournamentRepository(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
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
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }


    @Autowired
    public void setTournamentResultRecordRepository(TournamentResultRecordRepository tournamentResultRecordRepository) {
        this.tournamentResultRecordRepository = tournamentResultRecordRepository;
    }

    @Autowired
    public void setTournamentPlayerRegistrationRepository(TournamentPlayerRegistrationRepository tournamentPlayerRegistrationRepository) {
        this.tournamentPlayerRegistrationRepository = tournamentPlayerRegistrationRepository;
    }




    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public ResponseEntity<?> sendNotificationToUserBefore3Min() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            ZonedDateTime twoMinutesLater = now.plusMinutes(2);
            ZonedDateTime windowStart = twoMinutesLater.minusSeconds(30);
            ZonedDateTime windowEnd = twoMinutesLater.plusSeconds(30);
            List<Tournament> upcomingTournaments = tournamentRepository.findByStatusAndScheduledAtBetween(
                    TournamentStatus.SCHEDULED, windowStart, windowEnd
            );

/*
            List<Tournament> upcomingTournaments = tournamentRepository.findByStatusAndScheduledAtBetween(
                    TournamentStatus.SCHEDULED, now, fiveMinutesLater
            );
*/


            if (upcomingTournaments.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "No upcoming tournaments found.", null);
            }

            for (Tournament tournament : upcomingTournaments) {
                notifyRegisteredPlayers(tournament);
            }

            return responseService.generateResponse(HttpStatus.OK, "Notifications sent successfully.", null);

        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Scheduled(cron = "*/10 * * * * *") // Runs every 10 seconds
    public void autoStartScheduledTournaments() {
        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).truncatedTo(ChronoUnit.SECONDS);

            ZonedDateTime windowStart = now.plusSeconds(15);
            ZonedDateTime windowEnd = now.plusSeconds(20);

            List<Tournament> tournamentsToStart = tournamentRepository.findTournamentsToStart(
                    TournamentStatus.SCHEDULED,
                    windowStart,
                    windowEnd
            );

            for (Tournament tournament : tournamentsToStart) {
                try {
                    startTournament(tournament.getId()); // Safe to call, will start only once
                } catch (Exception e) {
                    exceptionHandling.handleException(e);
                }
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
        }
    }



//    @Scheduled(cron = "*/2 * * * * *")
//    public void autoStartScheduledTournaments() {
//
//        try{
//
//            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).truncatedTo(ChronoUnit.SECONDS);
///*            ZonedDateTime earlyWindowStart = now.plusSeconds(10);
//            ZonedDateTime earlyWindowEnd = now.plusSeconds(20);*/
//
//
//            ZonedDateTime targetScheduledAt = now.plusSeconds(10);
//
//            // Allow a small 1-second window around the target to handle scheduler delay
//            ZonedDateTime windowStart = targetScheduledAt.minusSeconds(1);
//            ZonedDateTime windowEnd = targetScheduledAt.plusSeconds(1);
//
///*            List<Tournament> tournamentsToStart = tournamentRepository.findTournamentsToStart(
//                    TournamentStatus.SCHEDULED,
//                    windowStart,
//                    windowEnd,
//                    now
//            );*/
//
//            List<Tournament> tournamentsToStart = tournamentRepository.findTournamentsToStart(
//                    TournamentStatus.SCHEDULED,
//                    windowStart,
//                    windowEnd
//            );
//            for (Tournament tournament : tournamentsToStart) {
//                if (tournament.getStatus() == TournamentStatus.SCHEDULED) {
//
//                    startTournament(tournament.getId());
//
//                }
//            }
//        }
//        catch (Exception e) {
//            exceptionHandling.handleException(e);
//
//        }
//    }

    private void notifyRegisteredPlayers(Tournament tournament) {
        int page = 0;
        int size = 50;
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
                        try {
                            notoficationFirebase.sendNotification(
                                    fcmToken,
                                    "Tournament starting soon!",
                                    "Tournament '" + tournament.getName() + "' is going to start soon. Please join now!"
                            );
                        } catch (Exception e) {
                                System.out.println("Error sending notification: " + e.getMessage());
                            }
                    }
                }
            }

            page++;
        } while (registrationPage.hasNext());
    }


    @Transactional
    public Tournament publishTournament(TournamentRequest tournamentRequest, Long vendorId) throws LimitExceededException {
        try {

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (tournamentRepository.existsByVendorIdAndStatus(vendorId, TournamentStatus.PENDING)) {
                throw new BusinessException("You already have a pending tournament. Please wait for admin approval before publishing another.", HttpStatus.BAD_REQUEST);
            }
            if (vendorEntity.getStatus() != VendorStatus.ACTIVE) {
                throw new BusinessException("Vendor is suspended or blocked. Publishing is not allowed.", HttpStatus.BAD_REQUEST);
            }

            Tournament tournament = new Tournament();

            Optional<AagAvailableGames> gameAvailable = aagGameRepository.findById(tournamentRequest.getExistinggameId());


            AagAvailableGames gameEntity = gameAvailable.orElseThrow(() ->
                    new BusinessException("Game not found with ID: " + tournamentRequest.getExistinggameId(), HttpStatus.NOT_FOUND)
            );

            tournament.setGameUrl(commonService.resolveGameImageUrl(gameEntity,tournamentRequest.getThemeId()));

            ThemeEntity theme = em.find(ThemeEntity.class, tournamentRequest.getThemeId());
            if (theme == null) {
                throw new BusinessException("No theme found with the provided ID" , HttpStatus.BAD_REQUEST);
            }

            Double totalPrize = (double) tournamentRequest.getEntryFee() * tournamentRequest.getParticipants();
            BigDecimal revenueAmmountforuser = BigDecimal.valueOf(totalPrize).multiply(PriceConstant.USER_PRIZE_PERCENT);
            int totalPlayers = tournamentRequest.getParticipants();

            int totalRounds = (int) Math.ceil(Math.log(totalPlayers) / Math.log(2));


//           BigDecimal roomprize = revenueAmmountforuser.divide(BigDecimal.valueOf(totalRounds));
            BigDecimal roomprize = revenueAmmountforuser.divide(
                    BigDecimal.valueOf(totalRounds), 2, RoundingMode.HALF_UP
            );

            // Set Vendor and Theme to the Game
            tournament.setName(gameAvailable.get().getGameName());
            tournament.setVendorId(vendorId);
            tournament.setTotalrounds(totalRounds);
            tournament.setTheme(theme);
            tournament.setExistinggameId(tournamentRequest.getExistinggameId());
            tournament.setTotalPrizePool(totalPrize);
            tournament.setRoomprize(roomprize);
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
                ZonedDateTime requestedTime = tournamentRequest.getScheduledAt()
                        .withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

                int minutes = requestedTime.getMinute();
                int remainder = minutes % 15;
                if (remainder != 0 || requestedTime.getSecond() > 0 || requestedTime.getNano() > 0) {
                    requestedTime = requestedTime
                            .plusMinutes(15 - remainder)
                            .withSecond(0)
                            .withNano(0);
                }

                if (requestedTime.isBefore(nowInKolkata.plusHours(1))) {
                    throw new BusinessException("The game must be scheduled at least 1 hour in advance.", HttpStatus.BAD_REQUEST);
                }

                tournament.setStatus(TournamentStatus.PENDING);
                tournament.setScheduledAt(requestedTime);
            } else {
                throw new BusinessException("Scheduled date is required", HttpStatus.BAD_REQUEST);
            }

            /*ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            if (tournamentRequest.getScheduledAt() != null) {

              ZonedDateTime scheduledInKolkata = tournamentRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

                if (scheduledInKolkata.isBefore(nowInKolkata.plusHours(1))) {
                    throw new BusinessException("The game must be scheduled at least 1 hours in advance." , HttpStatus.BAD_REQUEST);
                }
                ZonedDateTime scheduledInKolkata15 = tournamentRequest.getScheduledAt()
                        .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                        .plusMinutes(15);

                tournament.setStatus(TournamentStatus.PENDING);
                tournament.setScheduledAt(scheduledInKolkata15);

            }else {
               throw new BusinessException("Scheduled date is required" , HttpStatus.BAD_REQUEST);
            }*/


            // Set created and updated timestamps
            tournament.setCreatedDate(nowInKolkata);

            // Save tournament first to get its ID
            Tournament savedTournament = tournamentRepository.save(tournament);



            // Generate a shareable link for the game
            String shareableLink = generateShareableLink(tournament.getId(),vendorId);
            tournament.setShareableLink(shareableLink);
          /*  vendorEntity.setPublishedLimit((vendorEntity.getPublishedLimit() == null ? 0 : vendorEntity.getPublishedLimit()) + 1);
            vendorEntity.setTotal_tournament_published(vendorEntity.getTotal_tournament_published() == null ? 0 : vendorEntity.getTotal_tournament_published() + 1);*/

            // Send notification asynchronously (non-blocking)
            /*CompletableFuture.runAsync(() ->
                    followerNotificationService.notifyFollowersInParallel("tournament", savedTournament.getName(), vendorEntity)
            );*/
            commonService.notifyAdminsByRoleGeneric(Constant.ADMIN_ROLE, savedTournament);

            return savedTournament;

        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;      }
        catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the game: " + e.getMessage(), e);
        }
    }


    @Transactional
    public Tournament createTournamentWithFixedFee(TournamentRequest request, Long vendorId) {
        VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
        AagAvailableGames game = em.find(AagAvailableGames.class, request.getExistinggameId());
        ThemeEntity theme = em.find(ThemeEntity.class, request.getThemeId());

        if (vendorEntity == null || game == null || theme == null) {
            throw new BusinessException("Vendor, Game, or Theme not found.", HttpStatus.NOT_FOUND);
        }

        int entryFee = request.getEntryFee();
        int participants = request.getParticipants();
        int totalRounds = (int) Math.ceil(Math.log(participants) / Math.log(2));

        BigDecimal totalPrize = Constant.TOURNAMENT_PRIZE_POOL;
        BigDecimal userPrizePool = totalPrize.multiply(PriceConstant.USER_PRIZE_PERCENT);
        BigDecimal roomPrize = userPrizePool.divide(BigDecimal.valueOf(totalRounds), 2, RoundingMode.HALF_UP);

        ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime scheduledAt = request.getScheduledAt();

        if (scheduledAt == null) {
            scheduledAt = nowInKolkata.plusHours(Constant.TOURNAMENT_START_TIME);
        }

        // Align time to next 15 min slot
        int minutes = scheduledAt.getMinute();
        int remainder = minutes % 15;
        if (remainder != 0 || scheduledAt.getSecond() > 0 || scheduledAt.getNano() > 0) {
            scheduledAt = scheduledAt
                    .plusMinutes(15 - remainder)
                    .withSecond(0)
                    .withNano(0);
        }

        if (scheduledAt.isBefore(nowInKolkata.plusHours(1))) {
            throw new BusinessException("The game must be scheduled at least 1 hour in advance.", HttpStatus.BAD_REQUEST);
        }


        Tournament tournament = new Tournament();
        tournament.setName(game.getGameName());
        tournament.setVendorId(vendorId);
        tournament.setVendorEntity(vendorEntity);
        tournament.setTheme(theme);
        tournament.setExistinggameId(game.getId());
        tournament.setParticipants(participants);
        tournament.setEntryFee(entryFee);
        tournament.setTotalPrizePool(totalPrize.doubleValue());
        tournament.setRoomprize(roomPrize);
        tournament.setTotalrounds(totalRounds);
        tournament.setGameUrl(commonService.resolveGameImageUrl(game, theme.getId()));
        tournament.setScheduledAt(scheduledAt);
        tournament.setStatus(TournamentStatus.SCHEDULED);
        tournament.setCreatedDate(nowInKolkata);

        if (entryFee > 10) {
            tournament.setMove(Constant.TENMOVES);
        } else {
            tournament.setMove(Constant.SIXTEENMOVES);
        }

        Tournament saved = tournamentRepository.save(tournament);

        // Generate shareable link
        String shareableLink = generateShareableLink(saved.getId(), vendorId);
        saved.setShareableLink(shareableLink);
        tournamentRepository.save(saved);

        vendorEntity.setPublishedLimit((vendorEntity.getPublishedLimit() == null ? 0 : vendorEntity.getPublishedLimit()) + 1);
        vendorEntity.setTotal_tournament_published(vendorEntity.getTotal_tournament_published() == null ? 0 : vendorEntity.getTotal_tournament_published() + 1);
        // Send notification asynchronously (non-blocking)
        vendorRepository.save(vendorEntity);
        CompletableFuture.runAsync(() ->
                followerNotificationService.notifyFollowersInParallel("tournament", tournament.getName(), vendorEntity)
        );


        return saved;
    }



    @Transactional
    public TournamentPlayerRegistration registerPlayer(Long tournamentId, Long playerId) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));

            if(tournament.getStatus() == TournamentStatus.ACTIVE) {
                throw new BusinessException("Tournament is already active" , HttpStatus.BAD_REQUEST);

            }
            BigDecimal entryFeetosent = BigDecimal.valueOf(tournament.getEntryFee()).stripTrailingZeros();
            String feeString = entryFeetosent.toPlainString();

            commonService.deductFromWallet(
                    playerId,
                    (double) tournament.getEntryFee(),
                    "Rs. " + feeString + " deducted for playing " + tournament.getName() + " tournament"
            );


            BigDecimal vendorShareAmount = PriceConstant.VENDOR_REVENUE_PERCENT;
            commonService.addVendorEarningForPayment(tournament.getVendorId(), BigDecimal.valueOf(tournament.getEntryFee()), vendorShareAmount);



            // Check if the tournament has reached the maximum participant limit
            int currentRegistrations = tournamentPlayerRegistrationRepository
                    .countByTournamentIdAndStatus(tournamentId, TournamentPlayerRegistration.RegistrationStatus.REGISTERED);



            // Find the player by ID
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found" , HttpStatus.BAD_REQUEST));

            if(player==null) {
                throw new BusinessException("Player not found" , HttpStatus.BAD_REQUEST);
            }

            Optional<TournamentPlayerRegistration> existingRegistration = tournamentPlayerRegistrationRepository
                    .findByTournamentIdAndPlayer_PlayerId(tournamentId, playerId);

            if (existingRegistration.isPresent()) {
                throw new BusinessException("Player is already registered for this tournament.", HttpStatus.BAD_REQUEST);
            }

            if (currentRegistrations >= tournament.getParticipants()) {
                throw new BusinessException("The tournament has already reached the maximum number of participants.", HttpStatus.BAD_REQUEST);
            }

//            deductAmountFromWalletToRegisterInTournament(playerId, tournamentId);

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
                tournamentRepository.save(tournament);
            }

            commonService.addXpPoints(ActivityType.TOURNAMENT, player);

            return registration;

        }catch (BusinessException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;        }
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
            List<TournamentPlayerRegistration.RegistrationStatus> statuses = Arrays.asList(
                    TournamentPlayerRegistration.RegistrationStatus.REGISTERED,
                    TournamentPlayerRegistration.RegistrationStatus.ACTIVE
            );


            Page<TournamentPlayerRegistration> registrationPage =
                    tournamentPlayerRegistrationRepository.findPlayersByTournamentIdAndStatuses(
                            tournamentId,
                            statuses,
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
/*
            List<TournamentPlayerRegistration> registeredPlayers = tournamentPlayerRegistrationRepository.findByTournamentIdAndStatus(
                    tournamentId, TournamentPlayerRegistration.RegistrationStatus.REGISTERED
            );
*/
            List<TournamentPlayerRegistration> registeredPlayers = tournamentPlayerRegistrationRepository.findByTournamentIdAndStatusIn(
                    tournamentId, Arrays.asList(
                            TournamentPlayerRegistration.RegistrationStatus.REGISTERED,
                            TournamentPlayerRegistration.RegistrationStatus.ACTIVE
                    )
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
    public Page<Tournament> getAllTournaments(Pageable pageable, List<TournamentStatus> statuses, Long vendorId, String gamename) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM tournament t WHERE 1=1");
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM tournament t WHERE 1=1");

            Map<String, Object> params = new HashMap<>();

            if (statuses != null && !statuses.isEmpty()) {
                sql.append(" AND t.status IN :statuses");
                countSql.append(" AND t.status IN :statuses");
//                params.put("statuses", statuses);
                params.put("statuses", statuses.stream().map(Enum::name).collect(Collectors.toList()));

            }

            if (vendorId != null) {
                sql.append(" AND t.vendorId = :vendorId");
                countSql.append(" AND t.vendorId = :vendorId");
                params.put("vendorId", vendorId);
            }

            if (gamename != null && !gamename.trim().isEmpty()) {
                sql.append(" AND LOWER(t.name) LIKE :gamename");
                countSql.append(" AND LOWER(t.name) LIKE :gamename");
                params.put("gamename", "%" + gamename.trim().toLowerCase() + "%");
            }

            sql.append(" ORDER BY t.createddate DESC");

            // Main query
            Query query = em.createNativeQuery(sql.toString(), Tournament.class);
            params.forEach(query::setParameter);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Tournament> tournaments = query.getResultList();

            // Count query
            Query countQuery = em.createNativeQuery(countSql.toString());
            params.forEach(countQuery::setParameter);
            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(tournaments, pageable, total);
        } catch (BusinessException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching tournaments: " + e.getMessage(), e);
        }
    }




    public Page<Tournament> getFilteredTournaments(
            Integer page, Integer size, Sort sort,
            Long id, Long vendorId, String name, BigDecimal totalPrizePool, TournamentStatus status,
            String vendorName, String vendorEmail, String vendorMobile, String search
    ) {
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Tournament> spec = TournamentSpecification.withFilters(id, vendorId, name, totalPrizePool, status, vendorName, vendorEmail, vendorMobile, search);
        return tournamentRepository.findAll(spec, pageable);
    }


/*    @Transactional
    public Page<Tournament> getAllTournaments(Pageable pageable, List<TournamentStatus> statuses, Long vendorId,String gamename) {
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
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching tournaments: " + e.getMessage(), e);
        }
    }*/


    @Transactional
    public Page<Tournament> getAllActiveTournamentsByVendor(Pageable pageable, Long vendorId) {
        try {
            TournamentStatus status = TournamentStatus.ACTIVE;
            if (status != null && vendorId != null) {
                return tournamentRepository.findTournamentByStatusAndVendorId(status, vendorId, pageable);
            } else {
                return tournamentRepository.findAll(pageable);
            }
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Page<Tournament> getAllActiveScheduledTournamentsByVendor(Pageable pageable, Long vendorId) {
        try {
            TournamentStatus status = TournamentStatus.ACTIVE;
            TournamentStatus status1 = TournamentStatus.SCHEDULED;
            List<TournamentStatus> statuses = new ArrayList<>();
            statuses.add(status);
            statuses.add(status1);
            return tournamentRepository.findByStatusInAndVendorId(statuses, vendorId, pageable);

        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leagues: " + e.getMessage(), e);
        }
    }

    @Transactional
    public List<TournamentRoom> getAllRoomsByTournamentId(Long tournamentId) {
        try {
            List<String> statuses = Arrays.asList("ONGOING", "IN_PROGRESS","PLAYING");
            return roomRepository.findByTournamentIdAndStatusIn(tournamentId, statuses);
//            return roomRepository.findByTournamentId(tournamentId);
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
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
            throw e;
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

    private String generateShareableLink(Long gameId,Long vendorId) {
        return "https://backend.aagapp.com/vendor/"+  vendorId  +"/tournament/" + gameId ;
    }

    private void updateTournamentStatus(Tournament tournament, TournamentStatus status, String reason) {
        tournament.setStatus(status);
        tournamentRepository.save(tournament);

        if (status == TournamentStatus.REJECTED || status == TournamentStatus.CANCELLED) {
            String fcmToken = tournament.getVendorEntity().getFcmToken();
            if (fcmToken != null) {
                try {
                    notoficationFirebase.sendNotification(
                            fcmToken,
                            "⚠️ Tournament " + tournament.getName() + " was rejected",
                            "Reason: " + reason
                    );
                } catch (Exception e) {
                    System.out.println("Error sending notification: " + e.getMessage());
                }
            }

        }
    }

    @Transactional
    public Tournament startTournament(Long tournamentId) {


        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new BusinessException("Tournament not found", HttpStatus.BAD_REQUEST));


        if (tournament.getStatus() != TournamentStatus.SCHEDULED) {
            throw new IllegalStateException("Tournament is not in SCHEDULED status");
        }

        List<Player> activePlayers = getActivePlayers(tournamentId);

        if (activePlayers.isEmpty()) {
            updateTournamentStatus(tournament, TournamentStatus.REJECTED, "No active players found");
            return tournament;
        }

        if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);

            BigDecimal entryFeePerUser = BigDecimal.valueOf(tournament.getEntryFee());
            BigDecimal totalCollection = entryFeePerUser;
            BigDecimal userPrizePool = totalCollection.multiply(PriceConstant.USER_PRIZE_PERCENT);
            tournament.setRoomprize(userPrizePool);
            tournament.setTotalPrizePool(totalCollection.doubleValue());
            tournament.setTotalrounds(1);
            tournament.setStatus(TournamentStatus.COMPLETED);
            tournament.setStatusUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
            tournamentRepository.save(tournament);

            distributeRoundPrize(tournament, 1);

            TournamentResultRecord result = new TournamentResultRecord();
            result.setTournament(tournament);
            result.setRoomId(null);
            result.setPlayer(winner);
            result.setScore(0);
            result.setIsWinner(true);
            result.setStatus("WINNER");
            result.setRound(1);
            result.setPlayedAt(LocalDateTime.now());
            tournamentResultRecordRepository.save(result);

            Notification notification = new Notification();
            notification.setAmount(userPrizePool.doubleValue());
//            notification.setDetails("You won ₹ " + userPrizePool + " in Round 1");
            String prizeStr = userPrizePool.stripTrailingZeros().toPlainString();
            notification.setDetails("You won ₹ " + prizeStr + " in Round 1");

            notification.setDescription("Round Prize");
            notification.setRole("Customer");
            notification.setCustomerId(winner.getCustomer().getId());
            notification.setName(winner.getCustomer().getName()!=null?winner.getCustomer().getName():"N/A");

            notificationRepository.save(notification);

            String fcmToken = tournament.getVendorEntity().getFcmToken();
            if (fcmToken != null) {
                try{
                    notoficationFirebase.sendNotification(
                            fcmToken,
                            "⚠️ Tournament " + tournament.getName() + " was concluded",
                            "⚠️ Tournament " + tournament.getName() + " was concluded. " +
                                    "User " + winner.getPlayerId() + " is the winner with prize: " + userPrizePool);
                }catch (Exception e){
                    System.out.println("Error sending notification: " + e.getMessage());
                }
            }

            return tournament;
        }

        if (activePlayers.size() < 2) {
            throw new IllegalStateException("Not enough players to start the tournament (minimum 2 needed)");
        }

        Collections.shuffle(activePlayers);

        int totalPlayers = activePlayers.size();

        int freePassCount = activePlayers.size() % 2;
        int totalRounds = (int) Math.ceil(Math.log(totalPlayers + freePassCount) / Math.log(2));
        tournament.setTotalrounds(totalRounds);

        tournament.setStatus(TournamentStatus.ACTIVE);
        tournament.setStatusUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

        tournamentRepository.save(tournament);
        for (int i = 0; i < activePlayers.size(); i += 2) {
            if (i + 1 < activePlayers.size()) {
                TournamentRoom room = new TournamentRoom();
                room.setTournament(tournament);
                room.setMaxParticipants(2);
                room.setCurrentParticipants(0);
                room.setStatus("IN_PROGRESS");
                room.setRound(1);
                roomRepository.save(room);

                String gameName = tournament.getName().toLowerCase();
                String gamePassword;

                if (gameName.equals("ludo")) {
                    gamePassword = this.createNewGame(Constant.ludobaseurl, tournament.getId(), room.getId(),
                            room.getMaxParticipants(), tournament.getMove(), tournament.getRoomprize());
                } else if (gameName.equals("snake & ladder")) {
                    gamePassword = this.createNewGame(Constant.snakebaseUrl, tournament.getId(), room.getId(),
                            room.getMaxParticipants(), tournament.getMove(), tournament.getRoomprize());
                } else {
                    throw new BusinessException("Unsupported game: " + gameName, HttpStatus.BAD_REQUEST);
                }

                room.setGamepassword(gamePassword);
                roomRepository.save(room);

                assignPlayerToSpecificRoom(activePlayers.get(i), tournamentId, room);
                assignPlayerToSpecificRoom(activePlayers.get(i + 1), tournamentId, room);
            } else {
                assignFreePassToPlayer(activePlayers.get(i), tournamentId, 1);
            }
        }

        String fcmToken = tournament.getVendorEntity().getFcmToken();
        if (fcmToken != null) {
            try {
                String message = String.format(
                        "🎉 Tournament '%s' is now live!\n" +
                                "👥 Active Players: %d\n" +
                                "🎟️ Free Pass Given: %d\n" +
                                "🔁 Total Rounds: %d\n" +
                                "💰 Total Prize Pool: ₹%.2f\n" +
                                "🏆 Round Prize: ₹%.2f\n" +
                                "Monitor the progress and enjoy the event!",
                        tournament.getName(),
                        activePlayers.size(),
                        freePassCount,
                        totalRounds,
                        tournament.getTotalPrizePool()
                );

                notoficationFirebase.sendNotification(
                        fcmToken,
                        "🎉 Your Tournament Has Begun!",
                        message
                );
            } catch (Exception e) {
                // Skip error silently or log for debugging (optional)
                System.out.println("Error sending notification: " + e.getMessage());
            }
        }


        return tournament;
    }



   public void assignFreePassToPlayer(Player player, Long tournamentId, Integer roundNumber) {
        Optional<TournamentResultRecord> alreadyExists = tournamentResultRecordRepository
                .findByTournamentIdAndPlayerIdAndRound(tournamentId, player.getPlayerId(), roundNumber);


            System.out.println("No existing result found. Assigning FREE_PASS to player ID: " + player.getPlayerId());

            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new RuntimeException("Tournament not found with ID: " + tournamentId));

            TournamentResultRecord result = new TournamentResultRecord();
            result.setTournament(tournament);
            result.setRoomId(null);
            result.setPlayer(player);
            result.setScore(0);
            result.setIsWinner(true);
            result.setStatus("FREE_PASS");
            result.setRound(roundNumber);
            result.setPlayedAt(LocalDateTime.now());

            tournamentResultRecordRepository.save(result);

    }
/*    public boolean assignPlayerToSpecificRoom(Player player, Long tournamentId, TournamentRoom room) {
        if (player.getTournamentRoom() != null) return false;

        if (room.getCurrentParticipants() >= room.getMaxParticipants()) return false;

        player.setTournamentRoom(room);
        room.setCurrentParticipants(room.getCurrentParticipants() + 1);
        room.setStatus("PLAYING");
        playerRepository.save(player);
        roomRepository.save(room);
        return true;
    }*/


    public boolean assignPlayerToSpecificRoom(Player player, Long tournamentId, TournamentRoom room) {

        if (player.getTournamentRoom() != null){
            return false;
        }
        player.setTournamentRoom(room);
        room.setCurrentParticipants(room.getCurrentParticipants() + 1);
        room.setStatus("PLAYING");
        playerRepository.save(player);
        roomRepository.save(room);
        return true;
    }

    @Transactional
    public ResponseEntity<?> leaveRoom(Long playerId, Long tournamentId) {
        try {

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found with ID: " + tournamentId, HttpStatus.BAD_REQUEST));

            TournamentRoom tournamentRoom = player.getTournamentRoom();

            if (tournamentRoom == null) {
                return responseService.generateErrorResponse("Player is not in any Tournament Room", HttpStatus.BAD_REQUEST);
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

            // Remove player from room
            player.setTournamentRoom(null);
            playerRepository.save(player);

            // Now check if room is empty
            List<Player> updatedRemainingPlayers = playerRepository.findAllByTournamentRoom(tournamentRoom);

            if (updatedRemainingPlayers.isEmpty()) {
                tournamentRoom.setStatus("COMPLETED");
                roomRepository.save(tournamentRoom);
            }

            return responseService.generateSuccessResponse("Player left the Tournament Room", tournament.getName(), HttpStatus.OK);

        } catch (BusinessException ex) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, ex);
            throw ex;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player cannot leave the room because: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


/*    @Transactional
    public ResponseEntity<?> leaveRoom(Long playerId, Long tournamentId) {
        try {
            System.out.println("leaveRoom"+playerId + " " + tournamentId);

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId , HttpStatus.BAD_REQUEST));

            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found with ID: " + tournamentId , HttpStatus.BAD_REQUEST));

            TournamentRoom tournamentRoom = player.getTournamentRoom();

            if (tournamentRoom == null) {
                return responseService.generateErrorResponse("Player is not in any Tournament Room", HttpStatus.BAD_REQUEST);
            }

            List<Player> remainingPlayers = playerRepository.findAllByTournamentRoom(tournamentRoom);

            System.out.println("remainingPlayers_______"+remainingPlayers.size());

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

            player.setTournamentRoom(null);
            playerRepository.save(player);

            return responseService.generateSuccessResponse("Player left the Tournament Room", tournament.getName(), HttpStatus.OK);

        }catch (BusinessException ex) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, ex);
            throw ex;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Player cannot leave the room because: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
    private TournamentRoom convertTournamentResultToRoom(TournamentResultRecord result) {
        TournamentRoom room = new TournamentRoom();
        room.setMaxParticipants(2); // Assuming a 2-player room, you can adjust this logic if needed
        room.setGamepassword(null); // Set the game password or leave it as null if not applicable
        room.setTournament(result.getTournament());
        // Wrap the single Player into a List
        room.setCurrentPlayers(Collections.singletonList(result.getPlayer())); // Wrap single player in a List

        room.setStatus("FREE_PASS");
        return room;
    }

    @Transactional
    public TournamentRoom getMyRoomDetails(Long playerId, Long tournamentId) {


        TournamentRoom room = roomRepository.findRoomByPlayerIdAndTournamentId(playerId, tournamentId);

        if (room != null) {
            return room;
        }

        // Check if the player has a result record instead (could be a free pass)
        List<TournamentResultRecord> records = tournamentResultRecordRepository
                .findAllByPlayerIdAndTournamentIdOrderByIdDesc(playerId, tournamentId);

        if (records.isEmpty()) {
            throw new BusinessException("No tournament result found for player in this tournament.", HttpStatus.BAD_REQUEST);
        }

        TournamentResultRecord latestRecord = records.get(0);

        // If the result was a free pass (or any record not associated with a room)
        return convertTournamentResultToRoom(latestRecord);
    }


/*    @Transactional
    public TournamentRoom getMyRoomDetails(Long playerId, Long tournamentId) {

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new BusinessException("Player not found with ID: " + playerId, HttpStatus.BAD_REQUEST));

            TournamentRoom room = player.getTournamentRoom();
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

    }*/


public TournamentResultRecord addPlayerToNextRound(Long tournamentId, Integer roundNumber, TournamentResultRecord record) {

    Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new BusinessException("Tournament not found", HttpStatus.BAD_REQUEST));

    Player player = record.getPlayer();

    // === Check if record already exists ===
    Optional<TournamentResultRecord> existingRecord = tournamentResultRecordRepository
            .findByTournamentIdAndPlayerIdAndRound(tournamentId, player.getPlayerId(), roundNumber);

    if (existingRecord.isPresent()) {
        return existingRecord.get();
    }

    TournamentResultRecord nextRoundRecord = new TournamentResultRecord();
    nextRoundRecord.setTournament(tournament);
    nextRoundRecord.setPlayer(player);
    nextRoundRecord.setRound(roundNumber);
    nextRoundRecord.setStatus("READY_TO_PLAY");
    nextRoundRecord.setScore(0);

    return tournamentResultRecordRepository.save(nextRoundRecord);
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
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while retrieving players for tournament " + tournamentId + " and round " + roundNumber, e);
        }
    }

    public String createNewGame(String baseUrl, Long gameId, Long roomId, Integer players, Integer move, BigDecimal prize) {
        try {
            // Construct the URL with query parameters
            String url = baseUrl + "/CreateNewGame?gametype=TOURNAMENT"
                    + "&gameid=" + gameId
                    + "&roomid=" + roomId
                    + "&players=" + players
                    + "&prize=" + prize
                    + "&moves=" + move;

            System.out.println("Request URL: " + url);

            // Set Authorization and Content-Type headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer UFBZINFPQQPQ6RZ6Z5BFCI8K");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Send POST request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Parse the response
            String responseBody = response.getBody();
            System.out.println("Response Body: " + responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            String status = jsonResponse.path("Status").asText(null);

            if (status == null) {
                throw new BusinessException("Invalid response: 'Status' field missing", HttpStatus.BAD_REQUEST);
            }

            if ("SUCCESS".equalsIgnoreCase(status)) {
                // If response is SUCCESS, return GamePassword
                if (jsonResponse.has("GamePassword")) {
                    String gamePassword = jsonResponse.get("GamePassword").asText();
                    System.out.println("Game Password: " + gamePassword);
                    return gamePassword;
                } else {
                    throw new BusinessException("GamePassword missing in SUCCESS response", HttpStatus.BAD_REQUEST);
                }
            } else if ("Error".equalsIgnoreCase(status)) {
                // Handle API-level error with reason
                String reason = jsonResponse.path("Reason").asText("Unknown error occurred");
                throw new BusinessException("Game creation failed: " + reason, HttpStatus.BAD_REQUEST);
            } else {
                throw new BusinessException("Unexpected Status value: " + status, HttpStatus.BAD_REQUEST);
            }

        } catch (BusinessException e) {
            // Handle known business exception (e.g. validation, game already exists)
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        } catch (Exception e) {
            // Handle other unexpected errors
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while creating the game on the server: " + e.getMessage(), e);
        }
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
        tournament.setStatusUpdatedAt(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));
        tournamentRepository.save(tournament);


        String fcmToken = tournament.getVendorEntity().getFcmToken();
        if (fcmToken != null) {
            try{
                notoficationFirebase.sendNotification(
                        fcmToken,
                        "🏆 Tournament " + tournament.getName() + " has been Completed now",
                        "The tournament has successfully concluded. Check final results and prize distribution!"
                );
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    public void setvendorShare(Tournament tournament) {
        BigDecimal totalWinningAmount = BigDecimal.valueOf(tournament.getTotalPrizePool());
        BigDecimal vendorShareAmount = totalWinningAmount.multiply(PriceConstant.VENDOR_REVENUE_PERCENT);

        VendorEntity vendor = tournament.getVendorEntity();
        VendorWallet wallet = vendor.getWallet();

        if (wallet == null) {
            VendorWallet vendorWallet = new VendorWallet();
            vendorWallet.setVendorEntity(vendor);
            vendorWallet.setWinningAmount(vendorShareAmount);
            walletRepo.save(vendorWallet);

            vendor.setWallet(vendorWallet);
            vendorRepository.save(vendor);
        } else {
            wallet.setWinningAmount(wallet.getWinningAmount().add(vendorShareAmount));
            walletRepo.save(wallet);
        }
    }
    @Transactional
    public void processMatchResults(GameResult gameResult) {
        List<PlayerDtoWinner> players = gameResult.getPlayers();

        if (players == null || players.isEmpty()) {
            return;
        }

        // Filter valid players (non-zero IDs)
        List<PlayerDtoWinner> validPlayers = players.stream()
                .filter(p -> p.getPlayerId() != 0)
                .collect(Collectors.toList());

        if (validPlayers.isEmpty()) {
            return;
        }

        // If still <2 valid players, fetch from room
        if (validPlayers.size() < 2) {
            List<Player> roomPlayers = playerRepository.findByTournamentRoom_Id(gameResult.getRoomId());
            if (roomPlayers.isEmpty()) {
                return;
            }

            for (Player roomPlayer : roomPlayers) {
                boolean alreadyIncluded = validPlayers.stream()
                        .anyMatch(vp -> vp.getPlayerId().equals(roomPlayer.getPlayerId()));
                if (!alreadyIncluded) {
                    validPlayers.add(new PlayerDtoWinner(roomPlayer.getPlayerId(), 0)); // default score = 0
                }
                if (validPlayers.size() >= 2) {
                    break;
                }
            }

        }


        if (validPlayers.isEmpty()) {

            List<Player> roomPlayers = playerRepository.findByTournamentRoom_Id(gameResult.getRoomId());
            Tournament tournament = tournamentRepository.findById(gameResult.getGameId())
                    .orElseThrow(() -> new BusinessException("Game not found", HttpStatus.BAD_REQUEST));

            for (Player p : roomPlayers) {
                leaveRoom(p.getPlayerId(), tournament.getId());
            }
            return;

        }

        Tournament tournament = tournamentRepository.findById(gameResult.getGameId())
                .orElseThrow(() -> new BusinessException("Game not found", HttpStatus.BAD_REQUEST));

        // === 1 PLAYER ===
        if (validPlayers.size() == 1) {
            PlayerDtoWinner soleWinner = validPlayers.get(0);

            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), soleWinner, true);
            leaveRoom(soleWinner.getPlayerId(), tournament.getId());

            int currentRound = tournament.getRound();
            startNextRound(tournament.getId(), currentRound);
            return;
        }

        // === 2 or more players ===

        // Find highest score
        int maxScore = validPlayers.stream()
                .mapToInt(PlayerDtoWinner::getScore)
                .max()
                .orElseThrow(() -> new RuntimeException("Unable to determine max score"));

        // Collect winners (players with max score)
        List<PlayerDtoWinner> winners = validPlayers.stream()
                .filter(p -> p.getScore() == maxScore)
                .collect(Collectors.toList());

        // Losers = everyone else
        List<PlayerDtoWinner> losers = validPlayers.stream()
                .filter(p -> p.getScore() < maxScore)
                .collect(Collectors.toList());


        // === Save WINNERS ===
        for (PlayerDtoWinner winner : winners) {

            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), winner, true);
            leaveRoom(winner.getPlayerId(), tournament.getId());
        }

        // === Save LOSERS ===
        for (PlayerDtoWinner loser : losers) {
            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), loser, false);
            leaveRoom(loser.getPlayerId(), tournament.getId());
        }



        int currentRound = tournament.getRound();
        startNextRound(tournament.getId(), currentRound);
    }

/*    @Transactional
    public void processMatchResults(GameResult gameResult) {
        List<PlayerDtoWinner> players = gameResult.getPlayers();

        System.out.println("[INFO] Processing GameId: " + gameResult.getGameId());

        if (players == null || players.isEmpty()) {
            System.out.println("[WARN] Players list is null or empty. Skipping processing.");
            return;
        }

        // Filter valid players (non-zero IDs)
        List<PlayerDtoWinner> validPlayers = players.stream()
                .filter(p -> p.getPlayerId() != 0)
                .collect(Collectors.toList());

        if (validPlayers.isEmpty()) {
            System.out.println("[WARN] No valid players (non-zero). Skipping processing.");
            return;
        }

        // If still <2 valid players, fetch from room
        if (validPlayers.size() < 2) {
            List<Player> roomPlayers = playerRepository.findByTournamentRoom_Id(gameResult.getRoomId());
            if (roomPlayers.isEmpty()) {
                System.out.println("[WARN] No players found in room. Skipping processing.");
                return;
            }

            // Add room players who are not already in validPlayers
            for (Player roomPlayer : roomPlayers) {
                boolean alreadyIncluded = validPlayers.stream()
                        .anyMatch(vp -> vp.getPlayerId().equals(roomPlayer.getPlayerId()));
                if (!alreadyIncluded) {
                    validPlayers.add(new PlayerDtoWinner(roomPlayer.getPlayerId(), 0)); // default score = 0
                }
                if (validPlayers.size() >= 2) {
                    break;
                }
            }

            System.out.println("[INFO] After room assignment, valid players: " + validPlayers.stream()
                    .map(p -> p.getPlayerId() + "")
                    .collect(Collectors.joining(", ")));
        }

        if (validPlayers.isEmpty()) {
            System.out.println("[WARN] No valid players after room fallback. Skipping processing.");
            return;
        }

        Tournament tournament = tournamentRepository.findById(gameResult.getGameId())
                .orElseThrow(() -> new BusinessException("Game not found", HttpStatus.BAD_REQUEST));

        if (validPlayers.size() == 1) {
            // Single player is automatically winner
            PlayerDtoWinner winner = validPlayers.get(0);

            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), winner, true);
            leaveRoom(winner.getPlayerId(), tournament.getId());

            int currentRound = tournament.getRound();
            startNextRound(tournament.getId(), currentRound);
            return;
        }

        // Proceed with standard winner/loser logic (2 players)
        PlayerDtoWinner player1 = validPlayers.get(0);
        PlayerDtoWinner player2 = validPlayers.get(1);

        if (player1.getScore() > player2.getScore()) {
            // Normal winner-loser
            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), player1, true);
            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), player2, false);

            leaveRoom(player1.getPlayerId(), tournament.getId());
            leaveRoom(player2.getPlayerId(), tournament.getId());

        } else if (player2.getScore() > player1.getScore()) {
            // Normal winner-loser (reverse)
            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), player2, true);
            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), player1, false);

            leaveRoom(player2.getPlayerId(), tournament.getId());
            leaveRoom(player1.getPlayerId(), tournament.getId());

        } else {
            // TIE case → both are winners
            System.out.println("[INFO] Scores are tied! Declaring both as winners.");

            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), player1, true);
            storeMatchResult(gameResult.getGameId(), gameResult.getRoomId(), player2, true);

            leaveRoom(player1.getPlayerId(), tournament.getId());
            leaveRoom(player2.getPlayerId(), tournament.getId());
        }

        int currentRound = tournament.getRound();
        startNextRound(tournament.getId(), currentRound);
    }*/




/*    @Transactional
    public void processMatchResults(GameResult gameResult) {
            List<PlayerDtoWinner> players = gameResult.getPlayers();

            System.out.println("Players: " + gameResult.getGameId());

            // Check if players list is null or empty
            if (players == null || players.size() < 2) {
                throw new BusinessException("Players list is null or doesn't contain enough players" , HttpStatus.BAD_REQUEST);
            }

            PlayerDtoWinner player1 = players.get(0);
            PlayerDtoWinner player2 = players.get(1);

        if (player1.getPlayerId() == 0 || player2.getPlayerId() == 0) {
            List<Player> roomPlayers = playerRepository.findByTournamentRoom_Id(gameResult.getRoomId());
            if (roomPlayers.size() < 2) {
                throw new BusinessException("Room does not have enough players", HttpStatus.BAD_REQUEST);
            }

            Player roomPlayer1 = roomPlayers.get(0);
            Player roomPlayer2 = roomPlayers.get(1);

            player1 = new PlayerDtoWinner(roomPlayer1.getPlayerId(),player1.getScore() );
            player2 = new PlayerDtoWinner(roomPlayer2.getPlayerId(), player2.getScore());

            System.out.println("Reassigned players from room: " + player1.getPlayerId() + " , " + player2.getPlayerId());
        }

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

            leaveRoom(winner.getPlayerId(), tournament.getId());

            leaveRoom(loser.getPlayerId(), tournament.getId());

            int currentRound = tournament.getRound();
            startNextRound( tournament.getId(),  currentRound);

    }*/

    public void startNextRound(Long tournamentId, int currentRound) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found" , HttpStatus.BAD_REQUEST));

            if (isRoundCompleted(tournamentId, currentRound)) {
                distributeRoundPrize(tournament, currentRound);

                long freePassCount = tournamentResultRecordRepository
                        .countByTournamentIdAndRoundAndStatus(tournamentId, currentRound, "FREE_PASS");

                long winnerCount = tournamentResultRecordRepository
                        .countByTournamentIdAndRoundAndStatus(tournamentId, currentRound, "WINNER");


                if (freePassCount == 0 && winnerCount == 1) {
                    finishTournament(tournamentId);

                    System.out.println("🏆 Tournament finished! Only one winner remains.");
                    return;
                }
            } else {
                System.out.println("⏳ Round " + currentRound + " is not completed yet.");
            }
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;

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
        BigDecimal roundPrize = totalPrize;

        List<TournamentResultRecord> winners = tournamentResultRecordRepository
                .findByTournamentIdAndRoundAndIsWinnerTrue(tournament.getId(), round);

        Map<Long, TournamentResultRecord> uniqueWinnersMap = winners.stream()
                .collect(Collectors.toMap(
                        w -> w.getPlayer().getPlayerId(),
                        w -> w,
                        (existing, duplicate) -> existing
                ));

        List<TournamentResultRecord> uniqueWinners = new ArrayList<>(uniqueWinnersMap.values());

        int winnersCount = uniqueWinners.size();

        BigDecimal prizePerWinner = roundPrize.divide(BigDecimal.valueOf(winnersCount), 2, RoundingMode.HALF_UP);

        BigDecimal entryFee = BigDecimal.valueOf(tournament.getEntryFee());
        int totalPlayersInRound = uniqueWinnersMap.size() * 2;

        BigDecimal bonusCollected = entryFee.multiply(BigDecimal.valueOf(Constant.BONUS_PERCENT))
                .multiply(BigDecimal.valueOf(totalPlayersInRound));
        BigDecimal bonusPool = bonusCollected.multiply(BigDecimal.valueOf(2));
        BigDecimal bonusPerWinner = bonusPool.divide(BigDecimal.valueOf(winnersCount), 2, RoundingMode.HALF_UP);

        BigDecimal finalPayoutPerWinner = prizePerWinner.add(bonusPerWinner);


        for (TournamentResultRecord winner : uniqueWinners) {
            Notification notification = new Notification();
            notification.setAmount(finalPayoutPerWinner.doubleValue());
//            notification.setDetails("You won Rs. " + finalPayoutPerWinner + " in Round " + round);
            String payoutStr = finalPayoutPerWinner.stripTrailingZeros().toPlainString();
            notification.setDetails("You won Rs. " + payoutStr + " in Round " + round);

            notification.setDescription("Round Prize");
            notification.setRole("Customer");
            notification.setCustomerId(winner.getPlayer().getCustomer().getId());
            notification.setName(winner.getPlayer().getCustomer().getName()!=null?winner.getPlayer().getCustomer().getName():"N/A");

            notificationRepository.save(notification);

            Wallet wallet = walletRepository.findByCustomCustomer_Id(winner.getPlayer().getCustomer().getId());

            if(wallet.getWinningAmount() == null){
                wallet.setWinningAmount(BigDecimal.ZERO);
            }
            wallet.setWinningAmount(wallet.getWinningAmount().add(prizePerWinner));
            walletRepository.save(wallet);

            winner.setAmmount(
                    (winner.getAmmount() != null ? winner.getAmmount() : BigDecimal.ZERO).add(prizePerWinner)
            );

            CustomCustomer customCustomer = winner.getPlayer().getCustomer();

            BigDecimal currentBonus = customCustomer.getBonusBalance();
            if (currentBonus == null) {
                currentBonus = BigDecimal.ZERO;
            }

            customCustomer.setBonusBalance(currentBonus.add(bonusPerWinner));

            customCustomerRepository.save(customCustomer);

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

/*        room.setStatus("COMPLETED");
        roomRepository.save(room);*/

    }

    public boolean isRoundCompleted(Long tournamentId, int roundNumber) {

        long validRoomsCount = roomRepository.countByTournamentIdAndRoundAndCurrentParticipantsGreaterThan(
                tournamentId, roundNumber, 0);

        if (validRoomsCount == 0) {
            long freePassCount = tournamentResultRecordRepository
                    .countByTournamentIdAndRoundAndStatus(tournamentId, roundNumber, "FREE_PASS");
            return freePassCount > 0;
        }

        long completedRoomsCount = roomRepository.countByTournamentIdAndRoundAndStatusAndCurrentParticipantsGreaterThan(
                tournamentId, roundNumber, "COMPLETED", 0);

        return completedRoomsCount == validRoomsCount;
    }

/*    public boolean isRoundCompleted(Long tournamentId, int roundNumber) {
        // 1. Count all rooms (regardless of players)
        long totalRooms = roomRepository.countByTournamentIdAndRound(tournamentId, roundNumber);

        // 2. If no rooms created at all, check if only FREE_PASS players exist
        if (totalRooms == 0) {
            long freePassCount = tournamentResultRecordRepository
                    .countByTournamentIdAndRoundAndStatus(tournamentId, roundNumber, "FREE_PASS");
            return freePassCount > 0;
        }

        // 3. Count valid rooms where at least 1 player joined
        long validRoomsCount = roomRepository.countByTournamentIdAndRoundAndCurrentParticipantsGreaterThan(
                tournamentId, roundNumber, 0);

        // 4. If no valid rooms, i.e. all rooms are empty (0 participants), treat it as completed
        if (validRoomsCount == 0) {
            long freePassCount = tournamentResultRecordRepository
                    .countByTournamentIdAndRoundAndStatus(tournamentId, roundNumber, "FREE_PASS");

            return freePassCount > 0 || true; // either free pass or everything is empty = safe to continue
        }

        // 5. Count only those valid rooms that are completed
        long completedRoomsCount = roomRepository.countByTournamentIdAndRoundAndStatusAndCurrentParticipantsGreaterThan(
                tournamentId, roundNumber, "COMPLETED", 0);

        // 6. Round is completed only if all valid rooms are completed
        return completedRoomsCount == validRoomsCount;
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
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found", HttpStatus.BAD_REQUEST));

            List<TournamentResultRecord> participants = tournamentResultRecordRepository
                    .findByTournamentIdAndRoundAndStatus(tournamentId, roundNumber, "READY_TO_PLAY");

            if (participants.size() < 2) {
                throw new IllegalStateException("Not enough players to start the round (minimum 2 needed)");
            }

            Collections.shuffle(participants);

            for (int i = 0; i < participants.size(); i += 2) {
                TournamentResultRecord p1 = participants.get(i);
                Player player1 = p1.getPlayer();
                if (i + 1 >= participants.size()) {
                    p1.setStatus("FREE_PASS");
                    p1.setIsWinner(true);
                    tournamentResultRecordRepository.save(p1);
                    break;
                }

                TournamentResultRecord p2 = participants.get(i + 1);
                Player player2 = p2.getPlayer();

                TournamentRoom room = new TournamentRoom();
                room.setTournament(tournament);
                room.setRound(roundNumber);
                room.setMaxParticipants(2);
                room.setCurrentParticipants(0);
                room.setStatus("OPEN");

                // Save room after setting all fields
                roomRepository.save(room);

                assignPlayerToSpecificRoom(player1, tournamentId, room);
                assignPlayerToSpecificRoom(player2, tournamentId, room);

                String gameName = tournament.getName().toLowerCase();
                String gamePassword;

                if (gameName.equals("ludo")) {
                    gamePassword = this.createNewGame(Constant.ludobaseurl, tournament.getId(), room.getId(),
                            room.getMaxParticipants(), tournament.getMove(), tournament.getRoomprize());
                } else if (gameName.equals("snake & ladder")) {
                    gamePassword = this.createNewGame(Constant.snakebaseUrl, tournament.getId(), room.getId(),
                            room.getMaxParticipants(), tournament.getMove(), tournament.getRoomprize());
                } else {
                    throw new BusinessException("Unsupported game: " + gameName, HttpStatus.BAD_REQUEST);
                }

                room.setGamepassword(gamePassword);
//                room.setStatus("IN_PROGRESS");
                roomRepository.save(room); // Final update with game password

                // Save updated player status
                tournamentResultRecordRepository.save(p1);
                tournamentResultRecordRepository.save(p2);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new BusinessException("Error during match processing", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public void processNextRoundMatchesOld(Long tournamentId, Integer roundNumber) {
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

                        String gameName = tournament.getName().toLowerCase();
                        String gamePassword = null;

                        if (gameName.equals("ludo")) {
                            gamePassword = this.createNewGame(Constant.ludobaseurl, tournament.getId(), room.getId(),
                                    room.getMaxParticipants(), tournament.getMove(), tournament.getRoomprize());
                        } else if (gameName.equals("snake & ladder")) {
                            gamePassword = this.createNewGame(Constant.snakebaseUrl, tournament.getId(), room.getId(),
                                    room.getMaxParticipants(), tournament.getMove(), tournament.getRoomprize());
                        } else {
                            throw new BusinessException("Unsupported game: " + gameName, HttpStatus.BAD_REQUEST);
                        }

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
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament not found", HttpStatus.BAD_REQUEST));

            int totalPlayers = tournament.getParticipants();
            BigDecimal totalPrizePool = BigDecimal.valueOf(tournament.getTotalPrizePool());
            BigDecimal remainingPrize = totalPrizePool;

            List<Map<String, Object>> rounds = new ArrayList<>();
            int roundNumber = 1;
            int currentPlayers = totalPlayers;

            // Calculate total rounds
            int totalRounds = (int) (Math.log(totalPlayers) / Math.log(2));

            while (currentPlayers >= 2 && roundNumber <= totalRounds) {
                int winners = currentPlayers / 2;

                Map<String, Object> roundInfo = new HashMap<>();
                roundInfo.put("round", roundNumber + " Round");
                roundInfo.put("progress", currentPlayers == 2 ? "WINNER" : "Completed");
                roundInfo.put("numberOfWinners", currentPlayers == 2 ? "1" : String.valueOf(winners));
                roundInfo.put("totalPlayers", String.valueOf(currentPlayers));

                // Calculate prize for this round
                BigDecimal prizeForThisRound = getDynamicPrize(currentPlayers, tournament, remainingPrize);
                roundInfo.put("prize", prizeForThisRound);

                // Deduct the prize distributed for this round
                remainingPrize = remainingPrize.subtract(prizeForThisRound);

                // Ensure round 6 exists, even if the prize is exhausted
                if (remainingPrize.compareTo(BigDecimal.ZERO) <= 0 && roundNumber < totalRounds) {
                    // If remaining prize is 0 or less, just show zero prize for the final round
                    roundInfo.put("prize", BigDecimal.ZERO);
                    rounds.add(0, roundInfo);
                    break;
                }

                rounds.add(0, roundInfo);

                currentPlayers = winners;
                roundNumber++;
            }

            // Ensure the final round (e.g. round 6) is included in the list
            if (roundNumber <= totalRounds) {
                Map<String, Object> finalRoundInfo = new HashMap<>();
                finalRoundInfo.put("round", roundNumber + " Round");
                finalRoundInfo.put("progress", "WINNER");
                finalRoundInfo.put("numberOfWinners", "1");
                finalRoundInfo.put("totalPlayers", "2");
                finalRoundInfo.put("prize", remainingPrize.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : remainingPrize);
                rounds.add(finalRoundInfo);
            }

            return rounds;

        } catch (BusinessException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException(e);
        }
    }

    private BigDecimal getDynamicPrize(int currentPlayers, Tournament tournament, BigDecimal remainingPrize) {
        int winners = currentPlayers / 2;
        // If only one player is left, give them the remaining prize
        if (winners == 0) {
            return remainingPrize;
        }

        BigDecimal winnersBD = BigDecimal.valueOf(winners);
        BigDecimal roomPrize = tournament.getRoomprize(); // must be BigDecimal

        BigDecimal prizeForThisRound = roomPrize.divide(winnersBD, 2, RoundingMode.HALF_UP);

        return prizeForThisRound.min(remainingPrize);
    }




    public Page<TournamentResultRecordDTO> findTournamentResultsByUserId(Long userId, Pageable pageable, String tournamentName, Boolean winner) {
        try {
            StringBuilder sql = new StringBuilder("""
            SELECT trr.* FROM tournament_result_record trr
            JOIN tournament t ON t.id = trr.tournament_id
            WHERE trr.player_id = :userId
        """);

            StringBuilder countSql = new StringBuilder("""
            SELECT COUNT(*) FROM tournament_result_record trr
            JOIN tournament t ON t.id = trr.tournament_id
            WHERE trr.player_id = :userId
        """);

            if (tournamentName != null && !tournamentName.isEmpty()) {
                sql.append(" AND LOWER(t.name) LIKE LOWER(:tournamentName)");
                countSql.append(" AND LOWER(t.name) LIKE LOWER(:tournamentName)");
            }

            if (winner != null) {
                sql.append(" AND trr.iswinner = :winner");
                countSql.append(" AND trr.iswinner = :winner");
            }

            sql.append(" ORDER BY trr.updateddate DESC");

            Query query = em.createNativeQuery(sql.toString(), TournamentResultRecord.class);
            setParametersForTournament(query, userId, tournamentName, winner);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<TournamentResultRecord> records = query.getResultList();

            Query countQuery = em.createNativeQuery(countSql.toString());
            setParametersForTournament(countQuery, userId, tournamentName, winner);
            Long total = ((Number) countQuery.getSingleResult()).longValue();

            List<TournamentResultRecordDTO> dtoList = records.stream().map(record -> {
                TournamentResultRecordDTO dto = new TournamentResultRecordDTO();
                dto.setId(record.getId());
                dto.setRoomId(record.getRoomId());
                dto.setTournamentName(record.getTournament().getName());
                dto.setPlayerName(record.getPlayer().getCustomer().getName());
                dto.setPlayerProfilePic(record.getPlayer().getPlayerProfilePic());
                dto.setScore(record.getScore());
                dto.setAmount(record.getAmmount());
                dto.setIsWinner(Boolean.TRUE.equals(record.getIsWinner()) ? "true" : "false");
                dto.setRound(record.getRound());
                dto.setPlayedAt(record.getPlayedAt());
                return dto;
            }).toList();

            return new PageImpl<>(dtoList, pageable, total);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching tournament results by user: " + e.getMessage(), e);
        }
    }

    private void setParametersForTournament(Query query, Long userId, String tournamentName, Boolean winner) {
        query.setParameter("userId", userId);
        if (tournamentName != null && !tournamentName.isEmpty()) {
            query.setParameter("tournamentName", "%" + tournamentName + "%");
        }
        if (winner != null) {
            query.setParameter("winner", winner);
        }
    }

    @Transactional
    public String manualStartNextRound(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new BusinessException("Tournament not found", HttpStatus.BAD_REQUEST));

        int currentRound = tournament.getRound();

        if (!isRoundCompleted(tournamentId, currentRound)) {
            throw new BusinessException("Round " + currentRound + " is not completed yet.", HttpStatus.BAD_REQUEST);
        }

        int nextRound = currentRound + 1;
        if (nextRound <= tournament.getTotalrounds()) {
            List<TournamentResultRecord> readyPlayers = tournamentResultRecordRepository
                    .findByTournamentIdAndRoundAndStatus(tournamentId, nextRound, "READY_TO_PLAY");

            if (readyPlayers.size() <= 1) {
                // Only one player is ready for the next round
                distributeRoundPrize(tournament, nextRound);
                finishTournament(tournamentId);

                TournamentResultRecord result = new TournamentResultRecord();
                result.setTournament(tournament);
                result.setRoomId(null);
                result.setPlayer(readyPlayers.get(0).getPlayer());
                result.setScore(0);
                result.setIsWinner(true);
                result.setStatus("WINNER");
                result.setRound(nextRound);
                result.setPlayedAt(LocalDateTime.now());
                tournamentResultRecordRepository.save(result);
                BigDecimal prize = tournament.getRoomprize();

                Notification notification = new Notification();
                notification.setAmount(tournament.getRoomprize().doubleValue());
//                notification.setDetails("You won ₹ " + tournament.getRoomprize().doubleValue() + " in Round " + nextRound);
                notification.setDetails("You won ₹ " + prize.stripTrailingZeros().toPlainString() + " in Round " + nextRound);

                notification.setDescription("Round Prize");
                notification.setRole("Customer");
                notification.setCustomerId(readyPlayers.get(0).getPlayer().getPlayerId());
                notification.setName(readyPlayers.get(0).getPlayer().getCustomer().getName()!=null?readyPlayers.get(0).getPlayer().getCustomer().getName():"N/A");
                notificationRepository.save(notification);
                return "🏁 Only one player remains. Tournament finished.";
            }

            tournament.setRound(nextRound);
            tournamentRepository.save(tournament);
            processNextRoundMatches(tournamentId, nextRound);

            return "✅ Round " + currentRound + " completed. Round " + nextRound + " has started.";
        } else {
            finishTournament(tournamentId);
            return "🎯 Tournament completed!";
        }
    }

    public Tournament findTournamentById(Long id) throws GameNotFoundException {
        Optional<Tournament> tournament = tournamentRepository.findById(id);
        if (tournament.isPresent()) {
            return tournament.get();
        } else {
            throw new GameNotFoundException("Tournament not found");
        }
    }

    @Transactional
    public Tournament updateTournamentByAdmin(Long tournamentId, TournamentUpdateRequest request) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId)
                    .orElseThrow(() -> new BusinessException("Tournament ID: " + tournamentId + " not found", HttpStatus.NOT_FOUND));

            if (tournament.getStatus() == TournamentStatus.EXPIRED) {
                throw new BusinessException("Tournament has already expired. Update not allowed.", HttpStatus.BAD_REQUEST);
            } else if (tournament.getStatus() == TournamentStatus.ACTIVE) {
                throw new BusinessException("Tournament is already active. Update not allowed.", HttpStatus.BAD_REQUEST);
            }

            if (request.getEntryFee() != null) {
                tournament.setEntryFee(request.getEntryFee());
            }

            if (request.getMove() != null) {
                tournament.setMove(request.getMove());
            }

            if (request.getExistinggameId() != null) {
                tournament.setExistinggameId(request.getExistinggameId());
            }

            if (request.getParticipants() != null) {
                tournament.setParticipants(request.getParticipants());
            }

            if (request.getTotalPrizePool() != null) {
                Double totalPrizePool = request.getTotalPrizePool();
                tournament.setTotalPrizePool(totalPrizePool);

                BigDecimal calculatedRoomPrize = BigDecimal.valueOf(totalPrizePool)
                        .multiply(PriceConstant.USER_PRIZE_PERCENT)
                        .setScale(2, RoundingMode.HALF_UP);
                tournament.setRoomprize(calculatedRoomPrize);
            }


            // Allow override of auto-calculated roomprize if explicitly provided
            if (request.getRoomprize() != null) {
                tournament.setRoomprize(request.getRoomprize());
            }


          /*  if (request.getScheduledAt() != null) {
                ZonedDateTime scheduledAtInKolkata = request.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
                tournament.setScheduledAt(scheduledAtInKolkata);
                tournament.setEndDate(scheduledAtInKolkata);
            }*/

            tournament.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

            return tournamentRepository.save(tournament);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new BusinessException("Error updating tournament: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public Long getScheduledCount() {
//        return count by  scheduled status

        String sql = "SELECT COUNT(*) FROM tournament g WHERE g.status = 'SCHEDULED'";
        Query query = em.createNativeQuery(sql);
        return ((Number) query.getSingleResult()).longValue();

    }

    public Long getActiveCount() {
        String sql = "SELECT COUNT(*) FROM tournament g WHERE g.status = 'ACTIVE'";
        Query query = em.createNativeQuery(sql);
        return ((Number) query.getSingleResult()).longValue();
    }

    public Long getExpiredCount() {
        String sql = "SELECT COUNT(*) FROM tournament g WHERE g.status = 'COMPLETED'";
        Query query = em.createNativeQuery(sql);
        return ((Number) query.getSingleResult()).longValue();
    }


    @Transactional
//   @Scheduled(cron = "0 0 0/4 * * *")
    @Scheduled(cron = "0 0 0/2 * * *")

    public void autoPublishTournamentForVendor_6306470701() {
        Optional<VendorEntity> vendorOpt = vendorRepository.findByMobileNumber(Constant.MOBILE_6306470701);
        if (vendorOpt.isEmpty()) {
            System.out.println(" Vendor with mobile 6306470701 not found.");
            return;
        }

        VendorEntity vendor = vendorOpt.get();
        Long vendorId = vendor.getService_provider_id();

        List<AagAvailableGames> availableGames = aagGameRepository.findAll();

        for (AagAvailableGames gameMeta : availableGames) {
            Long gameId = gameMeta.getId();
            String gameName = gameMeta.getGameName();

            List<Long> themeIds = gameMeta.getThemes().stream()
                    .map(ThemeEntity::getId)
                    .sorted()
                    .toList();

            if (themeIds.isEmpty()) {
                System.out.println("⚠ No themes found for game: " + gameName);
                continue;
            }

            for (Long themeId : themeIds) {
                // Skip if already published today
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(now.getZone());
                ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

                Optional<Long> alreadyPublished = tournamentRepository.findThemeIdIfPublishedToday(
                        vendorId, gameId, themeId, TournamentStatus.SCHEDULED, startOfDay, endOfDay
                );
                if (alreadyPublished.isPresent()) {
                    System.out.println("⏩ Theme " + themeId + " already published today for Tournament of game " + gameId);
                    continue;
                }

                // Build tournament request
                TournamentRequest request = new TournamentRequest();
                List<Integer> entryFees = List.of(10, 25, 50);
                Integer entryfee = entryFees.get(new Random().nextInt(entryFees.size()));
//                Integer particpant  = 512;
                List<Integer> participants = List.of(512, 1024, 256);
                Integer particpant = participants.get(new Random().nextInt(participants.size()));


                int totalPlayers = particpant;

                int totalRounds = (int) Math.ceil(Math.log(totalPlayers) / Math.log(2));
                BigDecimal entryFeePerUser = BigDecimal.valueOf(entryfee);

                BigDecimal totalCollection = entryFeePerUser.multiply(BigDecimal.valueOf(totalPlayers));

                BigDecimal userPrizePool = totalCollection.multiply(PriceConstant.USER_PRIZE_PERCENT);
                BigDecimal roomPrizePool = userPrizePool.divide(new BigDecimal(totalRounds), RoundingMode.HALF_UP);
                request.setName(gameName);
                request.setExistinggameId(gameId);
                ZonedDateTime nowInIndia = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                ZonedDateTime scheduledAt = nowInIndia.plusHours(Constant.TOURNAMENT_START_TIME);
                request.setScheduledAt(scheduledAt);
               request.setTotalPrizePool(totalCollection.doubleValue());
                request.setThemeId(themeId);
                request.setParticipants(512);
                request.setEntryFee(entryfee);

                try {
                 Tournament tournament = createTournamentWithFixedFee(request, vendorId);
                    System.out.println("✅ Published TOURNAMENT " + gameName + " with theme " + themeId + " for vendor " + vendorId);
                    return;
                } catch (Exception e) {
                    System.err.println("❌ Failed to publish TOURNAMENT for game ID " + gameId + ": " + e.getMessage());
                }

                break; // Only one tournament publish per run
            }

            System.out.println("🔁 No unpublished tournament themes left for game ID: " + gameId + " (" + gameName + ")");
        }
    }

    public Tournament getTournamentById(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));
    }

    public Tournament saveTournament(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }



}
