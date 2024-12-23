package magma;

public class CompileException extends Exception {
    public CompileException(String message, String cause) {
        super(message + ": " + cause);
    }
}
