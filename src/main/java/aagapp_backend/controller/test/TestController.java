package aagapp_backend.controller.test;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.players.Player;
import aagapp_backend.services.ApiConstants;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.EmailService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.faqs.FAQService;
import aagapp_backend.services.firebase.NotoficationFirebase;
import aagapp_backend.services.tournamnetservice.TournamentService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private NotoficationFirebase notoficationFirebase;


    @Autowired
    private FAQService faqService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomCustomerService customCustomerService;

    // POST method to send onboarding email
    @PostMapping("/sendOnboardingEmail")
    public void sendOnboardingEmail(@RequestParam String email,
                                    @RequestParam String firstname,
                                    @RequestParam String lastname) throws IOException {
        emailService.sendOnboardingEmail(email, firstname, lastname);
    }

    // POST method to send profile verification email
    @PostMapping("/send-profile/{service_provider_id}")
    public void sendProfileVerificationEmail(@PathVariable Long service_provider_id) throws IOException {
        VendorEntity vendorEntity = entityManager.find(VendorEntity.class, service_provider_id);
        if (vendorEntity != null) {
            emailService.sendProfileVerificationEmail(vendorEntity, "AAG onboarding");
        }
    }

    // POST method to send profile rejection email
    @PostMapping("/sendProfileRejectionEmail/{service_provider_id}")
    public void sendProfileRejectionEmail(@PathVariable Long service_provider_id) throws IOException {
        VendorEntity vendorEntity = entityManager.find(VendorEntity.class, service_provider_id);
        if (vendorEntity != null) {
            emailService.sendProfileRejectionEmail(vendorEntity);
        }
    }


//    set faq data in the database
    @PostMapping("/faquser")
    public void setFaqData() {
        faqService.addFAQIfNeeded();
   }


//   send ntotification to user/vendor
    @PostMapping("/sendnotification/{service_provider_id}/{role}")
    public ResponseEntity<?> sendNotification(@PathVariable Long service_provider_id,@PathVariable String role) throws IOException {


        if ("vendor".equalsIgnoreCase(role)) {
            VendorEntity vendorEntity = entityManager.find(VendorEntity.class, service_provider_id);
            if (vendorEntity != null) {
                if (vendorEntity.getFcmToken() != null) {
                    notoficationFirebase.sendNotification(
                            vendorEntity.getFcmToken(),
                            "Tournament starting soon!",
                            "Tournament will start in 3 minutes. Please join now!"
                    );
                    return new ResponseEntity<>("Notification sent successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("No FCM token for player", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Vendor not found", HttpStatus.NOT_FOUND);
            }
        } else if ("user".equalsIgnoreCase(role)) {

            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, service_provider_id);
            if (customCustomer != null) {
                if (customCustomer.getFcmToken() != null) {
                    notoficationFirebase.sendNotification(
                            customCustomer.getFcmToken(),
                            "Tournament starting soon!",
                            "Tournament will start in 3 minutes. Please join now!"
                    );
                    return new ResponseEntity<>("Notification sent successfully", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("No FCM token for player", HttpStatus.BAD_REQUEST);
                }


            } else {
                return new ResponseEntity<>("Invalid role", HttpStatus.BAD_REQUEST);
            }


        }
        return new ResponseEntity<>("Invalid role", HttpStatus.BAD_REQUEST);

    }

    @GetMapping("/gender")
    public String getGender(@RequestParam String name) {
        return customCustomerService.getGenderByName(name);
    }


    @GetMapping("/active-players")
    public ResponseEntity<?> getActivePlayers(@RequestParam Long tournamentId) {
        try {
            List<Player> players = tournamentService.getActivePlayers(tournamentId);
            System.out.println("Players: " + players.size());
            return responseService.generateSuccessResponse("Players fetched successfully", players, HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
