package aagapp_backend.repository.tournament;

import aagapp_backend.entity.league.LeagueRoom;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.tournament.TournamentRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TournamentRoomRepository extends JpaRepository<TournamentRoom , Long> {

    List<TournamentRoom> findByTournamentIdAndStatus(Long tournamentId, String open);

    List<TournamentRoom> findByTournamentId(Long tournamentId);

    List<TournamentRoom> findByStatus(String active);

    @Query("SELECT COALESCE(SUM(r.maxParticipants), 0) FROM TournamentRoom r WHERE r.tournament.id = :tournamentId")
    long sumMaxParticipantsByTournamentId(@Param("tournamentId") Long tournamentId);


}
