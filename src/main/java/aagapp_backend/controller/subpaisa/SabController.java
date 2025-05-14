package aagapp_backend.controller.subpaisa;


import java.net.URISyntaxException;

import aagapp_backend.services.subpaisa.SabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;


import jakarta.annotation.PostConstruct;

@Controller
public class SabController {

    @Autowired
    private SabService sabService;

//    Initate payment
    @GetMapping("/initate-payment")
    public ModelAndView initate() {
        ModelAndView init = sabService.getSabPaisaPgService();

        return init;
    }

    @PostMapping("/response")
    public ResponseEntity<?> getresponse(@RequestParam("encResponse") String encResponse) {

        String pgResponseService = sabService.getPgResponseService(encResponse);
        System.out.println("pgResponseService________________" + pgResponseService);
        return new ResponseEntity<>(pgResponseService, HttpStatus.OK);
    }

    @GetMapping("/resp")
    public ModelAndView getRes() {
        ModelAndView sabPaisaPgService = sabService.getSabPaisaPgService();
        System.out.println(sabPaisaPgService);
        return sabPaisaPgService;
    }



    @GetMapping("/enq")
    public ResponseEntity<?> getTransEnq() {
        String statusEnq = sabService.getStatusEnq();
        return new ResponseEntity<String>(statusEnq, HttpStatus.OK);

    }

    @GetMapping("/MerchantAcknowledgement")
    public ResponseEntity<?> getAcknowledge(){
        String ack = sabService.getAck();
        return new ResponseEntity<>(ack, HttpStatus.OK);
    }

    @GetMapping("/Bank")
    public ResponseEntity<?> bankList(){
        String getBank = sabService.GetBank();
        return new ResponseEntity<String>(getBank, HttpStatus.OK);
    }
}


