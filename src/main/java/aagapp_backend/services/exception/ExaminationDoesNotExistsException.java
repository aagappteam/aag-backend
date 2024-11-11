package aagapp_backend.services.exception;

public class ExaminationDoesNotExistsException extends Exception
{
    String message;
    public ExaminationDoesNotExistsException(String message)
    {
        this.message = message;
    }
}
