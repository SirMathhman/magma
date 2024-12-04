package magma;

public record ApplicationError(Error e) implements Error {
    @Override
    public String display() {
        return e.display();
    }
}
