package aagapp_backend.services.games;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.dto.LeagueResponseDTO;
import aagapp_backend.dto.TournamentResponseDTO;
import aagapp_backend.entity.game.Game;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.payment.PaymentFeatures;
import aagapp_backend.services.tournamnetservice.TournamentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameLeagueTournamentService {

    @Autowired
    private EntityManager entityManager;

    private GameService gameService;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;
    private PlayerRepository playerRepository;
    private GameRoomRepository gameRoomRepository;
    private NotificationRepository notificationRepository;
    private LeagueService leagueService;
    private TournamentService tournamentService;

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    public void setLeagueService(@Lazy LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @Autowired
    public void setTournamentService(@Lazy TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @Autowired
    public void setNotificationRepository(@Lazy NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Autowired
    public void setGameService(@Lazy GameService gameService) {
        this.gameService = gameService;
    }

    @Autowired
    public void setPlayerRepository(@Lazy PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Autowired
    public void setGameRoomRepository(@Lazy GameRoomRepository gameRoomRepository) {
        this.gameRoomRepository = gameRoomRepository;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setPaymentFeatures(@Lazy PaymentFeatures paymentFeatures) {
        this.paymentFeatures = paymentFeatures;
    }

    @Transactional
    public Page<GetGameResponseDTO> getAllGames(String status, Long vendorId, Pageable pageable, LocalDate startDate, LocalDate endDate, LocalDate scheduledDate) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM aag_ludo_game g WHERE 1=1");

            // Retain vendorId filter
            if (vendorId != null) {
                sql.append(" AND g.vendor_id = :vendorId");
            }

            // Filter by status
            if (status != null && !status.isEmpty()) {
                sql.append(" AND g.status = :status");
            }

            // Handle filtering by scheduled date (start and end of the day)
            if (scheduledDate != null) {
                ZonedDateTime startOfDay = scheduledDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // End of the day
                sql.append(" AND g.scheduled_at BETWEEN :scheduledStart AND :scheduledEnd");
            }

            // Handle filtering by published date range (startDate and endDate)
            if (startDate != null && endDate != null) {
                ZonedDateTime startOfDay = startDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1); // End of the day
                sql.append(" AND g.created_at BETWEEN :pubStartDate AND :pubEndDate");
            }

            Query query = entityManager.createNativeQuery(sql.toString(), Game.class);

            // Set parameters
            if (vendorId != null) {
                query.setParameter("vendorId", vendorId);
            }
            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }
            if (scheduledDate != null) {
                ZonedDateTime startOfDay = scheduledDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // End of the day
                query.setParameter("scheduledStart", startOfDay);
                query.setParameter("scheduledEnd", endOfDay);
            }
            if (startDate != null && endDate != null) {
                ZonedDateTime startOfDay = startDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1); // End of the day
                query.setParameter("pubStartDate", startOfDay);
                query.setParameter("pubEndDate", endOfDay);
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Game> games = query.getResultList();

            List<GetGameResponseDTO> gameResponseDTOs = games.stream()
                    .map(game -> new GetGameResponseDTO(
                            game.getId(),
                            game.getFee(),
                            game.getMove(),
                            game.getStatus(),
                            game.getShareableLink(),
                            game.getAaggameid(),
                            game.getImageUrl(),
                            game.getTheme() != null ? game.getTheme().getName() : null,
                            game.getTheme() != null ? game.getTheme().getImageUrl() : null,
                            game.getScheduledAt() != null ? game.getScheduledAt().toString() : null,
                            game.getEndDate() != null ? game.getEndDate().toString() : null,
                            game.getMinPlayersPerTeam(),
                            game.getMaxPlayersPerTeam(),
                            game.getVendorEntity() != null ? game.getVendorEntity().getFirst_name() : null,
                            game.getVendorEntity() != null ? game.getVendorEntity().getProfilePic() : null
                    ))
                    .collect(Collectors.toList());

            String countSql = "SELECT COUNT(*) FROM aag_ludo_game g WHERE 1=1";
            if (vendorId != null) {
                countSql += " AND g.vendor_id = :vendorId";
            }
            if (status != null && !status.isEmpty()) {
                countSql += " AND g.status = :status";
            }

            Query countQuery = entityManager.createNativeQuery(countSql);
            if (vendorId != null) {
                countQuery.setParameter("vendorId", vendorId);
            }
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }

            Long count = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(gameResponseDTOs, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving games", e);
        }
    }


    @Transactional
    public Page<LeagueResponseDTO> getAllLeagues(String status, Long vendorId, Pageable pageable, LocalDate startDate, LocalDate endDate, LocalDate scheduledDate) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM aag_league l WHERE 1=1");

            // Retain vendorId filter
            if (vendorId != null) {
                sql.append(" AND l.vendor_id = :vendorId");
            }

            // Corrected status filter
            if (status != null && !status.isEmpty()) {
                sql.append(" AND l.status = :status");
            }

            // Handle filtering by published date range (startDate and endDate)
            if (startDate != null && endDate != null) {
                ZonedDateTime startOfDay = startDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1); // End of the day
                sql.append(" AND l.created_at BETWEEN :pubStartDate AND :pubEndDate");
            }

            // Handle filtering by scheduled date (start and end of the day)
            if (scheduledDate != null) {
                ZonedDateTime startOfDay = scheduledDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // End of the day
                sql.append(" AND l.scheduled_at BETWEEN :scheduledStart AND :scheduledEnd");
            }

            Query query = entityManager.createNativeQuery(sql.toString(), League.class);

            // Set parameters
            if (vendorId != null) {
                query.setParameter("vendorId", vendorId);
            }
            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }
            if (startDate != null && endDate != null) {
                ZonedDateTime startOfDay = startDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1); // End of the day
                query.setParameter("pubStartDate", startOfDay);
                query.setParameter("pubEndDate", endOfDay);
            }
            if (scheduledDate != null) {
                ZonedDateTime startOfDay = scheduledDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // End of the day
                query.setParameter("scheduledStart", startOfDay);
                query.setParameter("scheduledEnd", endOfDay);
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<League> leagues = query.getResultList();

            List<LeagueResponseDTO> leagueResponseDTOs = leagues.stream()
                    .map(league -> new LeagueResponseDTO(
                            league.getId(),
                            league.getFee(),
                            league.getMove(),
                            league.getStatus(),
                            league.getShareableLink(),
                            league.getTheme() != null ? league.getTheme().getName() : null,
                            league.getTheme() != null ? league.getTheme().getImageUrl() : null,
                            league.getScheduledAt() != null ? league.getScheduledAt().toString() : null,
                            league.getEndDate() != null ? league.getEndDate().toString() : null,
                            league.getMinPlayersPerTeam(),
                            league.getMaxPlayersPerTeam(),
                            league.getVendorEntity() != null ? league.getVendorEntity().getFirst_name() : null,
                            league.getVendorEntity() != null ? league.getVendorEntity().getProfilePic() : null
                    ))
                    .collect(Collectors.toList());

            String countSql = "SELECT COUNT(*) FROM aag_league l WHERE 1=1";
            if (vendorId != null) {
                countSql += " AND l.vendor_id = :vendorId";
            }
            if (status != null && !status.isEmpty()) {
                countSql += " AND l.status = :status";
            }

            Query countQuery = entityManager.createNativeQuery(countSql);
            if (vendorId != null) {
                countQuery.setParameter("vendorId", vendorId);
            }
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }

            Long count = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(leagueResponseDTOs, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving leagues", e);
        }
    }


    @Transactional
    public Page<TournamentResponseDTO> getAllTournaments(String status, Long vendorId, Pageable pageable, LocalDate startDate, LocalDate endDate, LocalDate scheduledDate) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM tournament t WHERE 1=1");

            // Retain vendorId filter
            if (vendorId != null) {
                sql.append(" AND t.vendor_id = :vendorId");
            }

            // Corrected status filter
            if (status != null && !status.isEmpty()) {
                sql.append(" AND t.status = :status");
            }

            // Handle filtering by published date range (startDate and endDate)
            if (startDate != null && endDate != null) {
                ZonedDateTime startOfDay = startDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1); // End of the day
                sql.append(" AND t.created_at BETWEEN :pubStartDate AND :pubEndDate");
            }

            // Handle filtering by scheduled date (start and end of the day)
            if (scheduledDate != null) {
                ZonedDateTime startOfDay = scheduledDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // End of the day
                sql.append(" AND t.scheduled_at BETWEEN :scheduledStart AND :scheduledEnd");
            }

            Query query = entityManager.createNativeQuery(sql.toString(), Tournament.class);

            // Set parameters
            if (vendorId != null) {
                query.setParameter("vendorId", vendorId);
            }
            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }
            if (startDate != null && endDate != null) {
                ZonedDateTime startOfDay = startDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusSeconds(1); // End of the day
                query.setParameter("pubStartDate", startOfDay);
                query.setParameter("pubEndDate", endOfDay);
            }
            if (scheduledDate != null) {
                ZonedDateTime startOfDay = scheduledDate.atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // End of the day
                query.setParameter("scheduledStart", startOfDay);
                query.setParameter("scheduledEnd", endOfDay);
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Tournament> tournaments = query.getResultList();

            List<TournamentResponseDTO> tournamentResponseDTOs = tournaments.stream()
                    .map(tournament -> new TournamentResponseDTO(
                            tournament.getId(),
                            tournament.getEntryFee(),
                            tournament.getMove(),
                            tournament.getStatus(),
                            tournament.getShareableLink(),
                            tournament.getExistinggameId(),
                            tournament.getTheme() != null ? tournament.getTheme().getName() : null,
                            tournament.getTheme() != null ? tournament.getTheme().getImageUrl() : null,
                            tournament.getScheduledAt() != null ? tournament.getScheduledAt().toString() : null
                    ))
                    .collect(Collectors.toList());

            String countSql = "SELECT COUNT(*) FROM tournament t WHERE 1=1";
            if (vendorId != null) {
                countSql += " AND t.vendor_id = :vendorId";
            }
            if (status != null && !status.isEmpty()) {
                countSql += " AND t.status = :status";
            }

            Query countQuery = entityManager.createNativeQuery(countSql);
            if (vendorId != null) {
                countQuery.setParameter("vendorId", vendorId);
            }
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }

            Long count = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(tournamentResponseDTOs, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving tournaments", e);
        }
    }


}
