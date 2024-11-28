package magma;

public record RuntimeError(String message) implements Error {
    @Override
    public String display() {
        return message;
    }
}
