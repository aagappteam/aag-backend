package aagapp_backend.repository.tournament;

import aagapp_backend.entity.league.LeagueResultRecord;
import aagapp_backend.entity.tournament.TournamentResultRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentResultRecordRepository extends JpaRepository<TournamentResultRecord, Long> {

    List<LeagueResultRecord> findByTournament_IdAndRoomId(Long leagueId, Long roomId);

    List<LeagueResultRecord> findByTournament_Id(Long leagueId);
}
