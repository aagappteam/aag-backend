package services.exception;

public class InvalidFileTypeException extends Exception {
    public InvalidFileTypeException(String message) {
        super(message);
    }
}