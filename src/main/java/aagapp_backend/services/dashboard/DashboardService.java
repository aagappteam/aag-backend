package aagapp_backend.services.dashboard;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.dto.TopVendorDto;
import aagapp_backend.dto.leaderboard.*;
import aagapp_backend.entity.game.Game;
import aagapp_backend.enums.GameRoomStatus;
import aagapp_backend.enums.GameStatus;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.game.GameRoomRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardService {

    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private GameRoomRepository roomRepo;
    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private GameRoomRepository gameRoomRepository;

/*
    public DashboardResponse getDashboard(int latestPage, int popularPage, int feePage, int influencerPage, int size) {

        Pageable latestPageable = PageRequest.of(latestPage, size, Sort.by("createdDate").descending());
        Page<Game> latest = gameRepo.findAll(latestPageable);

        Pageable popPageable = PageRequest.of(popularPage, size);
        List<Object[]> popularRaw = roomRepo.findPopularGames(popPageable);
        List<PopularGameDTO> popularGames = popularRaw.stream()
                .map(obj -> new PopularGameDTO(mapToDTO((Game) obj[0]), (Long) obj[1]))
                .toList();

        // Fee based - All games sorted by fee DESC
        Page<Game> feeSortedGames = gameRepo.findAll(
                PageRequest.of(feePage, size, Sort.by(Sort.Direction.DESC, "fee"))
        );

        // Influencer - Top vendor & their games
        List<TopVendorDto> topVendors = vendorRepository.findTopVendorsWithFollowerCount(PageRequest.of(0, 1));
        List<InfluencerGameDTO> influencerGames = new ArrayList<>();
        if (!topVendors.isEmpty()) {
            TopVendorDto top = topVendors.get(0);
            List<Game> vendorGames = gameRepo.findByVendorId(top.getVendorId(), PageRequest.of(influencerPage, size));
            influencerGames = vendorGames.stream()
                    .map(g -> new InfluencerGameDTO(mapToDTO(g), top.getVendorName()))
                    .toList();
        }

        return new DashboardResponse(
                toPageDTO(latest.map(this::mapToDTO)),
                new PageDTO<>(popularGames, popularPage, popPageable.getPageSize(), popularGames.size()),
                toPageDTO(feeSortedGames.map(this::mapToDTO)),
                new PageDTO<>(influencerGames, influencerPage, 1, influencerGames.size())
        );
    }
*/

    public DashboardResponse getDashboard(int latestPage, int popularPage, int feePage, int influencerPage, int size) {

        // Current timestamp for filtering expired games
        ZonedDateTime currentDate = ZonedDateTime.now();

        // Latest Games: Filtered by ACTIVE status and not expired
        Pageable latestPageable = PageRequest.of(latestPage, size, Sort.by("createdDate").descending());
        Page<Game> latest = gameRepo.findAllByStatusAndEndDateAfter(GameStatus.ACTIVE, currentDate, latestPageable);

        // Popular Games: Based on game room count, ACTIVE games only, not expired
        Pageable popPageable = PageRequest.of(popularPage, size);
        List<GameRoomStatus> statuses = List.of(GameRoomStatus.ONGOING, GameRoomStatus.INITIALIZED, GameRoomStatus.COMPLETED);
        GameStatus gameStatus = GameStatus.ACTIVE;

        List<Object[]> popularRaw = gameRoomRepository.findPopularGames(statuses, gameStatus, currentDate, popPageable);
        List<PopularGameDTO> popularGames = popularRaw.stream()
                .map(obj -> new PopularGameDTO(mapToDTO((Game) obj[0]), (Long) obj[1]))
                .toList();

        // Fee Sorted Games: Sorted DESC by fee, ACTIVE, not expired
        Page<Game> feeSortedGames = gameRepo.findAllByStatusAndEndDateAfter(
                GameStatus.ACTIVE,
                currentDate,
                PageRequest.of(feePage, size, Sort.by(Sort.Direction.DESC, "fee"))
        );

        // Pick the  vendor with followers > 0
        List<TopVendorDto> topVendors = vendorRepository.findTopVendorsWithFollowerCount(PageRequest.of(0, 10)); // or any number you want
        List<InfluencerGameDTO> influencerGames = new ArrayList<>();

        // Filter vendors with followerCount > 0
        topVendors.stream()
                .filter(v -> v.getFollowerCount() > 0)
                .forEach(top -> {
                    List<Game> vendorGames = gameRepo.findByVendorIdAndStatusAndEndDateAfter(
                            top.getVendorId(),
                            GameStatus.ACTIVE,
                            currentDate,
                            PageRequest.of(influencerPage, size)
                    );

                    List<InfluencerGameDTO> vendorGameDTOs = vendorGames.stream()
                            .map(g -> new InfluencerGameDTO(mapToDTO(g), top.getVendorName()))
                            .toList();

                    influencerGames.addAll(vendorGameDTOs);
                });


        return new DashboardResponse(
                toPageDTO(latest.map(this::mapToDTO)),
                new PageDTO<>(popularGames, popularPage, popPageable.getPageSize(), popularGames.size()),
                toPageDTO(feeSortedGames.map(this::mapToDTO)),
                new PageDTO<>(influencerGames, influencerPage, 1, influencerGames.size())
        );
    }
    private GetGameResponseDTO mapToDTO(Game game) {
        return new GetGameResponseDTO(
                game.getId(),
                game.getName(),
                game.getFee(),
                game.getMove(),
                game.getStatus(),
                game.getShareableLink(),
                game.getAaggameid(),
                game.getImageUrl(),
                game.getTheme() != null ? game.getTheme().getName() : null,
                game.getTheme() != null ? game.getTheme().getImageUrl() : null,
                game.getCreatedDate() != null ? game.getCreatedDate() : null,

                game.getScheduledAt() != null ? game.getScheduledAt() : null,

                game.getEndDate() != null ? game.getEndDate() : null,
                game.getMinPlayersPerTeam(),
                game.getMaxPlayersPerTeam(),
                game.getVendorEntity() != null ? game.getVendorEntity().getFirst_name() : null,
                game.getVendorEntity() != null ? game.getVendorEntity().getProfilePic() : null
        );
    }

    private <T> PageDTO<T> toPageDTO(Page<T> page) {
        return new PageDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}
