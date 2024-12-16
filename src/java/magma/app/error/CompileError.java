package magma.app.error;

public record CompileError(String message, Context context) implements Error {
    @Override
    public String display() {
        return message + ": " + context.display();
    }
}