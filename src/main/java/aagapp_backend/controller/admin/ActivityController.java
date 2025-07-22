package aagapp_backend.controller.admin;

import aagapp_backend.entity.admin.AdminLogs;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.admin.AdminLogService;
import aagapp_backend.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/activity")
public class ActivityController {
    @Autowired
    private ExceptionHandlingService exceptionHandling;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private AdminLogService adminLogsService;
    @GetMapping("/all")
    public ResponseEntity<?> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String search
    ) {
       try {
           Pageable pageable = PageRequest.of(page, size);
           Page<AdminLogs> adminLogs = adminLogsService.getAllLogs(pageable, roleName, performedBy, targetType, search);
           return responseService.generateSuccessResponseWithCount("Activities fetched successfully", adminLogs.getContent(), adminLogs.getTotalElements(), HttpStatus.OK);
       }catch (Exception e){
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
