package aagapp_backend.services.exception;

import com.twilio.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class ExceptionHandlingService implements ExceptionHandlingImplement {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingService.class);

    @Override
    public void handleHttpError(ResponseEntity<String> response) {
        HttpStatus statusCode = (HttpStatus) response.getStatusCode();
        String responseBody = response.getBody();
        throw new RuntimeException("HTTP Error: " + statusCode + ", Response Body: " + responseBody);
    }

    @Override
    public void handleHttpClientErrorException(HttpClientErrorException e) {
        HttpStatus statusCode = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        throw new RuntimeException("HTTP Client Error: " + statusCode + ", Response Body: " + responseBody, e);
    }

    @Override
    public void handleApiException(ApiException e) {
        int errorCode = e.getCode();
        String errorMessage = e.getMessage();

        if (errorCode == 21408) {

            throw new RuntimeException("Permission to send SMS not enabled for the region", e);
        } else {

            throw new RuntimeException("Api  Error: " + errorCode + ", Response Body: " + errorMessage, e);
        }

    }

    @Override
    public void handleException(Exception e) {

        if (e instanceof ApiException) {
            logger.error("API exception occurred: ", e.getMessage());
        } else if (e instanceof HttpClientErrorException) {
            logger.error("HTTP client error occurred: ", e.getMessage());
        } else {
            logger.error("Something went wrong: ", e.getMessage());
        }
    }


    public void handleException(HttpStatus status, Exception e) {

        if (status.equals(HttpStatus.BAD_REQUEST)) {
            logger.error(status + " " + e.getMessage());
        } else if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            logger.error(status + " " + e.getMessage());
        } else {
            logger.error("Something went wrong: " + e.getMessage());
        }

    }
}