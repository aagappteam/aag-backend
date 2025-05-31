package aagapp_backend.services.admin;

import aagapp_backend.dto.DashboardResponseAdmin;
import aagapp_backend.dto.NotificationDTO;
import aagapp_backend.dto.NotificationDTOAdmin;
import aagapp_backend.dto.game.GameResultRecordDTO;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.repository.game.GameResultRecordRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.league.LeagueResultRecordRepository;
import aagapp_backend.repository.tournament.TournamentResultRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardAdmin {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TournamentResultRecordRepository tournamentResultRecordRepository;
    @Autowired
    private GameResultRecordRepository gameResultRecordRepository;
    @Autowired
    private LeagueResultRecordRepository leagueResultRecordRepository;


    public DashboardResponseAdmin getDashboard() {

//      long activeUsers = playerRepository.countByStatus(PlayerStatus.ACTIVE);
        long activeTournaments = playerRepository.countByTournamentRoomIsNotNull();
        long activeGames = playerRepository.countByGameRoomIsNotNull();
        long activeLeagues = playerRepository.countByLeagueRoomIsNotNull();

        long completedTournaments = tournamentResultRecordRepository.count();
        long completedGames = gameResultRecordRepository.count();
        long completedLeagues = leagueResultRecordRepository.count();

        return new DashboardResponseAdmin(
                activeTournaments,
                activeGames,
                activeLeagues,
                completedTournaments,
                completedGames,
                completedLeagues
        );
    }


    public Page<GameResultRecordDTO> getAllGameResults(Pageable pageable) {
        return gameResultRecordRepository.findAll(pageable)
                .map(record -> {
                    return new GameResultRecordDTO(
                            record.getId(),
                            record.getRoomId(),
                            record.getGame().getName()!=null? record.getGame().getName():"No Name",
                            record.getPlayer().getCustomer().getName()!=null? record.getPlayer().getCustomer().getName():"No Name",
                            record.getPlayer().getPlayerProfilePic(),
                            record.getWinningammount(),
                            record.getScore(),
                            record.getIsWinner()?"Winner":"Looser",
                            record.getPlayedAt()
                    );
                });
    }

    @Transactional
    public Page<NotificationDTO> getAllNotifications(int page, int size) {
        try {
            String sql = "SELECT * FROM notification n ORDER BY n.created_date DESC";
            String countSql = "SELECT COUNT(*) FROM notification";

            Query query = entityManager.createNativeQuery(sql, Notification.class);
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            List<Notification> notifications = query.getResultList();

            // Map Notification to NotificationDTO
            List<NotificationDTO> dtos = notifications.stream().map(n -> new NotificationDTO(
                    n.getId(),
                    n.getVendorId(),
                    n.getRole(),
                    n.getCustomerId(),

                    n.getDescription(),
                    n.getDetails(),
                    n.getCreatedDate().toString(),

                    n.getAmount()
            )).collect(Collectors.toList());

            Query countQuery = entityManager.createNativeQuery(countSql);
            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(dtos, PageRequest.of(page, size), total);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching notifications: " + e.getMessage(), e);
        }
    }
    @Transactional
    public Page<NotificationDTOAdmin> getAllVendorNotifications(int page, int size,
                                                           String vendorName,
                                                           String details,
                                                           Double amount) {
        try {
            StringBuilder sql = new StringBuilder("SELECT n.id, n.vendorId, n.role, n.customerId, n.description, " +
                    "n.details, n.created_date, n.amount, v.first_name AS vendorName, v.primary_email AS vendorEmail " +
                    "FROM notification n " +
                    "JOIN vendor_table v ON n.vendorId = v.service_provider_id WHERE 1=1");

            if (vendorName != null && !vendorName.isEmpty()) {
                sql.append(" AND v.first_name LIKE :vendorName");
            }

            if (details != null && !details.isEmpty()) {
                sql.append(" AND n.details LIKE :details");
            }

            if (amount != null) {
                sql.append(" AND n.amount = :amount");
            }

            sql.append(" ORDER BY n.created_date DESC");

            // Count Query (same filters as above)
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM notification n " +
                    "JOIN vendor_table v ON n.vendorId = v.service_provider_id WHERE 1=1");

            if (vendorName != null && !vendorName.isEmpty()) {
                countSql.append(" AND v.first_name LIKE :vendorName");
            }

            if (details != null && !details.isEmpty()) {
                countSql.append(" AND n.details LIKE :details");
            }

            if (amount != null) {
                countSql.append(" AND n.amount = :amount");
            }

            Query query = entityManager.createNativeQuery(sql.toString());

            if (vendorName != null && !vendorName.isEmpty()) {
                query.setParameter("vendorName", "%" + vendorName + "%");
            }

            if (details != null && !details.isEmpty()) {
                query.setParameter("details", "%" + details + "%");
            }

            if (amount != null) {
                query.setParameter("amount", amount);
            }

            query.setFirstResult(page * size);
            query.setMaxResults(size);

            // Execute the query and map the results to a List of DTOs
            List<Object[]> rows = query.getResultList();
            List<NotificationDTOAdmin> resultDtoList = new ArrayList<>();

            for (Object[] row : rows) {
                // Convert each row to a NotificationDTO
                ZonedDateTime createdDate = null;
                if (row[6] != null) {
                    createdDate = ((Instant) row[6]).atZone(ZoneId.of("Asia/Kolkata"));

                    // Format the ZonedDateTime to the desired format (yyyy-MM-dd HH:mm:ss)
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = createdDate.format(formatter);

                    // Create DTO and add to list
                    NotificationDTOAdmin dto = new NotificationDTOAdmin(
                            (Long) row[0],                      // id
                            (Long) row[1],                      // vendorId
                            (String) row[2],                    // role
                            (row[3] != null ? (Long) row[3] : null), // customerId
                            (String) row[4],                    // description
                            (String) row[5],                    // details
                            formattedDate,                      // createdDate
                            (row[7] != null ? (Double) row[7] : null), // amount
                            (String) row[8],                    // vendorName
                            (String) row[9]                     // vendorEmail
                    );
                    resultDtoList.add(dto);
                }
            }

            // Count query to get total results
            Query countQuery = entityManager.createNativeQuery(countSql.toString());

            if (vendorName != null && !vendorName.isEmpty()) {
                countQuery.setParameter("vendorName", "%" + vendorName + "%");
            }

            if (details != null && !details.isEmpty()) {
                countQuery.setParameter("details", "%" + details + "%");
            }

            if (amount != null) {
                countQuery.setParameter("amount", amount);
            }

            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(resultDtoList, PageRequest.of(page, size), total);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching notifications: " + e.getMessage(), e);
        }
    }





