package magma.api.error;

public record RuntimeError(String message) implements Error {
    @Override
    public String display() {
        return message;
    }

    @Override
    public String format(int depth) {
        return message;
    }
}
