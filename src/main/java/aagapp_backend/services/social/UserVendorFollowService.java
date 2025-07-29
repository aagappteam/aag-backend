package aagapp_backend.services.social;

import aagapp_backend.dto.GetGameResponseDTO;
import aagapp_backend.dto.TopHostWeekDto;
import aagapp_backend.dto.TopVendorDto;
import aagapp_backend.dto.TopVendorWeekDto;
import aagapp_backend.dto.tournament.TournamentGetallDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.league.League;
import aagapp_backend.entity.social.UserVendorFollow;
import aagapp_backend.entity.tournament.Tournament;
import aagapp_backend.enums.TournamentStatus;
import aagapp_backend.enums.VendorStatus;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.social.UserVendorFollowRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.gameservice.GameService;
import aagapp_backend.services.league.LeagueService;
import aagapp_backend.services.tournamnetservice.TournamentService;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserVendorFollowService {

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private VenderService vendorService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserVendorFollowRepository followRepo;

    @Autowired
    private CustomCustomerRepository userRepo;

    @Autowired
    private VendorRepository vendorRepo;


    @Autowired
    private GameService gameService;

    @Autowired
    private LeagueService leagueService;

    @Autowired
    private TournamentService tournamentService;


/*    public String followVendor(Long userId, Long vendorId) {
        if (!followRepo.existsByUserIdAndVendorId(userId, vendorId)) {
            UserVendorFollow follow = new UserVendorFollow();
            follow.setUser(userRepo.findById(userId).orElseThrow());
            follow.setVendor(vendorRepo.findById(vendorId).orElseThrow());
            follow.setFollowedAt(LocalDateTime.now());
            followRepo.save(follow);
            this.updateFollowerCount(vendorId);

        }
        return "Followed";
    }

    //   update follower count of vendor
    @Transactional
    public void updateFollowerCount(Long vendorId) {
        VendorEntity vendor = vendorService.getServiceProviderById(vendorId);

        vendor.setFollowercount(vendor.getFollowercount()==null?0:vendor.getFollowercount()+1);
        entityManager.persist(vendor);
    }*/

/*
    @Transactional
    private void removeollowerCount(Long vendorId) {
        VendorEntity vendor = vendorService.getServiceProviderById(vendorId);
        vendor.setFollowercount(vendor.getFollowercount()==null?0:vendor.getFollowercount()-1);

        entityManager.persist(vendor);
    }
*/

/*    public String unfollowVendor(Long userId, Long vendorId) {
        followRepo.findByUserIdAndVendorId(userId, vendorId)
                .ifPresent(followRepo::delete);
        this.removeollowerCount(vendorId);
        return "Unfollowed";
    }*/

    @Transactional
    public String unfollowVendor(Long userId, Long vendorId) {
        // Check if user exists
        CustomCustomer player = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        if (player.getStatus() != VendorStatus.ACTIVE) {
            throw new BusinessException("You are Suspended or Blocked", HttpStatus.BAD_REQUEST);
        }

        // Check if user is following the vendor
        UserVendorFollow follow = followRepo.findByUserIdAndVendorId(userId, vendorId)
                .orElseThrow(() -> new BusinessException("User is not following the vendor",HttpStatus.BAD_REQUEST));

        followRepo.delete(follow);

        // Check if vendor exists
        VendorEntity vendor = vendorRepo.findById(vendorId)
                .orElseThrow(() -> new NoSuchElementException("Vendor not found with ID: " + vendorId));

        // Decrement follower count if > 0
        int currentCount = vendor.getFollowercount() != null ? vendor.getFollowercount() : 0;
        if (currentCount > 0) {
            vendor.setFollowercount(currentCount - 1);
            entityManager.merge(vendor);
        }

        return "Unfollowed";
    }



    @Transactional
    public String followVendor(Long userId, Long vendorId) {
        if (followRepo.existsByUserIdAndVendorId(userId, vendorId)) {
            throw new BusinessException("Already followed",HttpStatus.BAD_REQUEST);
        }

        CustomCustomer user = userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

        if (user.getStatus() != VendorStatus.ACTIVE) {
            throw new BusinessException("You are Suspended or Blocked", HttpStatus.BAD_REQUEST);
        }

        VendorEntity vendor = vendorRepo.findById(vendorId)
                .orElseThrow(() -> new NoSuchElementException("Vendor not found with ID: " + vendorId));

        if (vendor.getStatus() != VendorStatus.ACTIVE) {
            throw new BusinessException("Vendor is suspended or blocked. Publishing is not allowed.", HttpStatus.BAD_REQUEST);
        }


        UserVendorFollow follow = new UserVendorFollow();
        follow.setUser(user);
        follow.setVendor(vendor);
        follow.setFollowedAt(LocalDateTime.now());
        followRepo.save(follow);

        vendor.setFollowercount((vendor.getFollowercount() != null ? vendor.getFollowercount() : 0) + 1);
        entityManager.merge(vendor);

        return "Followed";
    }





/*
    public Map<String, Object> getFollowersOfVendor(Long vendorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Get the vendor only once
        VendorEntity vendor = vendorRepo.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        Page<UserVendorFollow> followPage = followRepo.findByVendorId(vendorId, pageable);

        // Map user data
        List<Map<String, Object>> userList = followPage.getContent().stream().map(follow -> {
            CustomCustomer user = follow.getUser();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", user.getName());
            userInfo.put("profilePic", user.getProfilePic());
            userInfo.put("email", user.getEmail());
            userInfo.put("isFollowing", true); // because it's from the follow list

            return userInfo;
        }).collect(Collectors.toList());

        // Users pagination info
        Map<String, Object> userPageMap = new HashMap<>();
        userPageMap.put("content", userList);
        userPageMap.put("pageNumber", followPage.getNumber());
        userPageMap.put("pageSize", followPage.getSize());
        userPageMap.put("totalPages", followPage.getTotalPages());
        userPageMap.put("totalElements", followPage.getTotalElements());
        userPageMap.put("last", followPage.isLast());

        // Final response
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> vendorMap = new HashMap<>();
        vendorMap.put("name", vendor.getName());
        vendorMap.put("profilePic", vendor.getProfilePic());
        vendorMap.put("email", vendor.getPrimary_email());
        vendorMap.put("followerCount", followRepo.countByVendorId(vendorId));

        response.put("vendor", vendorMap);
        response.put("users", userPageMap);

        return response;
    }
*/
public Map<String, Object> getVendorsWithDetails(Long userId, int page, int size, String firstName) {
    Pageable pageable = PageRequest.of(page, size);

    CustomCustomer user = customCustomerRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("User not found" + userId, HttpStatus.BAD_REQUEST));

    Page<UserVendorFollow> followPage = followRepo.findByUserId(userId, Pageable.unpaged()); // fetch all, we'll filter manually

    // Stream map with optional filter
    Stream<UserVendorFollow> followStream = followPage.getContent().stream();

    if (firstName != null && !firstName.trim().isEmpty()) {
        String[] nameParts = firstName.trim().split(" ");
        String firstNamePart = nameParts[0].toLowerCase();
        String lastNamePart = nameParts.length > 1 ? nameParts[1].toLowerCase() : "";

        followStream = followStream.filter(follow -> {
            VendorEntity vendor = follow.getVendor();
            String vendorFirstName = vendor.getFirst_name() != null ? vendor.getFirst_name().toLowerCase() : "";
            String vendorLastName = vendor.getLast_name() != null ? vendor.getLast_name().toLowerCase() : "";

            if (nameParts.length == 1) {
                return vendorFirstName.contains(firstNamePart) || vendorLastName.contains(firstNamePart);
            } else {
                return vendorFirstName.contains(firstNamePart) && vendorLastName.contains(lastNamePart);
            }
        });
    }

    // Collect filtered list
    List<Map<String, Object>> filteredVendors = followStream.map(follow -> {
        VendorEntity vendor = follow.getVendor();

        Map<String, Object> vendorInfo = new HashMap<>();
        vendorInfo.put("name", vendor.getName());
        vendorInfo.put("id", vendor.getService_provider_id());
        vendorInfo.put("profilePic", vendor.getProfilePic());
        vendorInfo.put("email", vendor.getPrimary_email());
        vendorInfo.put("user_name", vendor.getUser_name());
        vendorInfo.put("followerCount", followRepo.countByVendorId(vendor.getService_provider_id()));
        vendorInfo.put("isFollowing", true);

        return vendorInfo;
    }).collect(Collectors.toList());

    // Manual pagination on filtered list
    int start = Math.min(page * size, filteredVendors.size());
    int end = Math.min(start + size, filteredVendors.size());
    List<Map<String, Object>> paginatedList = filteredVendors.subList(start, end);

    // Pagination info
    Map<String, Object> vendorPageMap = new HashMap<>();
    vendorPageMap.put("content", paginatedList);
    vendorPageMap.put("pageNumber", page);
    vendorPageMap.put("pageSize", size);
    vendorPageMap.put("totalPages", (int) Math.ceil((double) filteredVendors.size() / size));
    vendorPageMap.put("totalElements", filteredVendors.size());
    vendorPageMap.put("last", end == filteredVendors.size());

    // Final response
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> userMap = new HashMap<>();
    userMap.put("name", user.getName());
    userMap.put("profilePic", user.getProfilePic());
    userMap.put("email", user.getEmail());

    response.put("user", userMap);
    response.put("vendors", vendorPageMap);

    return response;
}


    public Map<String, Object> getVendorsWithDetails(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Get the user only once
        CustomCustomer user = customCustomerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch the follows (i.e., vendors followed by user)
        Page<UserVendorFollow> followPage = followRepo.findByUserId(userId, pageable);

        // Map vendor data only
        List<Map<String, Object>> vendorList = followPage.getContent().stream().map(follow -> {
            VendorEntity vendor = follow.getVendor();

            Map<String, Object> vendorInfo = new HashMap<>();
            vendorInfo.put("name", vendor.getName());
            vendorInfo.put("id", vendor.getService_provider_id());
            vendorInfo.put("profilePic", vendor.getProfilePic());
            vendorInfo.put("email", vendor.getPrimary_email());
            vendorInfo.put("followerCount", followRepo.countByVendorId(vendor.getService_provider_id()));
            vendorInfo.put("isFollowing", true); // it's from follow list, so always true

            return vendorInfo;
        }).collect(Collectors.toList());

        // Vendor pagination info
        Map<String, Object> vendorPageMap = new HashMap<>();
        vendorPageMap.put("content", vendorList);
        vendorPageMap.put("pageNumber", followPage.getNumber());
        vendorPageMap.put("pageSize", followPage.getSize());
        vendorPageMap.put("totalPages", followPage.getTotalPages());
        vendorPageMap.put("totalElements", followPage.getTotalElements());
        vendorPageMap.put("last", followPage.isLast());

        // Final response structure
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getName());
        userMap.put("profilePic", user.getProfilePic());
        userMap.put("email", user.getEmail());

        response.put("user", userMap);
        response.put("vendors", vendorPageMap);

        return response;
    }


    public List<TopVendorDto> getTopVendors() {
        return followRepo.findTopVendorsWithFollowerCount();
    }

    public boolean isUserFollowing(Long userId, Long vendorId) {
        return followRepo.existsByUserIdAndVendorId(userId, vendorId);
    }

    public Long getFollowerCountOfVendor(Long vendorId) {
        return followRepo.countFollowersByVendorId(vendorId);
    }




    private void sendPushNotification(String token, String title, String body) {
        // Implementation to send notification using Firebase or other service
    }

    /*public List<TopVendorWeekDto> getTopVendorsThisWeek() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
            LocalDateTime endOfWeek = startOfWeek.plusDays(7).minusSeconds(1);
            return followRepo.findTopVendorsThisWeek(startOfWeek, endOfWeek);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while fetching top vendors this week: " + e.getMessage(), e);
        }
    }*/


