package aagapp_backend.services.tournamnetservice;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.notification.Notification;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.tournament.TournamentResultRecord;
import aagapp_backend.entity.wallet.Wallet;
import aagapp_backend.repository.NotificationRepository;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.AagGameRepository;
import aagapp_backend.repository.game.PlayerRepository;
import aagapp_backend.repository.game.ThemeRepository;
import aagapp_backend.repository.tournament.TournamentPlayerRegistrationRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.tournament.TournamentResultRecordRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.repository.wallet.VendorWalletRepository;
import aagapp_backend.repository.wallet.WalletRepository;
import aagapp_backend.services.CommonService;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.firebase.NotoficationFirebase;
import aagapp_backend.services.social.FollowerNotificationService;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TournamentPrizeService {


    private final Dotenv dotenv = Dotenv.load();


    @Autowired
    private VendorWalletRepository walletRepo;



    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private CustomCustomerRepository customCustomerRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private TournamentResultRecordRepository tournamentResultRecordRepository;
/*    @Autowired
    public void setTournamentRepository(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Autowired
    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }



    @Autowired
    public void setTournamentResultRecordRepository(TournamentResultRecordRepository tournamentResultRecordRepository) {
        this.tournamentResultRecordRepository = tournamentResultRecordRepository;
    }


    */

    @Transactional
    public void distributeRoundPrize(Tournament tournament, int round) {
        BigDecimal roundPrize = tournament.getRoomprize();

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

        if (winnersCount == 0) return;

        BigDecimal totalCash = roundPrize.multiply(Constant.TOURNAMENT_PRIZE_POOL_SENT_TO_USER);
        BigDecimal totalBonus = roundPrize.multiply(Constant.TOURNAMENT_PRIZE_POOL_SENT_AS_BONUS);

       /* System.out.println("Total Cash: " + totalCash);
        System.out.println("Total Bonus: " + totalBonus);
*/

        BigDecimal cashPerWinner = totalCash.divide(BigDecimal.valueOf(winnersCount), 2, RoundingMode.HALF_UP);
        BigDecimal bonusPerWinner = totalBonus.divide(BigDecimal.valueOf(winnersCount), 2, RoundingMode.HALF_UP);
        BigDecimal prizePerWinner = cashPerWinner.add(bonusPerWinner);

/*        System.out.println("Total cashPerWinner: " + cashPerWinner);
        System.out.println("Total bonusPerWinner: " + bonusPerWinner);
        System.out.println("Total prizePerWinner: " + prizePerWinner);*/

        for (TournamentResultRecord winner : uniqueWinners) {
            // Create notification
            Notification notification = new Notification();
            notification.setAmount(prizePerWinner.doubleValue());
            notification.setDetails("You won Rs. " + prizePerWinner.stripTrailingZeros().toPlainString() + " in Round " + round);
            notification.setDescription("Round Prize");
            notification.setRole("Customer");
            notification.setCustomerId(winner.getPlayer().getCustomer().getId());
            notification.setName(Optional.ofNullable(winner.getPlayer().getCustomer().getName()).orElse("N/A"));
            notificationRepository.save(notification);
            // Update winning wallet
            Wallet wallet = walletRepository.findByCustomCustomer_Id(winner.getPlayer().getCustomer().getId());
            System.out.println(winner.getPlayer().getCustomer().getId());

            if (wallet.getWinningAmount() == null) {
                wallet.setWinningAmount(BigDecimal.ZERO);
            }
            wallet.setWinningAmount(wallet.getWinningAmount().add(cashPerWinner));
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(wallet);

//            System.out.println("wallet: " + wallet.getWalletId());


            // Update result amount
            winner.setAmmount(Optional.ofNullable(winner.getAmmount()).orElse(BigDecimal.ZERO).add(prizePerWinner));

            // Update bonus balance
            CustomCustomer customCustomer = winner.getPlayer().getCustomer();
            BigDecimal currentBonus = Optional.ofNullable(customCustomer.getBonusBalance()).orElse(BigDecimal.ZERO);
            customCustomer.setBonusBalance(currentBonus.add(bonusPerWinner));
            customCustomerRepository.save(customCustomer);
            tournamentResultRecordRepository.save(winner);
        }
    }
}
