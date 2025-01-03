package magma.app.compile;

public class CompileException extends Exception {
    public CompileException(String message, String context) {
        super(message + ": " + context);
    }
}
