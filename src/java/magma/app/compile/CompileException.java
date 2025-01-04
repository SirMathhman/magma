package magma.app.compile;

public class CompileException extends Exception {
    public CompileException(String message, String context) {
        super(format(message, context));
    }

    public CompileException(String message, String context, CompileException cause) {
        super(format(message, context), cause);
    }

    private static String format(String message, String context) {
        return message + ": " + context;
    }
}
