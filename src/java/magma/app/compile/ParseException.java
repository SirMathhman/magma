package magma.app.compile;

public class ParseException extends CompileException {
    public ParseException(String message, String context) {
        super(message, context);
    }
}
