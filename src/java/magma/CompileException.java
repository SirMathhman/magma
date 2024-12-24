package magma;

public class CompileException extends ApplicationError {
    public CompileException(String message, String context) {
        super(message + ": " + context);
    }

    public CompileException(String message) {
        super(message);
    }
}
