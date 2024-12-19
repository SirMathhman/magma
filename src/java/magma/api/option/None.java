package magma.api.option;

import magma.api.Tuple;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class None<T> implements Option<T> {
    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public T orElseNull() {
        return null;
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
    public void ifPresent(Consumer<T> consumer) {
    }

    @Override
    public Option<T> or(Supplier<Option<T>> other) {
        return other.get();
    }

    @Override
    public <R> R match(Function<T, R> ifPresent, Supplier<R> ifEmpty) {
        return ifEmpty.get();
    }

    @Override
    public Option<T> filter(Predicate<T> predicate) {
        return new None<>();
    }

    @Override
    public Tuple<Boolean, T> toTuple(T other) {
        return new Tuple<>(false, other);
    }

    @Override
    public <R> Option<R> flatMap(Function<T, Option<R>> mapper) {
        return new None<>();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
