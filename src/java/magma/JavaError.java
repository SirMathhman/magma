package magma;

public record JavaError(Exception exception) implements Error {
}
