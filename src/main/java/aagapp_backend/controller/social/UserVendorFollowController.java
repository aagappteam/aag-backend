package aagapp_backend.controller.social;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.social.UserVendorFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/follow")
public class UserVendorFollowController {

    @Autowired
    private UserVendorFollowService followService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;

    @PostMapping("/vendor")
    public ResponseEntity<?> followVendor(@RequestParam Long userId, @RequestParam Long vendorId) {

        try{
            String result = followService.followVendor(userId, vendorId);
            return responseService.generateSuccessResponse("Followed",result, HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(
                HttpStatus.BAD_REQUEST,
                    e);
            return responseService.generateErrorResponse("failed to follow vendor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/unfollow-vendor")
    public ResponseEntity<?> unfollowVendor(@RequestParam Long userId, @RequestParam Long vendorId) {
            try{
                String result = followService.unfollowVendor(userId, vendorId);
                return responseService.generateSuccessResponse("Unfollowed",result, HttpStatus.OK);

            }catch (Exception e) {
                exceptionHandling.handleException(
                    HttpStatus.BAD_REQUEST,
                    e);
                return responseService.generateErrorResponse("failed to unfollow vendor", HttpStatus.INTERNAL_SERVER_ERROR);
            }
    }

/*    @GetMapping("/vendors-by-user/{userId}")
    public ResponseEntity<?> getFollowedVendors(@PathVariable Long userId) {

//        return ResponseEntity.ok(followService.getVendorsFollowedByUser(userId));
        try{
            return responseService.generateSuccessResponse("Vendors followed by user", followService.getVendorsFollowedByUser(userId), HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(
                    HttpStatus.BAD_REQUEST,
                    e);
            return responseService.generateErrorResponse("failed to get followed vendors", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @GetMapping("/vendors-by-user/{userId}")
    public ResponseEntity<?> getFollowedVendors(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> response = followService.getVendorsWithDetails(userId, page, size);
            return responseService.generateSuccessResponse("Vendors followed by user", response, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse("failed to get followed vendors", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

/*    @GetMapping("/users-by-vendor/{vendorId}")
    public ResponseEntity<?> getFollowers(@PathVariable Long vendorId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> response = followService.getFollowersOfVendor(vendorId, page, size);
            return responseService.generateSuccessResponse("Users following this vendor", response, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse("Failed to get vendor followers", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @GetMapping("/top-vendors")
    public ResponseEntity<?> getTopVendors() {
        return ResponseEntity.ok(followService.getTopVendors());
    }
    @GetMapping("/status")
    public ResponseEntity<?> isUserFollowing(@RequestParam Long userId, @RequestParam Long vendorId) {
        boolean isFollowing = followService.isUserFollowing(userId, vendorId);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }
}