/*
    @Transactional
    public Page<NotificationDTOAdmin> getAllVendorNotifications(int page, int size,
                                                                String vendorName,
                                                                String details,
                                                                Double amount) {
        try {
            // Base SQL Query
            StringBuilder sql = new StringBuilder("SELECT n.id, n.vendorId, n.role, n.customerId, n.description, " +
                    "n.details, n.created_date, n.amount, v.first_name AS vendorName, v.primary_email AS vendorEmail " +
                    "FROM notification n " +
                    "JOIN vendor_table v ON n.vendorId = v.service_provider_id WHERE 1=1");

            if (vendorName != null && !vendorName.isEmpty()) {
                sql.append(" AND v.first_name LIKE :vendorName");
            }

            if (details != null && !details.isEmpty()) {
                sql.append(" AND n.details LIKE :details");
            }

            if (amount != null) {
                sql.append(" AND n.amount = :amount");
            }

            sql.append(" ORDER BY n.created_date DESC");

            // Count Query (with same filters)
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM notification n " +
                    "JOIN vendor_table v ON n.vendorId = v.service_provider_id WHERE 1=1");

            if (vendorName != null && !vendorName.isEmpty()) {
                countSql.append(" AND v.first_name LIKE :vendorName");
            }

            if (details != null && !details.isEmpty()) {
                countSql.append(" AND n.details LIKE :details");
            }

            if (amount != null) {
                countSql.append(" AND n.amount = :amount");
            }

            // Main Query Execution (no DTO class passed — raw Object[])
            Query query = entityManager.createNativeQuery(sql.toString());

            if (vendorName != null && !vendorName.isEmpty()) {
                query.setParameter("vendorName", "%" + vendorName + "%");
            }

            if (details != null && !details.isEmpty()) {
                query.setParameter("details", "%" + details + "%");
            }

            if (amount != null) {
                query.setParameter("amount", amount);
            }

            query.setFirstResult(page * size);
            query.setMaxResults(size);

            List<Object[]> rows = query.getResultList();
            List<NotificationDTOAdmin> dtos = new ArrayList<>();
            for (Object[] row : rows) {
                NotificationDTOAdmin dto = new NotificationDTOAdmin(
                        row[0] != null ? ((Number) row[0]).longValue() : null,             // id
                        row[1] != null ? ((Number) row[1]).longValue() : null,             // vendorId
                        (String) row[2],                                                    // role
                        row[3] != null ? ((Number) row[3]).longValue() : null,             // customerId
                        (String) row[4],                                                    // description
                        (String) row[5],                                                    // details
                        row[6] != null ?
                                ((java.sql.Timestamp) row[6]).toInstant().atZone(ZoneId.of("Asia/Kolkata")) : null,
                        row[7] != null ? ((Number) row[7]).doubleValue() : null,           // amount
                        (String) row[8],                                                    // vendorName
                        (String) row[9]                                                     // vendorEmail
                );
                dtos.add(dto);
            }


            // Count query
            Query countQuery = entityManager.createNativeQuery(countSql.toString());

            if (vendorName != null && !vendorName.isEmpty()) {
                countQuery.setParameter("vendorName", "%" + vendorName + "%");
            }

            if (details != null && !details.isEmpty()) {
                countQuery.setParameter("details", "%" + details + "%");
            }

            if (amount != null) {
                countQuery.setParameter("amount", amount);
            }

            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(dtos, PageRequest.of(page, size), total);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching notifications: " + e.getMessage(), e);
        }
    }
*/




}
