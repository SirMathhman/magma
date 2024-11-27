package magma;

public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public boolean isErr() {
        return true;
    }

    @Override
    public Option<X> findErr() {
        return new Some<>(error);
    }

    @Override
    public Option<T> findValue() {
        return new None<>();
    }
}
