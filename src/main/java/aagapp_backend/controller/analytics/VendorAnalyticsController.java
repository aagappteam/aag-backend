package aagapp_backend.controller.analytics;

import aagapp_backend.dto.VendorAnalyticsResponse;
import aagapp_backend.services.analytics.VendorAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class VendorAnalyticsController {

    @Autowired
    private VendorAnalyticsService analyticsService;

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<VendorAnalyticsResponse> getVendorAnalytics(@PathVariable Long vendorId) {
        return ResponseEntity.ok(analyticsService.getVendorAnalytics(vendorId));
    }
}