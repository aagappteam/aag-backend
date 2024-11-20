package aagapp_backend.services.exception;

public class VendorSubmissionException extends RuntimeException {

    public VendorSubmissionException(String message) {
        super(message);  // Pass the message to the parent constructor.
    }
}
