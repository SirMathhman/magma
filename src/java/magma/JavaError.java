package magma;

public record JavaError(Exception exception) implements Error {
    @Override
    public String display() {
        return exception.getMessage();
    }
}
