package aagapp_backend.services.exception;

public class EntityDoesNotExistsException extends Exception
{
    String message;
    public EntityDoesNotExistsException(String message)
    {
        this.message = message;
    }
}
