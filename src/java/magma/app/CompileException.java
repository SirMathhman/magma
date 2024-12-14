package magma.app;

public class CompileException extends ApplicationException {
    public CompileException(String message, String context) {
        super(formatMessage(message, context));
    }

    public CompileException(String message, String context, Throwable cause) {
        super(formatMessage(message, context), cause);
    }

    private static String formatMessage(String message, String context) {
        return message + ": " + context;
    }
}