/*    public List<TopHostWeekDto> getTopHostsThisWeek() {
        try {
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay(now.getZone());
            ZonedDateTime endOfWeek = startOfWeek.plusDays(7).minusSeconds(1);
            return vendorRepo.findTopHostsThisWeek(startOfWeek, endOfWeek);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while fetching top hosts this week: " + e.getMessage(), e);
        }
    }*/
public List<TopHostWeekDto> getTopHostsThisWeek() {
    try {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime endOfWeek = startOfWeek.plusDays(7).minusSeconds(1);
        Pageable topThree = PageRequest.of(0, 3); // LIMIT 3
        return vendorRepo.findTopHostsThisWeek(startOfWeek, endOfWeek, topThree);
    } catch (Exception e) {
        throw new RuntimeException("Error occurred while fetching top hosts this week: " + e.getMessage(), e);
    }
}


    //following vendors
    public Map<String, Object> getFollowingVendorsOld(Long userId, int page, int size, String firstName) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        CustomCustomer user = customCustomerRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found " + userId, HttpStatus.BAD_REQUEST));

        Page<UserVendorFollow> followPage = followRepo.findByUserId(user.getId(), Pageable.unpaged());

        Stream<UserVendorFollow> followStream = followPage.getContent().stream();

        if (firstName != null && !firstName.trim().isEmpty()) {
            String[] nameParts = firstName.trim().split(" ");
            String firstNamePart = nameParts[0].toLowerCase();
            String lastNamePart = nameParts.length > 1 ? nameParts[1].toLowerCase() : "";

            followStream = followStream.filter(follow -> {
                VendorEntity vendor = follow.getVendor();
                String vendorFirstName = Optional.ofNullable(vendor.getFirst_name()).orElse("").toLowerCase();
                String vendorLastName = Optional.ofNullable(vendor.getLast_name()).orElse("").toLowerCase();

                if (nameParts.length == 1) {
                    return vendorFirstName.contains(firstNamePart) || vendorLastName.contains(firstNamePart);
                } else {
                    return vendorFirstName.contains(firstNamePart) && vendorLastName.contains(lastNamePart);
                }
            });
        }

        List<Map<String, Object>> filteredVendors = followStream.map(follow -> {
            VendorEntity vendor = follow.getVendor();
            Long vendorId = vendor.getService_provider_id();

            Map<String, Object> vendorInfo = new HashMap<>();
            vendorInfo.put("name", vendor.getName());
            vendorInfo.put("id", vendorId);
            vendorInfo.put("profilePic", vendor.getProfilePic());
            vendorInfo.put("email", vendor.getPrimary_email());
            vendorInfo.put("followerCount", followRepo.countByVendorId(vendorId));
            vendorInfo.put("isFollowing", true);

            List<Map<String, Object>> combinedActivities = new ArrayList<>();

            // Games
            Page<GetGameResponseDTO> games = gameService.getAllGames("ACTIVE", vendorId, Pageable.unpaged(), null);
            for (GetGameResponseDTO game : games.getContent()) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "GAME");
                item.put("createdDate", game.getCreatedAt());
                item.put("data", game);
                combinedActivities.add(item);
            }

            // Leagues
            Page<League> leaguesPage = leagueService.getAllActiveLeaguesByVendor(Pageable.unpaged(), vendorId);
            for (League league : leaguesPage.getContent()) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "LEAGUE");
                item.put("createdDate", league.getCreatedDate());
                item.put("data", league);
                combinedActivities.add(item);
            }

            // Tournaments
            Page<Tournament> tournamentsPage = tournamentService.getAllActiveTournamentsByVendor(Pageable.unpaged(), vendorId);
            for (Tournament tournament : tournamentsPage.getContent()) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "TOURNAMENT");
                item.put("createdDate", tournament.getCreatedDate());
                item.put("data", tournament);
                combinedActivities.add(item);
            }

            // Sort by createdDate (descending)
            combinedActivities.sort((a, b) -> {
                LocalDateTime dateA = (LocalDateTime) a.get("createdDate");
                LocalDateTime dateB = (LocalDateTime) b.get("createdDate");
                return dateB.compareTo(dateA);
            });

            vendorInfo.put("recentActivities", combinedActivities);

            return vendorInfo;
        }).collect(Collectors.toList());

        // Pagination manually
        int start = Math.min(page * size, filteredVendors.size());
        int end = Math.min(start + size, filteredVendors.size());
        List<Map<String, Object>> paginatedList = filteredVendors.subList(start, end);

        Map<String, Object> vendorPageMap = new HashMap<>();
        vendorPageMap.put("content", paginatedList);
        vendorPageMap.put("pageNumber", page);
        vendorPageMap.put("pageSize", size);
        vendorPageMap.put("totalPages", (int) Math.ceil((double) filteredVendors.size() / size));
        vendorPageMap.put("totalElements", filteredVendors.size());
        vendorPageMap.put("last", end == filteredVendors.size());

        Map<String, Object> response = new HashMap<>();
        response.put("vendors", vendorPageMap);

        return response;
    }


    public Map<String, Object> getFollowingVendors(Long userId, int page, int size, String firstName) {
//        Pageable pageable = PageRequest.of(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        CustomCustomer user = customCustomerRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found " + userId, HttpStatus.BAD_REQUEST));


        Page<UserVendorFollow> followPage = followRepo.findByUserId(user.getId(), Pageable.unpaged());

        Stream<UserVendorFollow> followStream = followPage.getContent().stream();

        if (firstName != null && !firstName.trim().isEmpty()) {
            String[] nameParts = firstName.trim().split(" ");
            String firstNamePart = nameParts[0].toLowerCase();
            String lastNamePart = nameParts.length > 1 ? nameParts[1].toLowerCase() : "";

            followStream = followStream.filter(follow -> {
                VendorEntity vendor = follow.getVendor();
                String vendorFirstName = vendor.getFirst_name() != null ? vendor.getFirst_name().toLowerCase() : "";
                String vendorLastName = vendor.getLast_name() != null ? vendor.getLast_name().toLowerCase() : "";

                if (nameParts.length == 1) {
                    return vendorFirstName.contains(firstNamePart) || vendorLastName.contains(firstNamePart);
                } else {
                    return vendorFirstName.contains(firstNamePart) && vendorLastName.contains(lastNamePart);
                }
            });
        }

        List<Map<String, Object>> filteredVendors = followStream.map(follow -> {
            VendorEntity vendor = follow.getVendor();

            Map<String, Object> vendorInfo = new HashMap<>();
            Long vendorId = vendor.getService_provider_id();

            vendorInfo.put("name", vendor.getUser_name()!=null ? vendor.getUser_name() : "Aagveer");
            vendorInfo.put("id", vendorId);
            vendorInfo.put("profilePic", vendor.getProfilePic());
            vendorInfo.put("email", vendor.getPrimary_email());
            vendorInfo.put("followerCount", followRepo.countByVendorId(vendorId));
            vendorInfo.put("isFollowing", true);

            Page<GetGameResponseDTO> games = gameService.getAllGames("ACTIVE", vendorId, pageable, null);
            vendorInfo.put("games", games.getContent() != null ? games.getContent() : Collections.emptyList());

            Page<League> leaguesPage = leagueService.getAllActiveLeaguesByVendor(pageable, vendorId);
            vendorInfo.put("leagues", leaguesPage.getContent() != null ? leaguesPage.getContent() : Collections.emptyList());


//            Page<Tournament> gamesPage = tournamentService.getAllActiveScheduledTournamentsByVendor(pageable,vendorId);
            Page<Tournament> gamesPage = tournamentService.getAllActiveScheduledTournamentsByVendorId(pageable, vendorId);

            List<TournamentGetallDTO> gameList = gamesPage.getContent().stream()
                    .map(this::mapToDTO)  // use your mapping logic
                    .collect(Collectors.toList());
            vendorInfo.put("tournaments", gameList);
//            vendorInfo.put("tournaments", gamesPage.getContent() != null ? gamesPage.getContent() : Collections.emptyList());

            return vendorInfo;
        }).collect(Collectors.toList());

        int start = Math.min(page * size, filteredVendors.size());
        int end = Math.min(start + size, filteredVendors.size());
        List<Map<String, Object>> paginatedList = filteredVendors.subList(start, end);

        Map<String, Object> vendorPageMap = new HashMap<>();
        vendorPageMap.put("content", paginatedList);
        vendorPageMap.put("pageNumber", page);
        vendorPageMap.put("pageSize", size);
        vendorPageMap.put("totalPages", (int) Math.ceil((double) filteredVendors.size() / size));
        vendorPageMap.put("totalElements", filteredVendors.size());
        vendorPageMap.put("last", end == filteredVendors.size());

        Map<String, Object> response = new HashMap<>();
        response.put("vendors", vendorPageMap);

        return response;

    }
