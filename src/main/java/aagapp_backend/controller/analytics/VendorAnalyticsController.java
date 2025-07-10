package aagapp_backend.controller.analytics;

import aagapp_backend.dto.VendorAnalyticsResponse;
import aagapp_backend.services.analytics.VendorAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/analytics")
public class VendorAnalyticsController {

    @Autowired
    private VendorAnalyticsService analyticsService;

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<VendorAnalyticsResponse> getVendorAnalytics(
            @PathVariable Long vendorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : null;

        VendorAnalyticsResponse response = analyticsService.getVendorAnalytics(vendorId, start, end);
        return ResponseEntity.ok(response);
    }

}