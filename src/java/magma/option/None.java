package magma.option;

import java.util.function.Consumer;
import java.util.function.Function;

public class None<T> implements Option<T> {
    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return new None<>();
    }

    @Override
    public T orElse(T other) {
        return other;
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
    }
}
