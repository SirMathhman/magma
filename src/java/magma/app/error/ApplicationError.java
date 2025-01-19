package magma.app.error;

public record ApplicationError(Error cause) implements Error {
    @Override
    public String display() {
        return this.cause.display();
    }
}
