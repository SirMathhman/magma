package magma;

public record JavaError(Exception e) implements Error {
}
