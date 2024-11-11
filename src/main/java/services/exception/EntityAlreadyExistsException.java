package services.exception;

public class EntityAlreadyExistsException extends Exception
{
    String message;
    public EntityAlreadyExistsException(String message)
    {
        this.message = message;
    }
}
