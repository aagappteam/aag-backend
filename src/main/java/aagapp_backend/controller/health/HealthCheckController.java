package aagapp_backend.controller.health;

import aagapp_backend.entity.SuccessResponse;
import aagapp_backend.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health")
@RestController
public class HealthCheckController {
    @Autowired
    private ResponseService responseService;
    @GetMapping("/ping")
    public ResponseEntity<SuccessResponse> ping() {
        return responseService.generateSuccessResponse("ping ", null, HttpStatus.OK);
    }
}
