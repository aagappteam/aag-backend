package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.TournamentResultRecord;
import aagapp_backend.entity.tournament.TournamentRoundParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRoundParticipantRepository extends JpaRepository<TournamentRoundParticipant, Long> {

    List<TournamentRoundParticipant> findByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);

    Optional<TournamentRoundParticipant> findByTournamentIdAndPlayerIdAndRoundNumber(Long tournamentId, Long playerId, Integer roundNumber);

    long countByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);

    void deleteByTournamentIdAndPlayerIdAndRoundNumber(Long tournamentId, Long playerId, Integer roundNumber);

    List<TournamentRoundParticipant> findByTournamentIdAndRoundNumberAndStatus(Long tournamentId, Integer roundNumber, String readyToPlay);
}
