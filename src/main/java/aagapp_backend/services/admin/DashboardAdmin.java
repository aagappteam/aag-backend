package aagapp_backend.services.admin;

import aagapp_backend.dto.DashboardResponseAdmin;
import aagapp_backend.dto.NotificationDTO;
import aagapp_backend.dto.NotificationDTOAdmin;
import aagapp_backend.dto.game.GameResultRecordDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.notification.NotificationShare;
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

    /**
     * Fetches paginated notifications from NotificationShare with optional filtering on amount.
     *
     * @param page   Page number (0-based)
     * @param size   Page size
     * @param amount Optional filter on amount (exact match)
     * @return Paginated list of NotificationDTO
     */
    /**
     * Fetches paginated notifications from NotificationShare with optional filtering on amount.
     *
     * @param page   Page number (0-based)
     * @param size   Page size
     * @param amount Optional filter on amount (exact match)
     * @return Paginated list of NotificationDTOAdmin
     */
/*    @Transactional
    public Page<NotificationDTOAdmin> getAllNotificationsOld Working(int page, int size, Double amount) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM notificationshare n WHERE 1=1");

            if (amount != null) {
                sql.append(" AND n.amount = :amount");
            }

            sql.append(" ORDER BY n.created_date DESC");

            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM notificationshare n WHERE 1=1");

            if (amount != null) {
                countSql.append(" AND n.amount = :amount");
            }

            Query query = entityManager.createNativeQuery(sql.toString(), NotificationShare.class);
            Query countQuery = entityManager.createNativeQuery(countSql.toString());

            if (amount != null) {
                query.setParameter("amount", amount);
                countQuery.setParameter("amount", amount);
            }

            query.setFirstResult(page * size);
            query.setMaxResults(size);

            List<NotificationShare> notifications = query.getResultList();

            // Map NotificationShare to NotificationDTOAdmin
            List<NotificationDTOAdmin> dtos = notifications.stream().map(n -> {
                String vendorName = null;
                String vendorEmail = null;

                if (n.getVendorId() != null) {
                    VendorEntity vendor = entityManager.find(VendorEntity.class, n.getVendorId());
                    if (vendor != null) {
                        vendorName = vendor.getFirst_name()!= null ? vendor.getFirst_name() : "N/A"; // adjust based on your entity
                        vendorEmail = vendor.getPrimary_email()!= null ? vendor.getPrimary_email() : "N/A"; // adjust based on your entity
                    }
                }

                return new NotificationDTOAdmin(
                        n.getId(),
                        n.getVendorId(),
                        "Vendor", // Default role for NotificationShare
                        null,     // customerId is not present in NotificationShare
                        n.getDescription(),
                        n.getDetails(),
                        n.getCreatedDate() != null ? n.getCreatedDate().toString() : null,
                        n.getAmount(),
                        vendorName,
                        vendorEmail
                );
            }).collect(Collectors.toList());

            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(dtos, PageRequest.of(page, size), total);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching notifications: " + e.getMessage(), e);
        }
    }*/
    @Transactional
    public Page<NotificationDTOAdmin> getAllNotifications(int page, int size, Double amount, String vendorName) {
        try {
            // Main query with JOIN to vendor_table to fetch names/emails in one go
            StringBuilder sql = new StringBuilder(
                    "SELECT n.id, n.vendorId, n.description, n.details, n.created_date, n.amount, " +
                            "v.first_name, v.last_name, v.primary_email " +
                            "FROM notificationshare n " +
                            "LEFT JOIN vendor_table v ON n.vendorId = v.service_provider_id " +
                            "WHERE 1=1 "
            );

            // Filters
            if (amount != null) {
                sql.append("AND n.amount = :amount ");
            }

            if (vendorName != null && !vendorName.isEmpty()) {
                String[] nameTokens = vendorName.trim().toLowerCase().split("\\s+");
                for (int i = 0; i < nameTokens.length; i++) {
                    sql.append("AND (LOWER(v.first_name) LIKE :nameToken").append(i)
                            .append(" OR LOWER(v.last_name) LIKE :nameToken").append(i).append(") ");
                }
            }

            sql.append("ORDER BY n.created_date DESC ");

            // Count query
            StringBuilder countSql = new StringBuilder(
                    "SELECT COUNT(*) " +
                            "FROM notificationshare n " +
                            "LEFT JOIN vendor_table v ON n.vendorId = v.service_provider_id " +
                            "WHERE 1=1 "
            );

            if (amount != null) {
                countSql.append("AND n.amount = :amount ");
            }

            if (vendorName != null && !vendorName.isEmpty()) {
                String[] nameTokens = vendorName.trim().toLowerCase().split("\\s+");
                for (int i = 0; i < nameTokens.length; i++) {
                    countSql.append("AND (LOWER(v.first_name) LIKE :nameToken").append(i)
                            .append(" OR LOWER(v.last_name) LIKE :nameToken").append(i).append(") ");
                }
            }

            // Prepare main query
            Query query = entityManager.createNativeQuery(sql.toString());
            Query countQuery = entityManager.createNativeQuery(countSql.toString());

            System.out.println(sql);
            System.out.println(countSql);

            if (amount != null) {
                query.setParameter("amount", amount);
                countQuery.setParameter("amount", amount);
            }

            if (vendorName != null && !vendorName.isEmpty()) {
                String[] nameTokens = vendorName.trim().toLowerCase().split("\\s+");
                for (int i = 0; i < nameTokens.length; i++) {
                    String paramValue = "%" + nameTokens[i] + "%";
                    query.setParameter("nameToken" + i, paramValue);
                    countQuery.setParameter("nameToken" + i, paramValue);
                }
            }

            query.setFirstResult(page * size);
            query.setMaxResults(size);

            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();

            List<NotificationDTOAdmin> dtos = new ArrayList<>();
            for (Object[] row : rows) {
                Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                Long vendorId = row[1] != null ? ((Number) row[1]).longValue() : null;
                String description = row[2] != null ? (String) row[2] : null;
                String details = row[3] != null ? (String) row[3] : null;


 /*               if (row[4] != null) {
                    ZonedDateTime createdDateTime = ((java.sql.Timestamp) row[4]).toInstant().atZone(ZoneId.of("Asia/Kolkata"));
                    createdDate = createdDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }*/

                ZonedDateTime createdDate = ((Instant) row[4]).atZone(ZoneId.of("Asia/Kolkata"));
                String formattedDate = createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                Double amountValue = row[5] != null ? ((Number) row[5]).doubleValue() : null;

                String firstName = row[6] != null ? (String) row[6] : "";
                String lastName = row[7] != null ? (String) row[7] : "";
                String vendorEmail = row[8] != null ? (String) row[8] : "N/A";

                String vendorFullName = (firstName + " " + lastName).trim();
                if (vendorFullName.isEmpty()) {
                    vendorFullName = "N/A";
                }

                NotificationDTOAdmin dto = new NotificationDTOAdmin(
                        id,
                        vendorId,
                        "Vendor",
                        null,
                        description,
                        details,
                        formattedDate,
                        amountValue,
                        vendorFullName,
                        vendorEmail
                );
                dtos.add(dto);
            }

            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(dtos, PageRequest.of(page, size), total);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching notifications: " + e.getMessage(), e);
        }
    }
