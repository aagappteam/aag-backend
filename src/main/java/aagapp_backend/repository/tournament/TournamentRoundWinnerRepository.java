package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.TournamentRoundWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRoundWinnerRepository extends JpaRepository<TournamentRoundWinner, Long> {

    List<TournamentRoundWinner> findByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);
    long countByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);
}
