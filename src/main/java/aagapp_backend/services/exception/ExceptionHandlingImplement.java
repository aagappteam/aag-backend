package aagapp_backend.services.exception;

import com.twilio.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;


public interface ExceptionHandlingImplement {
    void handleHttpError(ResponseEntity<String> response);

    String handleHttpClientErrorException(HttpClientErrorException e);

    String handleApiException(ApiException e);

    String handleException(Exception e);

    String handleException(HttpStatus status, Exception e);


}