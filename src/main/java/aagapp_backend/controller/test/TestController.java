package aagapp_backend.controller.test;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.EmailService;
import aagapp_backend.services.faqs.FAQService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private FAQService faqService;

    @Autowired
    private EntityManager entityManager;

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
}
