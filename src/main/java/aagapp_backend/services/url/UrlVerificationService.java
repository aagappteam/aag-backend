package aagapp_backend.services.url;

import aagapp_backend.components.url.SocialMediaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

@Service
public class UrlVerificationService {
    private RestTemplate restTemplate = new RestTemplate();
    private  SocialMediaValidator socialValidator;

    @Autowired
    public UrlVerificationService(RestTemplate restTemplate, SocialMediaValidator socialValidator) {
        this.socialValidator = socialValidator;
        this.restTemplate = restTemplate;
    }

    public boolean isUrlValid(String url, String platform) {
        boolean isFormatValid = false;

        // Validate URL format based on platform
        switch (platform.toLowerCase()) {
            case "facebook":
                isFormatValid = SocialMediaValidator.isFacebookUrl(url);
                break;
            case "twitter":
                isFormatValid = SocialMediaValidator.isTwitterUrl(url);
                break;
            case "snapchat":
                isFormatValid = SocialMediaValidator.isSnapchatUrl(url);
                break;
            default:
                throw new IllegalArgumentException("Unsupported platform");
        }

        if (!isFormatValid) {
            return false;
        }

        try {
//            HttpStatus statusCode = restTemplate.headForHeaders(url).getStatusCode();
            return statusCode.is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}

