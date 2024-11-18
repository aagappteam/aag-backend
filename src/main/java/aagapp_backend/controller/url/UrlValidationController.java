package aagapp_backend.controller.url;

import aagapp_backend.services.url.UrlVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/url")
public class UrlValidationController {

    @Autowired
    private UrlVerificationService urlVerificationService;

    @GetMapping("/validate")
    public boolean validateUrl(@RequestParam String url, @RequestParam String platform) {
        return urlVerificationService.isUrlValid(url, platform);
    }
}

