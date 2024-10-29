package magma;

import java.util.function.Consumer;

public interface Result<T, E> {
    void consume(Consumer<T> onValid, Consumer<E> onInvalid);
}
