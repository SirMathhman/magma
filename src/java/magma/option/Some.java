package magma.option;

import magma.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record Some<T>(T value) implements Option<T> {
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
    public <R> Option<R> flatMapValue(Function<T, Option<R>> mapper) {
        return mapper.apply(value);
    }

    @Override
    public Option<T> or(Supplier<Option<T>> supplier) {
        return this;
    }

    @Override
    public T orElse(T other) {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public <R> Option<Tuple<T, R>> and(Supplier<Option<R>> supplier) {
        return supplier.get().map(otherValue -> new Tuple<>(value, otherValue));
    }
}
