package aagapp_backend.controller.payment;

import aagapp_backend.dto.PaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/ssubpaisa")
public class SubpaisaController {

/*    @PostMapping("/initiate")
    public ResponseEntity<Map<String, String>> initiatePayment(@RequestBody PaymentRequest request) {



    }*/

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam Map<String, String> allParams) {
        // Process the callback parameters and update payment status
        return ResponseEntity.ok("Payment processed");
    }
}
