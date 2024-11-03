package magma.app;

public record ApplicationError(Error cause) implements Error {
    @Override
    public String asString() {
        return cause().asString();
    }
}
