package aagapp_backend.services.social;

import aagapp_backend.dto.TopVendorDto;
import aagapp_backend.entity.social.UserVendorFollow;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.social.UserVendorFollowRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserVendorFollowService {

    @Autowired
    private UserVendorFollowRepository followRepo;

    @Autowired
    private CustomCustomerRepository userRepo;

    @Autowired
    private VendorRepository vendorRepo;

    public String followVendor(Long userId, Long vendorId) {
        if (!followRepo.existsByUserIdAndVendorId(userId, vendorId)) {
            UserVendorFollow follow = new UserVendorFollow();
            follow.setUser(userRepo.findById(userId).orElseThrow());
            follow.setVendor(vendorRepo.findById(vendorId).orElseThrow());
            follow.setFollowedAt(LocalDateTime.now());
            followRepo.save(follow);
        }
        return "Followed";
    }

    public String unfollowVendor(Long userId, Long vendorId) {
        followRepo.findByUserIdAndVendorId(userId, vendorId)
                .ifPresent(followRepo::delete);
        return "Unfollowed";
    }

    public List<UserVendorFollow> getFollowersOfVendor(Long vendorId) {
        return followRepo.findByVendorId(vendorId);
    }

    public List<UserVendorFollow> getVendorsFollowedByUser(Long userId) {
        return followRepo.findByUserId(userId);
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


    public void notifyFollowers(Long vendorId, String title, String body) {
        List<UserVendorFollow> followers = followRepo.findByVendorId(vendorId);
        for (UserVendorFollow follow : followers) {
            String token = follow.getUser().getFcmToken();
            if (token != null) {
                sendPushNotification(token, title, body);
            }
        }
    }

    private void sendPushNotification(String token, String title, String body) {
        // Implementation to send notification using Firebase or other service
    }
}
