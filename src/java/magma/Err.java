package magma;

import java.util.function.Consumer;

public record Err<T, E>(E error) implements Result<T, E> {
    @Override
    public void consume(Consumer<T> onValid, Consumer<E> onInvalid) {
        onInvalid.accept(error);
    }
}
