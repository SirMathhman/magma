package magma;

public record Ok<T, X>(T s) implements Result<T, X> {
}
