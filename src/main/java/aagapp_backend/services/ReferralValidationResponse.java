package aagapp_backend.services;

import org.springframework.http.HttpStatus;

public class ReferralValidationResponse {
    private boolean valid;
    private String message;
    private HttpStatus status_code;

    public ReferralValidationResponse(boolean valid, String message,HttpStatus status_code) {
        this.valid = valid;
        this.message = message;
        this.status_code=status_code;
    }

    // Getters
    public boolean isValid() { return valid; }
    public String getMessage() { return message; }
    public HttpStatus getStatus_code(){
        return status_code;
    }
}

