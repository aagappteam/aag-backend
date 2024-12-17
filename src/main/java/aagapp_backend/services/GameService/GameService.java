package aagapp_backend.services.GameService;

import aagapp_backend.dto.GameRequest;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.game.Game;
import aagapp_backend.enums.GameStatus;
import aagapp_backend.enums.PaymentStatus;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.exception.ExceptionHandlingService;
import aagapp_backend.services.payment.PaymentFeatures;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.naming.LimitExceededException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class GameService {

    private GameRepository gameRepository;
    private ResponseService responseService;
    private PaymentFeatures paymentFeatures;
    private EntityManager em;
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    public void setExceptionHandling(ExceptionHandlingService exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setPaymentFeatures(@Lazy PaymentFeatures paymentFeatures) {
        this.paymentFeatures = paymentFeatures;
    }

    @Autowired
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Scheduled(cron = "0 * * * * *")  // Every minute
    public void checkAndActivateScheduledGames() {
        int page = 0;
        int pageSize = 1000;
        List<Long> vendorIds;
        while (!(vendorIds = getActiveVendorIdsInBatch(page, pageSize)).isEmpty()) {

            System.out.println("Scheduled task checkAndActivateScheduledGames started  " + vendorIds);

            for (Long vendorId : vendorIds) {
                updateGameStatusToActive(vendorId);
            }
            page++;
        }


    }


    public List<Long> getActiveVendorIdsInBatch(int page, int pageSize) {
        LocalDateTime now = LocalDateTime.now();

        String queryString = "SELECT v.id FROM VendorEntity v " +
                "JOIN PaymentEntity p ON p.vendorEntity.id = v.id " +
                "WHERE p.status = :activeStatus " +
                "AND p.expiryAt IS NOT NULL " +
                "AND p.expiryAt > :now " +
                "AND p.expiryAt = (" +
                "   SELECT MAX(p2.expiryAt) " +
                "   FROM PaymentEntity p2 " +
                "   WHERE p2.vendorEntity.id = v.id " +
                "   AND p2.expiryAt IS NOT NULL " +
                "   AND p2.expiryAt > :now" +
                ")";


        Query query = em.createQuery(queryString);
        query.setParameter("activeStatus", PaymentStatus.ACTIVE);
        query.setParameter("now", now);

        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Game publishGame(GameRequest gameRequest, Long vendorId) throws LimitExceededException {

        try {
            Game game = new Game();
            game.setName(gameRequest.getName());
            game.setDescription(gameRequest.getDescription());
            game.setEntryFee(gameRequest.getEntryFee());

            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (vendorEntity == null) {
                throw new RuntimeException("No records found for vendor");
            }

            ThemeEntity theme = em.find(ThemeEntity.class, gameRequest.getThemeId());
            if (theme == null) {
                throw new RuntimeException("No theme found with the provided ID");
            }

            game.setVendorEntity(vendorEntity);
            game.setTheme(theme);

            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            if (gameRequest.getScheduledAt() != null) {

                ZonedDateTime scheduledInKolkata = gameRequest.getScheduledAt()
                        .withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

                // Ensure that the game is scheduled at least 4 hours in advance
                if (scheduledInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The game must be scheduled at least 4 hours in advance.");
                }

                game.setStatus(GameStatus.SCHEDULED);
                game.setScheduledAt(scheduledInKolkata);
            } else {
//                game.setStatus(GameStatus.ACTIVE);
                game.setStatus(GameStatus.SCHEDULED);
               game.setScheduledAt(nowInKolkata.plusMinutes(15));
//                game.setScheduledAt(nowInKolkata);

            }

            game.setCreatedDate(nowInKolkata);
            game.setUpdatedDate(nowInKolkata);

            Game savedGame = gameRepository.save(game);

            String shareableLink = generateShareableLink(savedGame.getId());
            savedGame.setShareableLink(shareableLink);

            return gameRepository.save(savedGame);

        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new IllegalArgumentException("Invalid game schedule: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the game: " + e.getMessage(), e);
        }
    }

    private String generateShareableLink(Long gameId) {
        return "https://example.com/games/" + gameId;
    }

    @Transactional
    public Page<Game> getAllGames(String status, Long vendorId, Pageable pageable) {
        try {
            StringBuilder sql = new StringBuilder("SELECT * FROM aag_game g WHERE 1=1");

            if (status != null && !status.isEmpty()) {
                sql.append(" AND g.status = :status");
            }
            if (vendorId != null) {
                sql.append(" AND g.vendor_id = :vendorId");
            }

            Query query = em.createNativeQuery(sql.toString(), Game.class);

            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }
            if (vendorId != null) {
                query.setParameter("vendorId", vendorId);
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Game> games = query.getResultList();

            // Convert time fields (createdDate, updatedDate, scheduledAt) to Asia/Kolkata timezone
            games.forEach(game -> {
                if (game.getCreatedDate() != null) {
                    game.setCreatedDate(convertToKolkataTime(game.getCreatedDate()));

                }
                if (game.getUpdatedDate() != null) {
                    game.setUpdatedDate(convertToKolkataTime(game.getUpdatedDate()));
                }
                if (game.getScheduledAt() != null) {
                    game.setScheduledAt(convertToKolkataTime(game.getScheduledAt()));
                }
            });

            String countSql = "SELECT COUNT(*) FROM aag_game g WHERE 1=1";
            if (status != null && !status.isEmpty()) {
                countSql += " AND g.status = :status";
            }
            if (vendorId != null) {
                countSql += " AND g.vendor_id = :vendorId";
            }

            Query countQuery = em.createNativeQuery(countSql);
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }
            if (vendorId != null) {
                countQuery.setParameter("vendorId", vendorId);
            }

            Long count = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(games, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving games", e);
        }
    }



    public ResponseEntity<?> getGameDetails(Long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (game == null) {
            return ResponseService.generateErrorResponse("Game not found", HttpStatus.NOT_FOUND);
        }

        game.ifPresent(g -> {

            g.setCreatedDate(convertToKolkataTime(g.getCreatedDate()));
            g.setUpdatedDate(convertToKolkataTime(g.getUpdatedDate()));
            g.setScheduledAt(convertToKolkataTime(g.getScheduledAt()));

        });

        return responseService.generateSuccessResponse("Game details Found", game, HttpStatus.OK);
    }


   /* @Transactional
    public ResponseEntity<?> updateGame(Long vendorId, Long gameId, GameRequest gameRequest) {
        try {
            Game game = gameRepository.findById(gameId).orElseThrow(() ->
                    new IllegalArgumentException("Game with ID " + gameId + " not found.")
            );

            if (!game.getVendorEntity().getService_provider_id().equals(vendorId)) {
                throw new IllegalStateException("Game ID: " + gameId + " does not belong to Vendor ID: " + vendorId);
            }

            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            if (game.getStatus() == GameStatus.EXPIRED) {
                throw new IllegalStateException("Game ID: " + game.getId() + " has already expired. No update allowed.");
            } else if (game.getStatus() == GameStatus.ACTIVE) {
                throw new IllegalStateException("Game ID: " + game.getId() + " is already active. No update allowed.");
            }

            if (game.getScheduledAt() != null) {
                ZonedDateTime scheduledAtInKolkata = game.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

                // If the scheduled date is today or in the future, disallow updates
               *//* if (!scheduledAtInKolkata.isBefore(nowInKolkata)) {
                    throw new IllegalStateException("Game ID: " + game.getId() + " is scheduled for today or the future. No update allowed.");
                }*//*

                // Check if the scheduled date is 1 day ahead (T-1) and allow updates until 1 day before (T-1 day)
                ZonedDateTime oneDayBeforeScheduled = scheduledAtInKolkata.minusDays(1);
                if (nowInKolkata.isBefore(oneDayBeforeScheduled)) {
                    // We are within the acceptable window to update the game (up to T-1 day)


                    if (game.getStatus() == GameStatus.SCHEDULED) {
                        game.setStatus(GameStatus.ACTIVE);
                    }

                    if (gameRequest.getName() != null && !gameRequest.getName().isEmpty()) {
                        game.setName(gameRequest.getName());
                    }
                    if (gameRequest.getEntryFee() != null) {
                        game.setEntryFee(gameRequest.getEntryFee());
                    }
                    if (gameRequest.getDescription() != null && !gameRequest.getDescription().isEmpty()) {
                        game.setDescription(gameRequest.getDescription());
                    }

                    game.setScheduledAt(null);
                    game.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

                    gameRepository.save(game);
                    return responseService.generateSuccessResponse("Game updated successfully.", game, HttpStatus.OK);
                } else {
                    // If the current time is too close to the scheduled time or it's the scheduled date itself
                    throw new IllegalStateException("Game ID: " + game.getId() + " cannot be updated on the scheduled date or after.");
                }
            } else {
                throw new IllegalStateException("Game ID: " + game.getId() + " does not have a scheduled time.");
            }
        } catch (IllegalStateException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Error updating game details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
   @Transactional
   public ResponseEntity<?> updateGame(Long vendorId, Long gameId, GameRequest gameRequest) {
       try {
           System.out.println("Game ID: " + gameId + " Vendor ID: " + vendorId);

           // Updated JPQL query with correct column names
           String jpql = "SELECT g FROM Game g WHERE g.id = :gameId AND g.vendorEntity.id = :vendorId";
           TypedQuery<Game> query = em.createQuery(jpql, Game.class);
           query.setParameter("gameId", gameId);
           query.setParameter("vendorId", vendorId);

           // Fetch the game, throw exception if not found
           Game game = query.getResultList().stream()
                   .findFirst()
                   .orElseThrow(() -> new IllegalStateException("Game ID: " + gameId + " does not belong to Vendor ID: " + vendorId));

           System.out.println("Game ID: " + gameId + " Vendor ID: " + vendorId + " Game: " + game);

           // Continue with the rest of your logic...
           ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
           ZonedDateTime scheduledAtInKolkata = game.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

           // Validate game status
           if (game.getStatus() == GameStatus.EXPIRED) {
               throw new IllegalStateException("Game ID: " + game.getId() + " has already expired. No update allowed.");
           } else if (game.getStatus() == GameStatus.ACTIVE) {
               throw new IllegalStateException("Game ID: " + game.getId() + " is already active. No update allowed.");
           }

           // Validate scheduled date
           if (scheduledAtInKolkata != null) {
               ZonedDateTime oneDayBeforeScheduled = scheduledAtInKolkata.minusDays(1);

               if (nowInKolkata.isBefore(oneDayBeforeScheduled)) {
                   if (game.getStatus() == GameStatus.SCHEDULED) {
                       game.setStatus(GameStatus.ACTIVE);
                   }

                   // Update game details if provided
                   if (gameRequest.getName() != null && !gameRequest.getName().isEmpty()) {
                       game.setName(gameRequest.getName());
                   }
                   if (gameRequest.getEntryFee() != null) {
                       game.setEntryFee(gameRequest.getEntryFee());
                   }
                   if (gameRequest.getDescription() != null && !gameRequest.getDescription().isEmpty()) {
                       game.setDescription(gameRequest.getDescription());
                   }

                   game.setScheduledAt(null);
                   game.setUpdatedDate(ZonedDateTime.now(ZoneId.of("Asia/Kolkata")));

                   em.merge(game);
                   return ResponseEntity.ok("Game updated successfully"); // Success message
               } else {
                   throw new IllegalStateException("Game ID: " + game.getId() + " cannot be updated on the scheduled date or after.");
               }
           } else {
               throw new IllegalStateException("Game ID: " + game.getId() + " does not have a scheduled time.");
           }
       } catch (IllegalStateException e) {
           exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
           return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
       } catch (Exception e) {
           exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
           return responseService.generateErrorResponse("Error updating game details: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
   }



    public int countGamesByVendorIdAndScheduledDate(Long vendorId, LocalDate date) {
        String queryString = "SELECT COUNT(g) FROM Game g WHERE g.vendorEntity.id = :vendorId AND FUNCTION('DATE', g.createdDate) = :date";
        Query query = em.createQuery(queryString);
        query.setParameter("vendorId", vendorId);
        query.setParameter("date", date);

        return ((Long) query.getSingleResult()).intValue();
    }


    @Transactional
    public Page<Game> findGamesByVendor(Long vendorId, String status, Pageable pageable) {
        try {
            String sql = "SELECT * FROM aag_game g WHERE g.vendor_id = :vendorId";

            if (status != null && !status.isEmpty()) {
                sql += " AND g.status = :status";
            }

            Query query = em.createNativeQuery(sql, Game.class);
            query.setParameter("vendorId", vendorId);

            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<Game> games = query.getResultList();

            games.forEach(game -> {
                if (game.getCreatedDate() != null) {
                    game.setCreatedDate(convertToKolkataTime(game.getCreatedDate()));

                }
                if (game.getUpdatedDate() != null) {
                    game.setUpdatedDate(convertToKolkataTime(game.getUpdatedDate()));
                }
                if (game.getScheduledAt() != null) {
                    game.setScheduledAt(convertToKolkataTime(game.getScheduledAt()));
                }
            });

            String countSql = "SELECT COUNT(*) FROM aag_game g WHERE g.vendor_id = :vendorId";
            if (status != null && !status.isEmpty()) {
                countSql += " AND g.status = :status";
            }

            Query countQuery = em.createNativeQuery(countSql);
            countQuery.setParameter("vendorId", vendorId);
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }

            Long count = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(games, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving games by vendor ID: " + vendorId, e);
        }
    }

   @Transactional
   public Page<Game> findGamesScheduledForToday(Long vendorId, Pageable pageable) {
       try {
           ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
           ZonedDateTime startOfDay = nowInKolkata.toLocalDate().atStartOfDay(ZoneId.of("Asia/Kolkata"));
           ZonedDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // End of the current day

           String sql = "SELECT * FROM aag_game g WHERE g.vendor_id = :vendorId " +
                   "AND g.scheduled_at >= :startOfDay AND g.scheduled_at <= :endOfDay";

           Query query = em.createNativeQuery(sql, Game.class);
           query.setParameter("vendorId", vendorId);
           query.setParameter("startOfDay", startOfDay);
           query.setParameter("endOfDay", endOfDay);

           query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
           query.setMaxResults(pageable.getPageSize());

           List<Game> games = query.getResultList();

           games.forEach(game -> {

               game.setCreatedDate(convertToKolkataTime(game.getCreatedDate()));
               game.setUpdatedDate(convertToKolkataTime(game.getUpdatedDate()));
               game.setScheduledAt(convertToKolkataTime(game.getScheduledAt()));

           });

           String countSql = "SELECT COUNT(*) FROM aag_game g WHERE g.vendor_id = :vendorId " +
                   "AND g.scheduled_at >= :startOfDay AND g.scheduled_at <= :endOfDay";

           Query countQuery = em.createNativeQuery(countSql);
           countQuery.setParameter("vendorId", vendorId);
           countQuery.setParameter("startOfDay", startOfDay);
           countQuery.setParameter("endOfDay", endOfDay);

           Long count = ((Number) countQuery.getSingleResult()).longValue();

           return new PageImpl<>(games, pageable, count);
       } catch (Exception e) {
           throw new RuntimeException("Error retrieving games scheduled for today by vendor ID: " + vendorId, e);
       }
   }
    public static ZonedDateTime convertToKolkataTime(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));
    }


    @Transactional
    @Async
    public void updateGameStatusToActive(Long vendorId) {
        try {
            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            String sql = "SELECT * FROM aag_game g WHERE g.vendor_id = :vendorId " +
                    "AND g.scheduled_at <= :nowInKolkata AND g.status = :status";

            Query query = em.createNativeQuery(sql, Game.class);
            query.setParameter("vendorId", vendorId);
            query.setParameter("nowInKolkata", nowInKolkata);
            query.setParameter("status", "SCHEDULED");

            List<Game> games = query.getResultList();

            for (Game game : games) {

                game.setScheduledAt(convertToKolkataTime(game.getScheduledAt()));

                if (!game.getScheduledAt().isAfter(nowInKolkata)) {
                    game.setStatus(GameStatus.ACTIVE);
                    game.setScheduledAt(nowInKolkata);
                    game.setUpdatedDate(nowInKolkata);
                    gameRepository.save(game);
                    System.out.println("Game ID: " + game.getId() + " status updated to ACTIVE.");
                }
            }

        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error updating game statuses: " + e.getMessage(), e);
        }
    }



}

