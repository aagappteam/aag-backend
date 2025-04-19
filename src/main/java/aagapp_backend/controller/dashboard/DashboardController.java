package aagapp_backend.controller.dashboard;

import aagapp_backend.dto.leaderboard.DashboardResponse;
import aagapp_backend.services.dashboard.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/user")
    public ResponseEntity<?> getDashboard(
            @RequestParam(defaultValue = "0") int latestPage,
            @RequestParam(defaultValue = "0") int popularPage,
            @RequestParam(defaultValue = "0") int lowFeePage,
            @RequestParam(defaultValue = "0") int mediumFeePage,
            @RequestParam(defaultValue = "0") int highFeePage,
            @RequestParam(defaultValue = "0") int influencerPage,
            @RequestParam(defaultValue = "10") int size
    ) {
try{
    DashboardResponse response = dashboardService.getDashboard(latestPage, popularPage, lowFeePage, mediumFeePage, highFeePage, influencerPage, size);
    return ResponseEntity.ok(response);
}catch (Exception e){
    e.printStackTrace();
    return ResponseEntity.badRequest().body("Failed to get dashboard");
}
    }
}