package aagapp_backend.controller.test;

import aagapp_backend.entity.VendorEntity;
import aagapp_backend.services.EmailService;
import aagapp_backend.services.unity.UnityLudoService;
import jakarta.persistence.EntityManager;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController("/test")
public class TestController {


@Autowired
private EmailService emailService;

@Autowired
private EntityManager entityManager;

    @GetMapping("/sendOnboardingEmail")
    public void sendOnboardingEmail() throws IOException {
         emailService.sendOnboardingEmail("anilkantmishra357@gmail.com", "AAG onboarding", "Welcome to AAG!");

    }
    @GetMapping("/send-profile/{service_provider_id}")
    public void sendProfileVerificationEmail(@PathVariable Long service_provider_id) throws IOException {
        VendorEntity vendorEntity = entityManager.find(VendorEntity.class, service_provider_id);
        emailService.sendProfileVerificationEmail(vendorEntity ,"AAG onboarding");

    }

    @GetMapping("/sendProfileRejectionEmail/{service_provider_id}")
    public void sendProfileRejectionEmail(@PathVariable Long service_provider_id) throws IOException {
        VendorEntity vendorEntity = entityManager.find(VendorEntity.class, service_provider_id);

        emailService.sendProfileRejectionEmail(vendorEntity);

    }


}
