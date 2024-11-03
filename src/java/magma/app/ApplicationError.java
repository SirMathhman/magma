package magma.app;

public record ApplicationError(Error cause) implements Error {
    @Override
    public String format(int depth, int index) {
        return cause.format(depth, index);
    }
}
