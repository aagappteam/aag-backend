package aagapp_backend.repository.league;

import aagapp_backend.entity.league.League;
import aagapp_backend.entity.league.LeaguePass;
import aagapp_backend.entity.players.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaguePassRepository extends JpaRepository<LeaguePass, Long> {
    Optional<LeaguePass> findByPlayerAndLeague(Player player, League league);

}
