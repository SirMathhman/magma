package magma.io;

public record IOError(Error cause) implements Error {
    @Override
    public String display() {
        return this.cause.display();
    }
}
