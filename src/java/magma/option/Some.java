package magma.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record Some<T>(T value) implements Option<T> {
    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public T orElseNull() {
        return value;
    }

    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return new Some<>(mapper.apply(value));
    }

    @Override
    public T orElseGet(Supplier<T> other) {
        return value;
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
        consumer.accept(value);
    }

    @Override
    public Option<T> or(Supplier<Option<T>> other) {
        return this;
    }

    @Override
    public <R> R match(Function<T, R> ifPresent, Supplier<R> ifEmpty) {
        return ifPresent.apply(value);
    }
}
