package magma;

public class GenerateException extends CompileException {
    public GenerateException(String message, Node context) {
        super(message, context.toString());
    }
}
