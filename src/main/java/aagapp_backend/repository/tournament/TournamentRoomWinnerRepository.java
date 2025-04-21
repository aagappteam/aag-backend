package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.TouranamentRoomWinner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRoomWinnerRepository extends JpaRepository<TouranamentRoomWinner, Long> {

    Page<TouranamentRoomWinner> findByTournamentRoomId(Long tournamentId, Pageable pageable);
}
