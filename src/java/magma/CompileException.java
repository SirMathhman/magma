package magma;

public class CompileException extends Exception {
    public CompileException(String message, String context) {
        super(message + ": " + context);
    }

    public CompileException(String message, String context, CompileException cause) {
        super(message + ": " + context, cause);
    }
}
