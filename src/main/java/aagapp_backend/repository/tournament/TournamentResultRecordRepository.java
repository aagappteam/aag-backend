package aagapp_backend.repository.tournament;

import aagapp_backend.entity.league.LeagueResultRecord;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.entity.tournament.TournamentResultRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface TournamentResultRecordRepository extends JpaRepository<TournamentResultRecord, Long> {

    List<TournamentResultRecord> findByTournament_IdAndRoomId(Long leagueId, Long roomId);

    List<TournamentResultRecord> findByTournament_Id(Long leagueId);

    Page<TournamentResultRecord> findByTournament_IdAndRoomId(Long leagueId, Long roomId, Pageable pageable);

    Page<TournamentResultRecord> findByTournament_Id(Long leagueId, Pageable pageable);
    List<TournamentResultRecord> findByTournamentIdAndIsWinnerTrueAndRound(Long tournamentId, int round);
    List<TournamentResultRecord> findByTournamentIdAndRound(Long tournamentId, int round);
    Page<TournamentResultRecord> findByTournamentIdAndIsWinnerTrue(Long tournamentId, Pageable pageable);


    List<TournamentResultRecord> findByTournamentIdAndRoundAndIsWinnerTrue(Long id, int round);

    @Query("SELECT CASE WHEN COUNT(trr) > 0 THEN TRUE ELSE FALSE END " +
            "FROM TournamentResultRecord trr " +
            "WHERE trr.tournament.id = :tournamentId " +
            "AND trr.player.playerId = :playerId " +
            "AND trr.round = :round " +
            "AND trr.isWinner = TRUE")
    boolean existsByTournamentIdAndPlayerIdAndRoundAndIsWinnerTrue(@Param("tournamentId") Long tournamentId,
                                                                   @Param("playerId") Long playerId,
                                                                   @Param("round") Integer round);

    List<TournamentResultRecord> findByTournamentIdAndStatus(Long tournamentId, String readyToPlay);

    List<TournamentResultRecord> findTopByPlayerAndTournamentOrderByPlayedAtDesc(Player player, Tournament tournament);

    List<TournamentResultRecord> findByTournamentIdAndRoundAndStatus(Long tournamentId, int round, String status);

    @Query("SELECT COUNT(tr) FROM TournamentResultRecord tr " +
            "WHERE tr.tournament.id = :tournamentId " +
            "AND tr.round = :round " +
            "AND tr.isWinner = true " +
            "AND tr.score = 0")
    long countFreePassWinners(@Param("tournamentId") Long tournamentId,
                              @Param("round") Integer round);



    @Query("SELECT r FROM TournamentResultRecord r " +
            "WHERE r.tournament.id = :tournamentId " +
            "AND r.player.playerId = :playerId " +
            "AND r.round = :round " +
            "AND r.isWinner = true")
    Optional<TournamentResultRecord> findWinnerRecord(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId,
            @Param("round") Integer round);

    long countByTournamentIdAndRoundAndStatus(Long tournamentId, int roundNumber, String freePass);


    @Query("SELECT r FROM TournamentResultRecord r " +
            "WHERE r.tournament.id = :tournamentId " +
            "AND r.player.playerId = :playerId " +
            "AND r.round = :round")
    Optional<TournamentResultRecord> findByTournamentIdAndPlayerIdAndRound(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId,
            @Param("round") Integer round);

    List<TournamentResultRecord> findByTournamentIdAndRoundAndStatusIn(Long tournamentId, int roundNumber, List<String> list);
}