/*    public Map<String, Object> getFeedOfVendors(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Pageable subPageable = PageRequest.of(0, 10);

        Page<VendorEntity> vendorPage = vendorRepo.findAll(pageable);

        List<Map<String, Object>> vendorContent = vendorPage.getContent().stream().map(vendor -> {
            Map<String, Object> vendorInfo = new HashMap<>();
            Long vendorId = vendor.getService_provider_id();

            vendorInfo.put("id", vendorId);
            vendorInfo.put("name", vendor.getUser_name()!=null ? vendor.getUser_name() : "Aagveer");
            vendorInfo.put("email", vendor.getPrimary_email());
            vendorInfo.put("profilePic", vendor.getProfilePic());
            vendorInfo.put("followerCount", followRepo.countByVendorId(vendorId));
            vendorInfo.put("isFollowing", currentUserId != null && followRepo.existsByUserIdAndVendorId(currentUserId, vendorId));

            // Games
            Page<GetGameResponseDTO> games = gameService.getAllGames("ACTIVE", vendorId, subPageable, null);
            vendorInfo.put("games", games.hasContent() ? games.getContent() : Collections.emptyList());

            // Leagues
            Page<League> leagues = leagueService.getAllActiveLeaguesByVendor(subPageable, vendorId);
            vendorInfo.put("leagues", leagues.hasContent() ? leagues.getContent() : Collections.emptyList());

            // Tournaments
            List<TournamentStatus> statuses = Arrays.asList(TournamentStatus.ACTIVE, TournamentStatus.SCHEDULED);
            Page<Tournament> tournaments = tournamentService.getAllTournaments(pageable, statuses, null, null);

            List<TournamentGetallDTO> gameList = tournaments.getContent().stream()
                    .map(this::mapToDTO)  // use your mapping logic
                    .collect(Collectors.toList());
            vendorInfo.put("tournaments", gameList);

            return vendorInfo;
        }).collect(Collectors.toList());

        Map<String, Object> vendors = new HashMap<>();
        vendors.put("pageNumber", vendorPage.getNumber());
        vendors.put("pageSize", vendorPage.getSize());
        vendors.put("totalElements", vendorPage.getTotalElements());
        vendors.put("totalPages", vendorPage.getTotalPages());
        vendors.put("last", vendorPage.isLast());
        vendors.put("content", vendorContent);

        Map<String, Object> response = new HashMap<>();
        response.put("vendors", vendors);

        return response;
    }*/


    public TournamentGetallDTO mapToDTO(Tournament tournament) {
        TournamentGetallDTO dto = new TournamentGetallDTO();

        dto.setId(tournament.getId());
        dto.setVendorId(tournament.getVendorId());

        // Set vendorProfilePic from vendorEntity (can be null-safe)
        if (tournament.getVendorEntity() != null) {
            dto.setVendorProfilePic(tournament.getVendorEntity().getProfilePic());
            dto.setVendorName(tournament.getVendorEntity().getName());

        } else {
            dto.setVendorProfilePic(""); // or default image URL
            dto.setVendorName("");

        }

        dto.setName(tournament.getName());
        dto.setTotalPrizePool(tournament.getTotalPrizePool());
        dto.setRound(tournament.getRound());
        dto.setTotalrounds(tournament.getTotalrounds());
        dto.setRoomprize(tournament.getRoomprize());
        dto.setTheme(tournament.getTheme());
        dto.setExistinggameId(tournament.getExistinggameId());
        dto.setGameUrl(tournament.getGameUrl());
        dto.setParticipants(tournament.getParticipants());
        dto.setCurrentJoinedPlayers(tournament.getCurrentJoinedPlayers());
        dto.setEntryFee(tournament.getEntryFee());
        dto.setMove(tournament.getMove());
        dto.setStatus(tournament.getStatus());
        dto.setShareableLink(tournament.getShareableLink());
        dto.setCreatedDate(tournament.getCreatedDate());
        dto.setScheduledAt(tournament.getScheduledAt());
        dto.setUpdatedDate(tournament.getUpdatedDate());
        dto.setEndDate(tournament.getEndDate());
        dto.setStatusUpdatedAt(tournament.getStatusUpdatedAt());

        return dto;

    }


    //following vendors
    public Map<String, Object> getFeedOfVendors(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Pageable pageable1 = PageRequest.of(0, 10);



        Page<VendorEntity> followPage = vendorRepo.findByIsPaid(true, pageable);

        List<Map<String, Object>> filteredVendors = followPage.getContent().stream()
                .map(vendor -> {
                    Long vendorId = vendor.getService_provider_id();

                    Page<GetGameResponseDTO> games = gameService.getVisibleGames( vendorId, pageable1);
                    Page<League> leaguesPage = leagueService.getAllActiveLeaguesByVendorFeed(pageable1, vendorId);
                    List<TournamentStatus> statuses = Arrays.asList(TournamentStatus.ACTIVE, TournamentStatus.SCHEDULED);
                    Page<Tournament> gamesPage = tournamentService.getAllTournaments(pageable1, statuses, vendorId, null);

                    List<TournamentGetallDTO> gameList = gamesPage.getContent().stream()
                            .map(this::mapToDTO)  // use your mapping logic
                            .collect(Collectors.toList());

                    // Skip only if all three are empty
                    if (games.isEmpty() && leaguesPage.isEmpty() && gamesPage.isEmpty()) {
                        return null; // skip vendor
                    }

                    Map<String, Object> vendorInfo = new HashMap<>();
                    vendorInfo.put("name", vendor.getUser_name() != null ? vendor.getUser_name() : "Aagveer");
                    vendorInfo.put("id", vendorId);
                    vendorInfo.put("profilePic", vendor.getProfilePic());
                    vendorInfo.put("email", vendor.getPrimary_email());
                    vendorInfo.put("followerCount", followRepo.countByVendorId(vendorId));
                    vendorInfo.put("isFollowing", currentUserId != null && followRepo.existsByUserIdAndVendorId(currentUserId, vendorId));
                    vendorInfo.put("games", games.getContent());
                    vendorInfo.put("leagues", leaguesPage.getContent());
//                    vendorInfo.put("tournaments", gamesPage.getContent());
                    vendorInfo.put("tournaments", gameList);


                    return vendorInfo;
                })
                .filter(Objects::nonNull) // remove skipped/null vendors
                .collect(Collectors.toList());

        Map<String, Object> vendorPageMap = new HashMap<>();
        vendorPageMap.put("content", filteredVendors);
        vendorPageMap.put("pageNumber", page);
        vendorPageMap.put("pageSize", size);
        vendorPageMap.put("totalPages", (int) Math.ceil((double) filteredVendors.size() / size));
        vendorPageMap.put("totalElements", filteredVendors.size());
        vendorPageMap.put("last", followPage.isLast());

        Map<String, Object> response = new HashMap<>();
        response.put("vendors", vendorPageMap);
        return response;
    }

    public Map<String, Object> getFeedOfVendorId(Long vendorId, int page, int size) {
        Optional<VendorEntity> optionalVendor = vendorRepo.findById(vendorId);
        if (!optionalVendor.isPresent()) {
            throw new RuntimeException("Vendor not found with ID: " + vendorId);
        }

        VendorEntity vendor = optionalVendor.get();
        Map<String, Object> vendorInfo = new HashMap<>();

        Long spId = vendor.getService_provider_id();

        vendorInfo.put("name", vendor.getName());
        vendorInfo.put("id", spId);
        vendorInfo.put("profilePic", vendor.getProfilePic());
        vendorInfo.put("email", vendor.getPrimary_email());
        vendorInfo.put("followerCount", followRepo.countByVendorId(spId));
        int totalGamePublished = vendor.getTotal_game_published() != null ? vendor.getTotal_game_published() : 0;
        int totalLeaguePublished = vendor.getTotal_league_published() != null ? vendor.getTotal_league_published() : 0;
        int totalTournamentPublished = vendor.getTotal_tournament_published() != null ? vendor.getTotal_tournament_published() : 0;
        int totalHostedGames = totalGamePublished + totalLeaguePublished + totalTournamentPublished;

        vendorInfo.put("hostedgames", totalHostedGames);
        vendorInfo.put("isFollowing", true);

        Pageable pageable = PageRequest.of(page, size);

        // Games - paginated
        Page<GetGameResponseDTO> games = gameService.getAllGames("ACTIVE", spId, pageable, null);
        vendorInfo.put("games", games.getContent() != null ? games.getContent() : Collections.emptyList());
        vendorInfo.put("gamesPage", Map.of(
                "pageNumber", games.getNumber(),
                "pageSize", games.getSize(),
                "totalPages", games.getTotalPages(),
                "totalElements", games.getTotalElements(),
                "last", games.isLast()
        ));

        Page<League> leaguesPage = leagueService.getAllActiveLeaguesByVendor(pageable, spId);
        vendorInfo.put("leagues", leaguesPage.getContent() != null ? leaguesPage.getContent() : Collections.emptyList());
        vendorInfo.put("leaguesPage", Map.of(
                "pageNumber", leaguesPage.getNumber(),
                "pageSize", leaguesPage.getSize(),
                "totalPages", leaguesPage.getTotalPages(),
                "totalElements", leaguesPage.getTotalElements(),
                "last", leaguesPage.isLast()
        ));

        // Tournaments - paginated
        Page<Tournament> tournamentsPage = tournamentService.getAllActiveScheduledTournamentsByVendor(pageable, spId);
        List<TournamentGetallDTO> gameList = tournamentsPage.getContent().stream()
                .map(this::mapToDTO)  // use your mapping logic
                .collect(Collectors.toList());
        vendorInfo.put("tournaments", gameList);
//        vendorInfo.put("tournaments", tournamentsPage.getContent() != null ? tournamentsPage.getContent() : Collections.emptyList());
        vendorInfo.put("tournamentsPage", Map.of(
                "pageNumber", tournamentsPage.getNumber(),
                "pageSize", tournamentsPage.getSize(),
                "totalPages", tournamentsPage.getTotalPages(),
                "totalElements", tournamentsPage.getTotalElements(),
                "last", tournamentsPage.isLast()
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("vendor", vendorInfo);

        return response;
    }


  /*  public Map<String, Object> getFeedOfVendors(int page, int size) {
        Pageable fetchPageable = PageRequest.of(0, 200); // Fetch enough vendors
        Pageable contentPageable = PageRequest.of(0, 10);

        Page<VendorEntity> allVendorsPage = vendorRepo.findAll(fetchPageable);
        List<VendorEntity> allVendors = allVendorsPage.getContent();

        List<Map<String, Object>> vendorList = new ArrayList<>();

        for (VendorEntity vendor : allVendors) {
            Long vendorId = vendor.getService_provider_id();

            Page<GetGameResponseDTO> games = gameService.getAllGames("ACTIVE", vendorId, contentPageable, null);
            List<GetGameResponseDTO> gameList = games.getContent() != null ? games.getContent() : Collections.emptyList();

            Page<League> leaguesPage = leagueService.getAllActiveLeaguesByVendor(contentPageable, vendorId);
            List<League> leagueList = leaguesPage.getContent() != null ? leaguesPage.getContent() : Collections.emptyList();

            Page<Tournament> tournamentsPage = tournamentService.getAllActiveTournamentsByVendor(contentPageable, vendorId);
            List<Tournament> tournamentList = tournamentsPage.getContent() != null ? tournamentsPage.getContent() : Collections.emptyList();

            boolean hasActiveContent = !gameList.isEmpty() || !leagueList.isEmpty() || !tournamentList.isEmpty();

            Map<String, Object> vendorInfo = new HashMap<>();
            vendorInfo.put("name", vendor.getName());
            vendorInfo.put("id", vendorId);
            vendorInfo.put("profilePic", vendor.getProfilePic());
            vendorInfo.put("email", vendor.getPrimary_email());
            vendorInfo.put("followerCount", followRepo.countByVendorId(vendorId));
            vendorInfo.put("isFollowing", true);
            vendorInfo.put("games", gameList);
            vendorInfo.put("leagues", leagueList);
            vendorInfo.put("tournaments", tournamentList);
            vendorInfo.put("hasActiveContent", hasActiveContent); // helper for sorting

            vendorList.add(vendorInfo);
        }

        // Sort active content vendors first
        vendorList.sort((v1, v2) -> Boolean.compare(!(Boolean) v1.get("hasActiveContent"), !(Boolean) v2.get("hasActiveContent")));

        // Remove helper key
        vendorList.forEach(v -> v.remove("hasActiveContent"));

        // Manual Pagination
        int totalElements = vendorList.size();
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<Map<String, Object>> paginatedList = vendorList.subList(fromIndex, toIndex);

        Map<String, Object> vendorPageMap = new HashMap<>();
        vendorPageMap.put("content", paginatedList);
        vendorPageMap.put("pageNumber", page);
        vendorPageMap.put("pageSize", size);
        vendorPageMap.put("totalPages", (int) Math.ceil((double) totalElements / size));
        vendorPageMap.put("totalElements", totalElements);
        vendorPageMap.put("last", toIndex >= totalElements);

        Map<String, Object> response = new HashMap<>();
        response.put("vendors", vendorPageMap);

        return response;
    }*/



}
