package services.exception;

public class CustomerDoesNotExistsException extends Exception
{
    String message;
    public CustomerDoesNotExistsException(String message)
    {
        this.message = message;
    }
}
