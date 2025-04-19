package aagapp_backend.services.dashboard;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.dto.leaderboard.*;
import aagapp_backend.entity.game.Game;
import aagapp_backend.repository.game.GameRepository;
import aagapp_backend.repository.game.GameRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private GameRoomRepository roomRepo;

    public DashboardResponse getDashboard(int latestPage, int popularPage, int lowFeePage, int mediumFeePage, int highFeePage, int influencerPage, int size) {

        Pageable pageable = PageRequest.of(latestPage, size, Sort.by("createdDate").descending());
        Page<Game> latest = gameRepo.findAll(pageable);

        Pageable popPageable = PageRequest.of(popularPage, size);
        List<Object[]> popularRaw = roomRepo.findPopularGames(popPageable);
        List<PopularGameDTO> popularGames = popularRaw.stream()
                .map(obj -> new PopularGameDTO(mapToDTO((Game) obj[0]), (Long) obj[1]))
                .toList();

        Page<Game> lowFee = gameRepo.findByFeeBetween(0, 10, PageRequest.of(lowFeePage, size));
        Page<Game> mediumFee = gameRepo.findByFeeBetween(11, 50, PageRequest.of(mediumFeePage, size));
        Page<Game> highFee = gameRepo.findByFeeGreaterThanEqual(51, PageRequest.of(highFeePage, size));

        Page<Game> influencerGames = gameRepo.findByVendorEntityIsNotNull(PageRequest.of(influencerPage, size));

        return new DashboardResponse(
                toPageDTO(latest.map(this::mapToDTO)),
                new PageDTO<>(popularGames, popularPage, popPageable.getPageSize(), popularGames.size()),
                new EntryFeeGamesDTO(
                        toPageDTO(lowFee.map(this::mapToDTO)),
                        toPageDTO(mediumFee.map(this::mapToDTO)),
                        toPageDTO(highFee.map(this::mapToDTO))
                ),
                toPageDTO(influencerGames.map(g -> new InfluencerGameDTO(mapToDTO(g), g.getVendorEntity().getName())))
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
                game.getCreatedDate(),
                game.getScheduledAt(),
                game.getEndDate(),
                game.getMinPlayersPerTeam(),
                game.getMaxPlayersPerTeam(),
                game.getVendorEntity() != null ? game.getVendorEntity().getName() : null,
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
