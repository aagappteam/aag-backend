package aagapp_backend.repository.league;

import aagapp_backend.entity.game.GameResultRecord;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeagueResultRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeagueResultRecordRepository extends JpaRepository<LeagueResultRecord, Long> {

    List<LeagueResultRecord> findByLeague_IdAndRoomId(Long leagueId, Long roomId);

    List<LeagueResultRecord> findByLeague_Id(Long leagueId);
}
