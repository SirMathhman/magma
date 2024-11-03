package magma.app;

public record ApplicationError(Error cause) implements Error {
    @Override
    public String format(int depth) {
        return cause.format(depth);
    }
}
