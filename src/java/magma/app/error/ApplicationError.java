package magma.app.error;

public record ApplicationError(Error error) implements Error {
    @Override
    public String display() {
        return error.display();
    }

    @Override
    public String format(int depth) {
        return error.format(depth);
    }
}
