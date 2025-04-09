package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.TournamentRoundWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRoundWinnerRepository extends JpaRepository<TournamentRoundWinner, Long> {

    List<TournamentRoundWinner> findByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);
    long countByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);

    Optional<TournamentRoundWinner> findByTournamentIdAndPlayerIdAndRoundNumber(Long tournamentId, Long playerId, Integer roundNumber);

    // Custom delete method based on tournamentId and playerId
    void deleteByTournamentIdAndPlayerId(Long tournamentId, Long playerId);
}
