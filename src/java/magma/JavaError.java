package magma;

public record JavaError<X extends Exception>(X error) implements Error {
    @Override
    public String display() {
        return error.getMessage();
    }
}
