package aagapp_backend.repository.tournament;

import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.tournament.TournamentRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentRoomRepository extends JpaRepository<TournamentRoom , Long> {

    List<TournamentRoom> findByTournamentIdAndStatus(Long tournamentId, String open);

    List<TournamentRoom> findByTournamentId(Long tournamentId);
}
