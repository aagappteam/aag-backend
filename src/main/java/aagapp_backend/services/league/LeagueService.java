package aagapp_backend.services.league;

import aagapp_backend.dto.LeagueRequest;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.enums.LeagueStatus;
import aagapp_backend.repository.league.LeagueRepository;
import aagapp_backend.services.GameService.GameService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LeagueService {
    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private GameService gameservice;


    @Autowired
    private ExceptionHandlingService exceptionHandling;
   /* public Page<League> getAllLeagues(String status, Long vendorId, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<League> cq = cb.createQuery(League.class);
        Root<League> root = cq.from(League.class);

        // List of predicates for dynamic query
        Predicate predicate = cb.conjunction();  // Start with an always-true predicate

        if (status != null) {
            predicate = cb.and(predicate, cb.equal(root.get("status"), LeagueStatus.valueOf(status)));
        }

        if (vendorId != null) {
            predicate = cb.and(predicate, cb.equal(root.get("vendorEntity").get("id"), vendorId));
        }

        cq.where(predicate);

        TypedQuery<League> query = em.createQuery(cq);

        // For pagination
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        int firstResult = pageNumber * pageSize;

        // Set the pagination limits
        query.setFirstResult(firstResult);
        query.setMaxResults(pageSize);

        // Execute the query and return a Page
        long totalResults = getTotalLeagues(status, vendorId);
        return new PageImpl<>(query.getResultList(), pageable, totalResults);
    }

    // Helper method to get the total count of records for pagination
    private long getTotalLeagues(String status, Long vendorId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<League> countRoot = countQuery.from(League.class);

        Predicate predicate = cb.conjunction();  // Start with an always-true predicate

        if (status != null) {
            predicate = cb.and(predicate, cb.equal(countRoot.get("status"), LeagueStatus.valueOf(status)));
        }

        if (vendorId != null) {
            predicate = cb.and(predicate, cb.equal(countRoot.get("vendorEntity").get("id"), vendorId));
        }

        countQuery.where(predicate);
        countQuery.select(cb.count(countRoot));

        // Execute the count query
        return em.createQuery(countQuery).getSingleResult();
    }*/

    public League publishLeague(LeagueRequest leagueRequest, Long vendorId) {

        try {
            VendorEntity vendorEntity = em.find(VendorEntity.class, vendorId);
            if (vendorEntity == null) {
                throw new RuntimeException("No records found for vendor");
            }

            // Create a new League object and set its properties
            League league = new League();
            league.setName(leagueRequest.getName());
            league.setDescription(leagueRequest.getDescription());
            league.setEntryFee(leagueRequest.getEntryFee());
            league.setVendorEntity(vendorEntity);

            ZonedDateTime nowInKolkata = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            if (leagueRequest.getScheduledAt() != null) {
                ZonedDateTime scheduledAtInKolkata = leagueRequest.getScheduledAt().withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

                // Ensure the league is scheduled at least 4 hours in advance
                if (scheduledAtInKolkata.isBefore(nowInKolkata.plusHours(4))) {
                    throw new IllegalArgumentException("The league must be scheduled at least 4 hours in advance.");
                }

                // Set scheduledAt instead of startDate
                league.setScheduledAt(scheduledAtInKolkata);
            } else {
                league.setScheduledAt(nowInKolkata.plusMinutes(15));
            }

            league.setEndDate(league.getScheduledAt().plusDays(7));

            league.setMinPlayersPerTeam(leagueRequest.getMinPlayersPerTeam());
            league.setMaxPlayersPerTeam(leagueRequest.getMaxPlayersPerTeam());

            league.setStatus(LeagueStatus.ACTIVE);

            return leagueRepository.save(league);



        } catch (IllegalArgumentException e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new IllegalArgumentException("Invalid league schedule: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error occurred while publishing the league: " + e.getMessage(), e);
        }
    }

    public League updateLeague(Long vendorId , Long leagueId, LeagueRequest leagueRequest) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
/*
        league.setName(leagueRequest.getName());
        league.setEndDate(leagueRequest.getEndDate());
        league.setStatus(leagueRequest.getStatus());*/
        return leagueRepository.save(league);
    }

    public Page<League> findLeaguesByVendor(Long vendorId, String status, Pageable pageable) {
        try {
            String sql = "SELECT * FROM aag_league l WHERE l.vendor_id = :vendorId";

            if (status != null && !status.isEmpty()) {
                sql += " AND l.status = :status";
            }

            Query query = em.createNativeQuery(sql, League.class);
            query.setParameter("vendorId", vendorId);

            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }

            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());

            List<League> leagues = query.getResultList();

            // Convert date fields to the desired time zone (e.g., Asia/Kolkata)
            leagues.forEach(league -> {
                if (league.getCreatedDate() != null) {
                    league.setCreatedDate(gameservice.convertToKolkataTime(league.getCreatedDate()));
                }
                if (league.getUpdatedDate() != null) {
                    league.setUpdatedDate(gameservice.convertToKolkataTime(league.getUpdatedDate()));
                }
                if (league.getScheduledAt() != null) {
                    league.setScheduledAt(gameservice.convertToKolkataTime(league.getScheduledAt()));
                }
            });

            // Create the SQL query to count the total number of leagues for pagination
            String countSql = "SELECT COUNT(*) FROM aag_league l WHERE l.vendor_id = :vendorId";
            if (status != null && !status.isEmpty()) {
                countSql += " AND l.status = :status";
            }

            // Create the count query
            Query countQuery = em.createNativeQuery(countSql);
            countQuery.setParameter("vendorId", vendorId);
            if (status != null && !status.isEmpty()) {
                countQuery.setParameter("status", status);
            }

            Long count = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(leagues, pageable, count);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving leagues by vendor ID: " + vendorId, e);
        }
    }

}

