package magma.app;

public class CompileException extends ApplicationException {
    public CompileException(String message, String context) {
        super(message + ": " + context);
    }
}

