package aagapp_backend.controller.payment;

import aagapp_backend.dto.PaymentRequest;
import aagapp_backend.services.subpaisa.SabService;
import aagapp_backend.utils.Encryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/subpaisa")
public class SubpaisaController {

    @Autowired
    private SabService sabService;

    @GetMapping("/initate-payment")
    public ModelAndView getSabPaisaRequestPage() {
        return sabService.getSabPaisaPgService();
    }
    @PostMapping("/response")
    public ModelAndView handleSabPaisaResponse(@RequestBody Map<String, Object> payload) {
        // Extract the 'data' JSON string
        String dataStr = (String) payload.get("data");

        // Now extract the encData param value from 'dataStr' string
        // The 'data' string looks like: "encData=xxxx&clientCode=ES2S"
        String encData = null;
        if (dataStr != null) {
            // Split by '&', find encData
            String[] parts = dataStr.split("&");
            for (String part : parts) {
                if (part.startsWith("encData=")) {
                    encData = part.substring("encData=".length());
                    break;
                }
            }
        }

        if (encData == null) {
            throw new IllegalArgumentException("encData missing in response");
        }

        // Decrypt the encData
        String decryptedResponse = sabService.getPgResponseService(encData);

        ModelAndView mav = new ModelAndView("paymentResult");
        mav.addObject("paymentDetails", decryptedResponse);
        return mav;
    }


/*
    @PostMapping("/response")
    public ModelAndView handleSabPaisaResponse(@RequestParam("encData") String encData) {
        String decryptedResponse = sabService.getPgResponseService(encData);
        ModelAndView mav = new ModelAndView("paymentResult"); // the Thymeleaf HTML page name
        mav.addObject("paymentDetails", decryptedResponse);
        return mav;
    }
*/



    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestParam Map<String, String> allParams) {
        // Process the callback parameters and update payment status
        return ResponseEntity.ok("Payment processed");
    }
}
