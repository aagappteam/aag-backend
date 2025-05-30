package aagapp_backend.services.admin;

import aagapp_backend.dto.DashboardResponseAdmin;
import aagapp_backend.dto.NotificationDTO;
import aagapp_backend.dto.game.GameResultRecordDTO;
import aagapp_backend.entity.game.GameResultRecord;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.repository.game.GameResultRecordRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.league.LeagueResultRecordRepository;
import aagapp_backend.repository.tournament.TournamentResultRecordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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


/*    public Page<GameResultRecordDTO> getAllGameResults(Pageable pageable) {
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
    }*/

/*    @Transactional
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
    }*/


}
