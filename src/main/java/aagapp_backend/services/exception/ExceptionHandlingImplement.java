package aagapp_backend.services.exception;

import com.twilio.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;


public interface ExceptionHandlingImplement {
    void handleHttpError(ResponseEntity<String> response);

    void handleHttpClientErrorException(HttpClientErrorException e);

    void handleApiException(ApiException e);

    void handleException(Exception e);

    void handleException(HttpStatus status, Exception e);
}