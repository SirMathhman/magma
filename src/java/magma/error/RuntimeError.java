package magma.error;

public record RuntimeError(String message) implements Error {
    @Override
    public String display() {
        return message;
    }
}
