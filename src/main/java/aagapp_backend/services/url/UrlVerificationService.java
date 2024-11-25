package aagapp_backend.services.url;

import aagapp_backend.components.url.SocialMediaValidator;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UrlVerificationService {
    private RestTemplate restTemplate;
    private SocialMediaValidator socialValidator;
    private final ExceptionHandlingImplement exceptionHandlingImplement;

    @Autowired
    public UrlVerificationService(
            RestTemplate restTemplate,
            SocialMediaValidator socialValidator,
            ExceptionHandlingImplement exceptionHandlingImplement) {
        this.restTemplate = restTemplate;
        this.socialValidator = socialValidator;
        this.exceptionHandlingImplement = exceptionHandlingImplement;
    }


    public boolean isUrlValid(String url, String platform) {
        boolean isFormatValid = false;

        // Validating the URL format for the specific platform
        switch (platform.toLowerCase()) {
            case "facebook":
                isFormatValid = socialValidator.isFacebookUrl(url);
                break;
            case "twitter":
                isFormatValid = socialValidator.isTwitterUrl(url);
                break;
            case "snapchat":
                isFormatValid = socialValidator.isSnapchatUrl(url);
                break;
            case "instagram":
                isFormatValid = socialValidator.isInstagramUrl(url);
                break;
            case "youtube":
                isFormatValid = socialValidator.isYouTubeUrl(url);
                break;
            default:
                throw new IllegalArgumentException("Unsupported platform");
        }



        try {
/*            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.HEAD, entity, Void.class);

            return response.getStatusCode().is2xxSuccessful();*/
            if (!isFormatValid) {
                return false;
            }else{
                return true;

            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                System.out.println("Forbidden access to URL: " + url);
            }
            exceptionHandlingImplement.handleException(e);
            return false;
        }
    }
}
