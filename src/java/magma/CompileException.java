package magma;

public class CompileException extends ApplicationException {
    public CompileException() {
    }

    public CompileException(String message, String context) {
        super(message + ": " + context);
    }
}
