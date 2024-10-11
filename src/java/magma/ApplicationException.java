package magma;

public class ApplicationException extends Exception {
    public ApplicationException(Exception cause) {
        super(cause);
    }

    public ApplicationException() {
    }
}
