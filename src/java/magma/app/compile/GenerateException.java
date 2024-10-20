package magma.app.compile;

public class GenerateException extends CompileException {
    public GenerateException(String message, Node context) {
        super(message, "\n" + context.toString());
    }
}