/*    @Transactional
    public Page<NotificationDTOAdmin> getAllVendorNotificationsold(int page, int size,
                                                                String name,
                                                                Double amount,String role) {
        try {
            // Base queries
            StringBuilder sql = new StringBuilder(
                    "SELECT n.id, n.vendorId, n.role, n.customerId, n.description, " +
                            "n.details, n.created_date, n.amount, " +
                            "v.first_name, v.last_name, v.primary_email, " +  // last_name added for filtering
                            "c.name, c.email " +
                            "FROM notification n " +
                            "LEFT JOIN vendor_table v ON n.vendorId = v.service_provider_id " +
                            "LEFT JOIN CUSTOM_USER c ON n.customerId = c.customerId " +
                            "WHERE 1=1 ");

            StringBuilder countSql = new StringBuilder(
                    "SELECT COUNT(*) " +
                            "FROM notification n " +
                            "LEFT JOIN vendor_table v ON n.vendorId = v.service_provider_id " +
                            "LEFT JOIN CUSTOM_USER c ON n.customerId = c.customerId " +
                            "WHERE 1=1 ");

            // --- Add vendor name filtering conditions ---
            if (vendorName != null && !vendorName.trim().isEmpty()) {
                String[] nameParts = vendorName.trim().split("\\s+");
                if (nameParts.length == 1) {
                    sql.append("AND (LOWER(v.first_name) LIKE LOWER(CONCAT('%', :namePart, '%')) " +
                            "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :namePart, '%'))) ");
                    countSql.append("AND (LOWER(v.first_name) LIKE LOWER(CONCAT('%', :namePart, '%')) " +
                            "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :namePart, '%'))) ");
                } else {
                    // Match any of the two parts in either first_name or last_name
                    sql.append("AND (LOWER(v.first_name) LIKE LOWER(CONCAT('%', :firstPart, '%')) " +
                            "OR LOWER(v.first_name) LIKE LOWER(CONCAT('%', :secondPart, '%')) " +
                            "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :firstPart, '%')) " +
                            "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :secondPart, '%'))) ");
                    countSql.append("AND (LOWER(v.first_name) LIKE LOWER(CONCAT('%', :firstPart, '%')) " +
                            "OR LOWER(v.first_name) LIKE LOWER(CONCAT('%', :secondPart, '%')) " +
                            "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :firstPart, '%')) " +
                            "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :secondPart, '%'))) ");
                }
            }

            // Amount filter
            if (amount != null) {
                sql.append("AND n.amount = :amount ");
                countSql.append("AND n.amount = :amount ");
            }

            sql.append("ORDER BY n.created_date DESC ");

            // Prepare queries
            Query query = entityManager.createNativeQuery(sql.toString());
            Query countQuery = entityManager.createNativeQuery(countSql.toString());

            // --- Set parameters after query creation ---

            if (vendorName != null && !vendorName.trim().isEmpty()) {
                String[] nameParts = vendorName.trim().split("\\s+");
                if (nameParts.length == 1) {
                    String namePart = nameParts[0].trim();
                    query.setParameter("namePart", namePart);
                    countQuery.setParameter("namePart", namePart);
                } else {
                    String firstPart = nameParts[0].trim();
                    String secondPart = nameParts[1].trim();
                    query.setParameter("firstPart", firstPart);
                    query.setParameter("secondPart", secondPart);
                    countQuery.setParameter("firstPart", firstPart);
                    countQuery.setParameter("secondPart", secondPart);
                }
            }

            if (amount != null) {
                query.setParameter("amount", amount);
                countQuery.setParameter("amount", amount);
            }

            // Pagination
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();

            List<NotificationDTOAdmin> resultDtoList = new ArrayList<>();

            for (Object[] row : rows) {
                Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                Long vendorId = row[1] != null ? ((Number) row[1]).longValue() : null;
                String role = row[2] != null ? (String) row[2] : null;
                Long customerId = row[3] != null ? ((Number) row[3]).longValue() : null;
                String description = row[4] != null ? (String) row[4] : null;
                String detailsStr = row[5] != null ? (String) row[5] : null;

                ZonedDateTime createdDate = ((Instant) row[6]).atZone(ZoneId.of("Asia/Kolkata"));
                String formattedDate = createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Double amountValue = row[7] != null ? ((Number) row[7]).doubleValue() : null;

                String name = "N/A";
                String email = "N/A";

                if ("VENDOR".equalsIgnoreCase(role) && vendorId != null) {
                    String vendorFirstName = row[8] != null ? (String) row[8] : null;
                    String vendorLastName = row[9] != null ? (String) row[9] : null;
                    String vendorEmail = row[10] != null ? (String) row[10] : null;
                    name = ((vendorFirstName != null ? vendorFirstName : "") + " " + (vendorLastName != null ? vendorLastName : "")).trim();
                    email = vendorEmail != null ? vendorEmail : "N/A";
                } else if ("CUSTOMER".equalsIgnoreCase(role) && customerId != null) {
                    String customerName = row[11] != null ? (String) row[11] : null;
                    String customerEmail = row[12] != null ? (String) row[12] : null;
                    name = customerName != null ? customerName : "N/A";
                    email = customerEmail != null ? customerEmail : "N/A";
                }

                NotificationDTOAdmin dto = new NotificationDTOAdmin(
                        id,
                        vendorId,
                        role,
                        customerId,
                        description,
                        detailsStr,
                        formattedDate,
                        amountValue,
                        name,
                        email
                );
                resultDtoList.add(dto);
            }

            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(resultDtoList, PageRequest.of(page, size), total);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching notifications: " + e.getMessage(), e);
        }
    }*/

    @Transactional
    public Page<NotificationDTOAdmin> getAllVendorNotifications(
            int page, int size,
            String role,
            String name,
            Double amount) {

        try {
            StringBuilder baseSelect = new StringBuilder(
                    "SELECT n.id, n.vendorId, n.role, n.customerId, n.description, " +
                            "n.details, n.created_date, n.amount ");

            StringBuilder baseCount = new StringBuilder("SELECT COUNT(*) ");

            StringBuilder baseFrom = new StringBuilder("FROM notification n ");

            if (role == null || role.trim().isEmpty()) {
                // Vendor join
                baseSelect.append(", v.first_name, v.last_name, v.primary_email ");
                baseFrom.append("LEFT JOIN vendor_table v ON n.vendorId = v.service_provider_id ");
            } else if ("customer".equalsIgnoreCase(role.trim())) {
                // Customer join
                baseSelect.append(", c.name, c.email ");
                baseFrom.append("LEFT JOIN CUSTOM_USER c ON n.customerId = c.customerId ");
            } else {
                throw new IllegalArgumentException("Unsupported role: " + role);
            }

            // Build WHERE clause
            StringBuilder whereClause = new StringBuilder("WHERE 1=1 ");

            if (role == null || role.trim().isEmpty()) {
                whereClause.append("AND (n.role IS NULL OR LOWER(n.role) = 'vendor') ");
            } else {
                whereClause.append("AND LOWER(n.role) = 'customer' ");
            }

            if (name != null && !name.trim().isEmpty()) {
                String[] nameParts = name.trim().split("\\s+");
                if (role == null || role.trim().isEmpty()) {
                    // Vendor name filter
                    if (nameParts.length == 1) {
                        whereClause.append("AND (LOWER(v.first_name) LIKE LOWER(CONCAT('%', :namePart, '%')) " +
                                "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :namePart, '%'))) ");
                    } else {
                        whereClause.append("AND (LOWER(v.first_name) LIKE LOWER(CONCAT('%', :firstPart, '%')) " +
                                "OR LOWER(v.first_name) LIKE LOWER(CONCAT('%', :secondPart, '%')) " +
                                "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :firstPart, '%')) " +
                                "OR LOWER(v.last_name) LIKE LOWER(CONCAT('%', :secondPart, '%'))) ");
                    }
                } else {
                    // Customer name filter
                    whereClause.append("AND LOWER(c.name) LIKE LOWER(CONCAT('%', :customerName, '%')) ");
                }
            }

            if (amount != null) {
                whereClause.append("AND n.amount = :amount ");
            }

            // Compose full queries
            String sql = baseSelect.toString() + baseFrom.toString() + whereClause.toString() + " ORDER BY n.created_date DESC ";
            String countSql = baseCount.toString() + baseFrom.toString() + whereClause.toString();

            Query query = entityManager.createNativeQuery(sql);
            Query countQuery = entityManager.createNativeQuery(countSql);

            // Set parameters for name
            if (name != null && !name.trim().isEmpty()) {
                String[] nameParts = name.trim().split("\\s+");
                if (role == null || role.trim().isEmpty()) {
                    if (nameParts.length == 1) {
                        query.setParameter("namePart", nameParts[0]);
                        countQuery.setParameter("namePart", nameParts[0]);
                    } else {
                        query.setParameter("firstPart", nameParts[0]);
                        query.setParameter("secondPart", nameParts[1]);
                        countQuery.setParameter("firstPart", nameParts[0]);
                        countQuery.setParameter("secondPart", nameParts[1]);
                    }
                } else {
                    query.setParameter("customerName", name.trim());
                    countQuery.setParameter("customerName", name.trim());
                }
            }

            // Set amount parameter
            if (amount != null) {
                query.setParameter("amount", amount);
                countQuery.setParameter("amount", amount);
            }

            // Pagination - only on data query
            query.setFirstResult(page * size);
            query.setMaxResults(size);

            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();

            List<NotificationDTOAdmin> resultDtoList = new ArrayList<>();

            for (Object[] row : rows) {
                Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                Long vendorId = row[1] != null ? ((Number) row[1]).longValue() : null;
                String fetchedRole = row[2] != null ? (String) row[2] : null;
                Long customerId = row[3] != null ? ((Number) row[3]).longValue() : null;
                String description = row[4] != null ? (String) row[4] : null;
                String detailsStr = row[5] != null ? (String) row[5] : null;

                ZonedDateTime createdDate = ((Instant) row[6]).atZone(ZoneId.of("Asia/Kolkata"));
                String formattedDate = createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                Double amountValue = row[7] != null ? ((Number) row[7]).doubleValue() : null;

                String displayName = "N/A";
                String displayEmail = "N/A";

                if (role == null || role.trim().isEmpty()) {
                    // Vendor columns at index 8,9,10
                    String vendorFirstName = row[8] != null ? (String) row[8] : null;
                    String vendorLastName = row[9] != null ? (String) row[9] : null;
                    String vendorEmail = row[10] != null ? (String) row[10] : null;
                    displayName = ((vendorFirstName != null ? vendorFirstName : "") + " " + (vendorLastName != null ? vendorLastName : "")).trim();
                    displayEmail = vendorEmail != null ? vendorEmail : "N/A";
                } else {
                    // Customer columns at index 8,9
                    String customerName = row[8] != null ? (String) row[8] : null;
                    String customerEmail = row[9] != null ? (String) row[9] : null;
                    displayName = customerName != null ? customerName : "N/A";
                    displayEmail = customerEmail != null ? customerEmail : "N/A";
                }

                NotificationDTOAdmin dto = new NotificationDTOAdmin(
                        id,
                        vendorId,
                        fetchedRole,
                        customerId,
                        description,
                        detailsStr,
                        formattedDate,
                        amountValue,
                        displayName,
                        displayEmail
                );
                resultDtoList.add(dto);
            }

            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(resultDtoList, PageRequest.of(page, size), total);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching notifications: " + e.getMessage(), e);
        }
    }



    // Helper Methods
    private Map<Long, VendorEntity> fetchVendorsByIds(Set<Long> vendorIds) {
        if (vendorIds.isEmpty()) return Collections.emptyMap();
        List<VendorEntity> vendors = entityManager.createQuery(
                        "SELECT v FROM VendorEntity v WHERE v.id IN :ids", VendorEntity.class)
                .setParameter("ids", vendorIds)
                .getResultList();
        return vendors.stream().collect(Collectors.toMap(VendorEntity::getService_provider_id, v -> v));
    }

    private Map<Long, CustomCustomer> fetchCustomersByIds(Set<Long> customerIds) {
        if (customerIds.isEmpty()) return Collections.emptyMap();
        List<CustomCustomer> customers = entityManager.createQuery(
                        "SELECT c FROM CustomCustomer c WHERE c.customerId IN :ids", CustomCustomer.class)
                .setParameter("ids", customerIds)
                .getResultList();
        return customers.stream().collect(Collectors.toMap(CustomCustomer::getId, c -> c));
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
