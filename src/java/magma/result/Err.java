package magma.result;

import java.util.Optional;
import java.util.function.Consumer;

public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public void consume(Consumer<T> valueConsumer, Consumer<X> errorConsumer) {
        errorConsumer.accept(error);
    }

    @Override
    public Optional<T> findValue() {
        return Optional.empty();
    }

    @Override
    public Optional<X> findError() {
        return Optional.of(error);
    }
}
