package aagapp_backend.controller.admin;

import aagapp_backend.components.JwtUtil;
import aagapp_backend.entity.admin.AdminLogs;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.RoleService;
import aagapp_backend.services.admin.AdminLogService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/activity")
public class ActivityController {
    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private AdminLogService adminLogsService;
    @GetMapping("/all")
    public ResponseEntity<?> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String search,
            @RequestHeader(value = "Authorization") String authorization
    ) {
       try {

           if (authorization == null || !authorization.startsWith("Bearer ")) {
               return responseService.generateErrorResponse("Invalid or missing Authorization header", HttpStatus.BAD_REQUEST);
           }

           String token = authorization.substring(7);
           Integer role = jwtUtil.extractRoleId(token);
           String roleName = roleService.findRoleName(role);

           Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
           Page<AdminLogs> adminLogs = adminLogsService.getAllLogs(pageable, roleName, performedBy, targetType, search);
           return responseService.generateSuccessResponseWithCount("Activities fetched successfully", adminLogs.getContent(), adminLogs.getTotalElements(), HttpStatus.OK);
       }catch (Exception e){
           exceptionHandling.handleException(e);
           return responseService.generateErrorResponse("Error processing request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

//    update read true
    @PostMapping("/read")
    public ResponseEntity<?> updateRead(@RequestParam(required = false) Long id) {
        try {
            adminLogsService.updateRead(id);
            return responseService.generateResponse(HttpStatus.OK, "Notification updated", null);
        }catch (EntityNotFoundException e){
            return responseService.generateErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error processing request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

/*    @GetMapping
    public Page<AdminLogs> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate,desc") String[] sort
    ) {
        Sort sorting = Sort.by(Arrays.stream(sort)
                .map(s -> {
                    String[] parts = s.split(",");
                    return new Sort.Order(Sort.Direction.fromString(parts[1]), parts[0]);
                }).toList());

        Pageable pageable = PageRequest.of(page, size, sorting);
        return adminLogsService.getMyNotifications(user.getId(), user.getRole(), pageable);
    }*/

}
