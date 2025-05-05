
package aagapp_backend.services.exception;

import aagapp_backend.services.EmailService;
import com.twilio.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class ExceptionHandlingService implements ExceptionHandlingImplement {

    @Autowired
    private EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingService.class);

    @Override
    public void handleHttpError(ResponseEntity<String> response) {
        HttpStatus statusCode = (HttpStatus) response.getStatusCode();
        String responseBody = response.getBody();
        logger.error("HTTP Error: " + statusCode + ", Response Body: " + responseBody);

        throw new RuntimeException("HTTP Error: " + statusCode + ", Response Body: " + responseBody);
    }

    @Override
    public String handleHttpClientErrorException(HttpClientErrorException e) {
        HttpStatus statusCode = (HttpStatus) e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        logger.error("HTTP Error: " + statusCode + ", Response Body: " +e);
        throw new RuntimeException("HTTP Client Error: " + statusCode + ", Response Body: " + responseBody, e);
    }

    @Override
    public String handleApiException(ApiException e) {
        int errorCode = e.getCode();
        String errorMessage = e.getMessage();

        if (errorCode == 21408) {
            logger.error("HTTP Client Error " + errorMessage, e);

            throw new RuntimeException("Permission to send SMS not enabled for the region", e);
        } else {
            logger.error("HTTP Client Error: " + errorMessage, e);
            throw new RuntimeException("Api  Error: " + errorCode + ", Response Body: " + errorMessage, e);
        }

    }

    @Override
    public String handleException(Exception e) {
        logger.error("Error occurred: " + e.getMessage());

        if (e instanceof ApiException) {
            return handleApiException((ApiException) e);
        } else if (e instanceof HttpClientErrorException) {
            logger.error("Unhandled exception" + e);

            return handleHttpClientErrorException((HttpClientErrorException) e);
        } else {
            logger.error("Unhandled exception" + e);
            return "Something went wrong: " + e.getMessage();
        }

    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    private boolean isBusinessOrCausedByBusinessException(Throwable e) {
        while (e != null) {
            if (e instanceof BusinessException) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }
    public String handleException(HttpStatus status, Exception e, HttpServletRequest request) {
        String userId = getUserIdSafely(request);

        String requestUrl = (request != null) ? request.getRequestURL().toString() : "N/A";
        String queryString = (request != null) ? request.getQueryString() : null;
        String fullUrl = (queryString != null) ? requestUrl + "?" + queryString : requestUrl;

        String errorDetails = "UserId: " + userId +
                "\nRequest URL: " + fullUrl +
                "\nStatus: " + status +
                "\nMessage: " + e.getMessage() +
                "\nStackTrace: " + getStackTraceAsString(e);

        if (!isBusinessOrCausedByBusinessException(e)) {
            emailService.sendErrorEmail("Error Occurred in Application", errorDetails);
        }

        if (status.equals(HttpStatus.BAD_REQUEST)) {
            return status + " " + e.getMessage();
        } else if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            return status + " " + e.getMessage();
        } else {
            return "Something went wrong: " + e.getMessage();
        }
    }


    public String handleException(HttpStatus status, Exception e) {
        String errorDetails = "Status: " + status + "\nMessage: " + e.getMessage() + "\nStackTrace: " + getStackTraceAsString(e);

        if (!isBusinessOrCausedByBusinessException(e)) {


            emailService.sendErrorEmail("Error Occurred in Application", errorDetails);
        }

        if(status.equals(HttpStatus.BAD_REQUEST)){
            return status + " " + e.getMessage();
        } else if(status.equals(HttpStatus.INTERNAL_SERVER_ERROR)){
            return status + " " + e.getMessage();
        } else {
            return "Something went wrong: " + e.getMessage();
        }
    }

    public String getUserIdSafely(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
            if (request != null) {
                Object userIdAttr = request.getSession().getAttribute("userId");
                if (userIdAttr != null) return userIdAttr.toString();
            }
        } catch (Exception ex) {
        }
        return "Unknown";
    }



}
