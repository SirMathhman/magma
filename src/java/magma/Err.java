package magma;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Err<>(error);
    }

    @Override
    public <R> R match(Function<T, R> onOk, Function<X, R> onErr) {
        return onErr.apply(error);
    }

    @Override
    public Optional<T> findValue() {
        return Optional.empty();
    }

    @Override
    public Optional<X> findError() {
        return Optional.of(error);
    }

    @Override
    public void consume(Consumer<T> valueConsumer, Consumer<X> errorConsumer) {
        errorConsumer.accept(error);
    }

    @Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper) {
        return new Err<>(error);
    }
}
