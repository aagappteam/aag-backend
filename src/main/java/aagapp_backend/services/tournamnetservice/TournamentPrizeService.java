package aagapp_backend.services.tournamnetservice;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TournamentPrizeService {

    public Map<Integer, Double> calculatePrizeDistribution(Double totalPrize, int totalRounds) {
        Map<Integer, Double> prizePerRound = new HashMap<>();

        double prizeEachRound = totalPrize / totalRounds;

        for (int round = 1; round <= totalRounds; round++) {
            prizePerRound.put(round, prizeEachRound);
        }

        return prizePerRound;
    }

    public Map<Integer, Double> calculatePrizePerWinner(Map<Integer, Double> prizePerRound, Map<Integer, Integer> winnersPerRound) {
        Map<Integer, Double> winnerPayouts = new HashMap<>();

        for (Map.Entry<Integer, Double> entry : prizePerRound.entrySet()) {
            int round = entry.getKey();
            double totalPrize = entry.getValue();
            int winners = winnersPerRound.getOrDefault(round, 1);

            double payoutPerWinner = totalPrize / winners;
            winnerPayouts.put(round, payoutPerWinner);
        }

        return winnerPayouts;
    }
}
