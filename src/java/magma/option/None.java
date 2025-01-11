package magma.option;

import magma.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class None<T> implements Option<T> {
    public None() {
    }

    @Override
    public <R> R match(Function<T, R> ifPresent, Supplier<R> ifEmpty) {
        return ifEmpty.get();
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
    }

    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return new None<>();
    }

    @Override
    public T orElseGet(Supplier<T> other) {
        return other.get();
    }

    @Override
    public Tuple<Boolean, T> toTuple(T other) {
        return new Tuple<>(false, other);
    }

    @Override
    public Option<T> or(Supplier<Option<T>> other) {
        return other.get();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public T unwrap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T orElse(T other) {
        return other;
    }
}
