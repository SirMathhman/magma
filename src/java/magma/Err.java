package magma;

public record Err<T, X>(X invalidRoot) implements Result<T, X> {
}
