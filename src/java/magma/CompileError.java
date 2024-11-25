package magma;

public record CompileError(String message, String context) implements Error {
    @Override
    public String display() {
        return message + ": " + context;
    }
}
