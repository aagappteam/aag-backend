package aagapp_backend.controller.social;
import aagapp_backend.dto.TopVendorDto;
import aagapp_backend.dto.TopVendorWeekDto;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.exception.BusinessException;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import aagapp_backend.services.social.UserVendorFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/follow")
public class UserVendorFollowController {

    @Autowired
    private UserVendorFollowService followService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;

    @PostMapping("/follow-vendor")
    public ResponseEntity<?> followVendor(@RequestParam Long userId, @RequestParam Long vendorId) {
        try {
            String result = followService.followVendor(userId, vendorId);
            return responseService.generateSuccessResponse("Followed", result, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Failed to follow vendor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/unfollow-vendor")
    public ResponseEntity<?> unfollowVendor(@RequestParam Long userId, @RequestParam Long vendorId) {
        try {
            String result = followService.unfollowVendor(userId, vendorId);
            return responseService.generateSuccessResponse("Unfollowed", result, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e);
            return responseService.generateErrorResponse("Failed to unfollow vendor", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/vendors-by-user/{userId}")
    public ResponseEntity<?> getFollowedVendors(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(value = "firstName", required = false) String firstName) {
        try {
            Map<String, Object> response = followService.getVendorsWithDetails(userId, page, size, firstName);
            return responseService.generateSuccessResponse("Vendors followed by user", response, HttpStatus.OK);
        }catch (BusinessException e){
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            exceptionHandling.handleException(HttpStatus.BAD_REQUEST, e);
            return responseService.generateErrorResponse("Failed to get followed vendors", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

/*    @GetMapping("/vendors-by-user/{userId}")
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
    }*/

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

    @GetMapping("/top-vendors-this-week")
    public ResponseEntity<?> getTopVendorsThisWeek() {
        try {
            List<TopVendorWeekDto> topVendors = followService.getTopVendorsThisWeek();
            return responseService.generateSuccessResponse("Top vendors this week", topVendors, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse(
                    "Error fetching top vendors: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @GetMapping("/following-vendors")
    public ResponseEntity<Map<String, Object>> getFollowedVendorsWithDetails(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String firstName) {

        Map<String, Object> data = followService.getFollowingVendors(userId, page, size, firstName);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Vendors followed by user");
        response.put("status", "OK");
        response.put("status_code", 200);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    //get all vendors
    @GetMapping("/all-feed")
    public ResponseEntity<Map<String, Object>> getFeedOfUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> data = followService.getFeedOfVendors( page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User feed Vendors List");
        response.put("status", "OK");
        response.put("status_code", 200);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-feed/{vendorId}")
    public ResponseEntity<Map<String, Object>> getFeedOfUser(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> data = followService.getFeedOfVendorId( vendorId,page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User feed Vendors List");
        response.put("status", "OK");
        response.put("status_code", 200);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

}