package magma;

public record ApplicationError(Error error) implements Error {
    @Override
    public String display() {
        return error.display();
    }
}
