package magma.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class None<T> implements Option<T> {
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
    public <R> Option<R> flatMapValue(Function<T, Option<R>> mapper) {
        return new None<>();
    }

    @Override
    public Option<T> or(Supplier<Option<T>> supplier) {
        return supplier.get();
    }
}
