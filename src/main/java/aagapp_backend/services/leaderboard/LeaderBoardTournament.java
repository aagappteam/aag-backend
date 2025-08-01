package aagapp_backend.services.leaderboard;
import aagapp_backend.dto.game.GameLeaderboardResponseDTOTornamentAdmin;
import aagapp_backend.dto.game.LeaderboardResponseDTOTournamentAdmin;
import aagapp_backend.dto.leaderboard.TournamentLeaderboardDto;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.ThemeEntity;
import aagapp_backend.entity.players.Player;
import aagapp_backend.entity.tournament.TournamentResultRecord;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.game.ThemeRepository;
import aagapp_backend.repository.tournament.TournamentRepository;
import aagapp_backend.repository.tournament.TournamentResultRecordRepository;
import aagapp_backend.repository.tournament.TournamentRoomRepository;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderBoardTournament {

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
    public LeaderboardResponseDTOTournamentAdmin getLeaderboardforvendor(Long tournamentId, Pageable pageable, boolean winnersOnly){
        try {
            // 1. Fetch the game details
            Optional<Tournament> gameOpt = tournamentRepository.findById(tournamentId);
            if (gameOpt.isEmpty()) {
                throw new BusinessException("Game not found with ID: " + tournamentId, HttpStatus.BAD_REQUEST);
            }
            Tournament game = gameOpt.get();

            // 2. Fetch the theme associated with the game
            Optional<ThemeEntity> themeOpt = themeRepository.findById(game.getTheme().getId());
            if (themeOpt.isEmpty()) {
                throw new BusinessException("Theme not found for game with ID: " + tournamentId, HttpStatus.BAD_REQUEST);
            }
            ThemeEntity theme = themeOpt.get();

//            // 3. Fetch all tournament results (only winners)
//            Page<TournamentResultRecord> resultPage = tournamentResultRecordRepository
//                    .findByTournamentIdAndIsWinnerTrue(tournamentId, Pageable.unpaged()); // fetch all for aggregation

            Page<TournamentResultRecord> resultPage;
            if (winnersOnly) {
                resultPage = tournamentResultRecordRepository.findByTournamentIdAndIsWinnerTrue(tournamentId, Pageable.unpaged());
            } else {
                resultPage = tournamentResultRecordRepository.findByTournamentIdAndIsWinnerFalse(tournamentId, Pageable.unpaged());
            }


            List<TournamentResultRecord> results = resultPage.getContent();

            // 4. Fetch total players in game rooms
//            long totalPlayers = tournamentRoomRepository.sumMaxParticipantsByTournamentId(tournamentId);
            long totalPlayers = Optional.ofNullable(
                    tournamentRoomRepository.sumMaxParticipantsByTournamentId(tournamentId)
            ).orElse(0L);

            // 5. Aggregate players
            Map<Long, GameLeaderboardResponseDTOTornamentAdmin> playerMap = new HashMap<>();

            for (TournamentResultRecord result : results) {
                Player player = result.getPlayer();
                Long playerId = player.getPlayerId();

                // Fetch player details
                Optional<CustomCustomer> playerDetailsOpt = customCustomerRepository.findById(playerId);
                if (playerDetailsOpt.isEmpty()) {
                    throw new BusinessException("Player details not found for player ID: " + playerId, HttpStatus.BAD_REQUEST);
                }
                CustomCustomer playerDetails = playerDetailsOpt.get();

                double winningAmount = result.getAmmount() != null ? result.getAmmount().doubleValue() : 0.0;

                GameLeaderboardResponseDTOTornamentAdmin existing = playerMap.get(playerId);

                if (existing == null) {
                    GameLeaderboardResponseDTOTornamentAdmin dto = new GameLeaderboardResponseDTOTornamentAdmin();
                    dto.setPlayerId(playerId);
                    dto.setMobileNumber(playerDetails.getMobileNumber());
                    dto.setState(playerDetails.getState());
                    dto.setPlayerName(playerDetails.getName());
                    dto.setProfilePicture(playerDetails.getProfilePic());
                    dto.setRound(result.getRound());
                    dto.setScore(result.getScore());
//                    dto.setWinningammount(winningAmount);
                    dto.setWinningammount(BigDecimal.valueOf(winningAmount).setScale(2, RoundingMode.HALF_UP).doubleValue());

                    playerMap.put(playerId, dto);
                } else {
                    // Sum the winning amount
//                    existing.setWinningammount(existing.getWinningammount() + winningAmount);
                    double updatedAmount = BigDecimal.valueOf(existing.getWinningammount() + winningAmount)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    existing.setWinningammount(updatedAmount);

                    // Update round and score if this is a higher round
                    if (result.getRound() > existing.getRound()) {
                        existing.setRound(result.getRound());
                        existing.setScore(result.getScore());
                    }
                }
            }

            // Convert to list
            List<GameLeaderboardResponseDTOTornamentAdmin> playerList = new ArrayList<>(playerMap.values());

            // ✅ Sort: Highest round first, then by score descending
            playerList.sort((a, b) -> {
                int cmp = Integer.compare(b.getRound(), a.getRound());
                if (cmp != 0) return cmp;
                return Integer.compare(b.getScore(), a.getScore());
            });


            // 6. Apply manual paging (if required)
            int pageSize = pageable.getPageSize();
            int currentPage = pageable.getPageNumber();
            int totalItems = playerList.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);

            int start = Math.min(currentPage * pageSize, totalItems);
            int end = Math.min(start + pageSize, totalItems);

            List<GameLeaderboardResponseDTOTornamentAdmin> pagedPlayers = playerList.subList(start, end);

            // 7. Build response
            LeaderboardResponseDTOTournamentAdmin response = new LeaderboardResponseDTOTournamentAdmin();
            response.setGameName(game.getName());
            response.setGameFee((double) game.getEntryFee());
            response.setTotalprizepool(game.getTotalPrizePool().toString());
            response.setRoundprize(game.getRoomprize().toString());
            response.setGameIcon(game.getGameUrl());
            response.setThemeName(theme.getName());
            response.setTotalPlayers((int) totalPlayers);
            response.setPlayers(pagedPlayers);
            response.setTotalPages(totalPages);
            response.setTotalItems(totalItems);
            response.setCurrentPage(currentPage);

            return response;

        } catch (BusinessException e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return null;
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            throw new RuntimeException("Error fetching leaderboard: " + e.getMessage(), e);
        }
    }


    public LeaderboardResponseDTOTournamentAdmin getLeaderboardforvendorold(Long tournamentId, Pageable pageable) {
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
            List<GameLeaderboardResponseDTOTornamentAdmin> playerList = new ArrayList<>();
            for (TournamentResultRecord result : results) {
                Player player = result.getPlayer();

                // Fetch player details
                Optional<CustomCustomer> playerDetails = customCustomerRepository.findById(player.getPlayerId());
                if (playerDetails.isEmpty()) {
                    throw new BusinessException("Player details not found for player ID: " + player.getPlayerId() , HttpStatus.BAD_REQUEST);
                }

                GameLeaderboardResponseDTOTornamentAdmin playerDTO = new GameLeaderboardResponseDTOTornamentAdmin();
                playerDTO.setPlayerId(player.getPlayerId());
                playerDTO.setMobileNumber(playerDetails.get().getMobileNumber());
                playerDTO.setState(playerDetails.get().getState());
                playerDTO.setRound(result.getRound());
                playerDTO.setPlayerName(playerDetails.get().getName());
                playerDTO.setProfilePicture(playerDetails.get().getProfilePic());
                playerDTO.setScore(result.getScore());
                playerDTO.setWinningammount(result.getAmmount() != null ? result.getAmmount().doubleValue() : 0.0);

                playerList.add(playerDTO);
            }

            // 6. Return final wrapped DTO with pagination
            LeaderboardResponseDTOTournamentAdmin response = new LeaderboardResponseDTOTournamentAdmin();
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
