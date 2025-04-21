package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.TournamentRoomWinner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRoomWinnerRepository extends JpaRepository<TournamentRoomWinner, Long> {

    Page<TournamentRoomWinner> findByTournamentRoomId(Long tournamentId, Pageable pageable);
}
