package aagapp_backend.services.social;

import aagapp_backend.dto.TopVendorDto;
import aagapp_backend.dto.TopVendorWeekDto;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.social.UserVendorFollow;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.social.UserVendorFollowRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.vendor.VenderService;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
        userRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));

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

        VendorEntity vendor = vendorRepo.findById(vendorId)
                .orElseThrow(() -> new NoSuchElementException("Vendor not found with ID: " + vendorId));

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


/*    public void notifyFollowers(Long vendorId, String title, String body) {
        List<UserVendorFollow> followers = followRepo.findByVendorId(vendorId);
        for (UserVendorFollow follow : followers) {
            String token = follow.getUser().getFcmToken();
            if (token != null) {
                sendPushNotification(token, title, body);
            }
        }
    }*/

    private void sendPushNotification(String token, String title, String body) {
        // Implementation to send notification using Firebase or other service
    }

    public List<TopVendorWeekDto> getTopVendorsThisWeek() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
            LocalDateTime endOfWeek = startOfWeek.plusDays(7).minusSeconds(1);
            return followRepo.findTopVendorsThisWeek(startOfWeek, endOfWeek);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while fetching top vendors this week: " + e.getMessage(), e);
        }
    }


}
