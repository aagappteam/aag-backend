package aagapp_backend.controller.dashboard;

import aagapp_backend.dto.leaderboard.DashboardResponse;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.dashboard.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ResponseService responseService;

    @GetMapping("/user")
    public ResponseEntity<?> getDashboard(
            @RequestParam(defaultValue = "0") int latestPage,
            @RequestParam(defaultValue = "0") int popularPage,
            @RequestParam(defaultValue = "0") int feePage,
            @RequestParam(defaultValue = "0") int influencerPage,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            DashboardResponse response = dashboardService.getDashboard(latestPage, popularPage, feePage, influencerPage, size);
            return responseService.generateSuccessResponse("Dashboard retrieved successfully.", response, HttpStatus.OK);
        }catch (RuntimeException e){
            return responseService.generateErrorResponse("Error retrieving dashboard: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {


            e.printStackTrace();
            return responseService.generateErrorResponse("Error retrieving dashboard: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
