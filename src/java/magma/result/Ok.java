package magma.result;

import java.util.Optional;
import java.util.function.Consumer;

public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public void consume(Consumer<T> valueConsumer, Consumer<X> errorConsumer) {
        valueConsumer.accept(value);
    }

    @Override
    public Optional<T> findValue() {
        return Optional.of(value);
    }

    @Override
    public Optional<X> findError() {
        return Optional.empty();
    }
}
