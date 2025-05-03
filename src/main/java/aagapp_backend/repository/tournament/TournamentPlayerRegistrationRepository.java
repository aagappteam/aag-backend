package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.tournament.TournamentPlayerRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface TournamentPlayerRegistrationRepository extends JpaRepository<TournamentPlayerRegistration, Long> {

    // Find all registered players for a given tournament
//    List<TournamentPlayerRegistration> findByTournamentIdAndStatus(Long tournamentId, TournamentPlayerRegistration.RegistrationStatus status);

    // Find a player's registration for a specific tournament by player ID and tournament ID
    Optional<TournamentPlayerRegistration> findByTournamentIdAndPlayer_PlayerId(Long tournamentId, Long playerId);

    // Find a player's registration for a specific tournament by player ID and tournament ID and registration status
//    Optional<TournamentPlayerRegistration> findByTournamentIdAndPlayerIdAndStatus(Long tournamentId, Long playerId, TournamentPlayerRegistration.RegistrationStatus status);

    // Get the number of registered players for a specific tournament
    int countByTournamentIdAndStatus(Long tournamentId, TournamentPlayerRegistration.RegistrationStatus status);
    Page<TournamentPlayerRegistration> findByTournamentIdAndStatus(Long tournamentId, TournamentPlayerRegistration.RegistrationStatus status, Pageable pageable);

    List<TournamentPlayerRegistration> findByTournamentIdAndStatus(Long tournamentId, TournamentPlayerRegistration.RegistrationStatus registrationStatus);

}
