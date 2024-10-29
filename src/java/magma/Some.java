package magma;

import java.util.function.Consumer;
import java.util.function.Function;

public record Some<T>(T value) implements Option<T> {
    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return new Some<>(mapper.apply(value));
    }

    @Override
    public T orElse(T other) {
        return value;
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
        consumer.accept(value);
    }
}
