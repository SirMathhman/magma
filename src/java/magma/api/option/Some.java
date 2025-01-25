package magma.api.option;

import magma.api.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record Some<T>(T value) implements Option<T> {
    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return new Some<>(mapper.apply(this.value));
    }

    @Override
    public T orElseGet(Supplier<T> other) {
        return this.value;
    }

    @Override
    public T orElse(T other) {
        return this.value;
    }

    @Override
    public <R> Option<R> flatMap(Function<T, Option<R>> mapper) {
        return mapper.apply(this.value);
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
        consumer.accept(this.value);
    }

    @Override
    public Option<T> or(Supplier<Option<T>> other) {
        return this;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public Option<T> filter(Predicate<T> predicate) {
        return predicate.test(this.value) ? this : new None<>();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Tuple<Boolean, T> toTuple(T other) {
        return new Tuple<>(true, this.value);
    }

    @Override
    public <R> Option<Tuple<T, R>> and(Supplier<Option<R>> supplier) {
        return supplier.get().map(otherValue -> new Tuple<>(this.value, otherValue));
    }
}
