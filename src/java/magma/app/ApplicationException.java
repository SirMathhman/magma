package magma.app;

public class ApplicationException extends Exception {
    public ApplicationException(Exception cause) {
        super(cause);
    }

    public ApplicationException() {
    }

    public ApplicationException(String message) {
        super(message);
    }
}
