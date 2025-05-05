package aagapp_backend.services.leaderboard;

import aagapp_backend.dto.GameLeaderboardResponseDTO;
import aagapp_backend.dto.LeaderboardDto;
import aagapp_backend.dto.LeaderboardResponseDTO;
import aagapp_backend.dto.leaderboard.TournamentLeaderboardDto;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.league.LeagueResultRecord;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.TournamentResultRecord;
import aagapp_backend.entity.tournament.TournamentRoomWinner;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.ThemeRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.tournament.TournamentResultRecordRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.repository.tournament.TournamentRoomWinnerRepository;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaderBoardTournament {

    @Autowired
    private TournamentRoomWinnerRepository tournamentRoomWinnerRepository;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private TournamentRoomRepository tournamentRoomRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private TournamentResultRecordRepository tournamentResultRecordRepository;

    public GameLeaderboardResponseDTO getLeaderboardforvendor(Long tournamentId, Pageable pageable) {
        try{
            // 1. Fetch the game details
            Optional<Tournament> gameOpt = tournamentRepository.findById(tournamentId);
            if (gameOpt.isEmpty()) {
                throw new BusinessException("Game not found with ID: " + tournamentId , HttpStatus.BAD_REQUEST);
            }
            Tournament game = gameOpt.get();

            // 2. Fetch the theme associated with the game
            Optional<ThemeEntity> themeOpt = themeRepository.findById(game.getTheme().getId());
            if (themeOpt.isEmpty()) {
                throw new BusinessException("Theme not found for game with ID: " + tournamentId , HttpStatus.BAD_REQUEST);
            }
            ThemeEntity theme = themeOpt.get();

            // 3. Fetch all tournament results (players with scores, only winners)
            Page<TournamentResultRecord> resultPage = tournamentResultRecordRepository
                    .findByTournamentIdAndIsWinnerTrue(tournamentId, pageable);
            List<TournamentResultRecord> results = resultPage.getContent();

            // 4. Fetch total players in game rooms (sum of maxPlayers from GameRoom)
            long totalPlayers = tournamentRoomRepository.sumMaxParticipantsByTournamentId(tournamentId);

            // 5. Prepare player list
            List<LeaderboardResponseDTO> playerList = new ArrayList<>();
            for (TournamentResultRecord result : results) {
                Player player = result.getPlayer();

                // Fetch player details
                Optional<CustomCustomer> playerDetails = customCustomerRepository.findById(player.getPlayerId());
                if (playerDetails.isEmpty()) {
                    throw new BusinessException("Player details not found for player ID: " + player.getPlayerId() , HttpStatus.BAD_REQUEST);
                }

                LeaderboardResponseDTO playerDTO = new LeaderboardResponseDTO();
                playerDTO.setPlayerId(player.getPlayerId());
                playerDTO.setPlayerName(playerDetails.get().getName());
                playerDTO.setProfilePicture(playerDetails.get().getProfilePic());
                playerDTO.setScore(result.getScore());
                playerDTO.setWinningammount(result.getAmmount() != null ? result.getAmmount().doubleValue() : 0.0);

                playerList.add(playerDTO);
            }

            // 6. Return final wrapped DTO with pagination
            GameLeaderboardResponseDTO response = new GameLeaderboardResponseDTO();
            response.setGameName(game.getName());
            response.setGameFee((double) game.getEntryFee());
            response.setGameIcon(game.getGameUrl());
            response.setThemeName(theme.getName());
            response.setTotalPlayers((int) totalPlayers);
            response.setPlayers(playerList);
            response.setTotalPages(resultPage.getTotalPages());
            response.setTotalItems(resultPage.getNumberOfElements());
            response.setCurrentPage(resultPage.getNumber());

            return response;
        }catch (BusinessException e){
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        }
        catch (Exception e){
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leaderboard: " + e.getMessage(), e);

        }
    }



    public List<TournamentLeaderboardDto> getLeaderboard(Long leagueId, Long roomId, Boolean winnerFlag) {
     try{
         List<TournamentResultRecord> results;

         if (roomId != null) {
             results = tournamentResultRecordRepository.findByTournament_IdAndRoomId(leagueId, roomId);
         } else {
             results = tournamentResultRecordRepository.findByTournament_Id(leagueId);
         }

         if (results.isEmpty()) {
             throw new BusinessException("No game results found for this room and game." , HttpStatus.BAD_REQUEST);
         }

         // Filter based on winner param
         if (winnerFlag != null) {
             results = results.stream()
                     .filter(record -> winnerFlag.equals(record.getIsWinner()))
                     .collect(Collectors.toList());
         }

         results.sort(Comparator.comparing(TournamentResultRecord::getScore).reversed());

         return results.stream()
                 .map(this::mapToTournamentLeaderboardDto)
                 .collect(Collectors.toList());
     }catch (BusinessException e){
         exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
         return null;
     }catch (Exception e){
         exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
         throw new RuntimeException("Error fetching rooms: " + e.getMessage(), e);
     }
    }

    private TournamentLeaderboardDto mapToTournamentLeaderboardDto(TournamentResultRecord record) {
        Player player = record.getPlayer();

       /* BigDecimal totalCollection = BigDecimal.valueOf(record.getTournament().getEntryFee())
                .multiply(BigDecimal.valueOf(record.getTournament().getCurrentJoinedPlayers()));*/
        BigDecimal userWin = record.getAmmount();

        return new TournamentLeaderboardDto(
                player.getPlayerId(),
                player.getCustomer().getName(),
                player.getCustomer().getProfilePic(),
                record.getScore(),
                record.getIsWinner(),
                userWin,
                record.getRound(),
                record.getRoomId() != null ? record.getRoomId() : null
        );
    }


}
