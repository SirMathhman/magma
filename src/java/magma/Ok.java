package magma;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(value));
    }

    @Override
    public <R> R match(Function<T, R> onOk, Function<X, R> onErr) {
        return onOk.apply(value);
    }

    @Override
    public Optional<T> findValue() {
        return Optional.of(value);
    }

    @Override
    public Optional<X> findError() {
        return Optional.empty();
    }

    @Override
    public void consume(Consumer<T> valueConsumer, Consumer<X> errorConsumer) {
        valueConsumer.accept(value);
    }
}
