package magma.result;

import java.util.function.Consumer;

public record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public void consume(Consumer<T> onValid, Consumer<E> onInvalid) {
        onValid.accept(value);
    }
}
