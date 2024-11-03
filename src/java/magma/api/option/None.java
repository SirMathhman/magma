package magma.api.option;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class None<T> implements Option<T> {
    @Override
    public void ifPresent(Consumer<T> consumer) {
    }

    @Override
    public T orElse(T other) {
        return other;
    }

    @Override
    public <R> Option<R> map(Function<T, R> mapper) {
        return new None<>();
    }

    @Override
    public <R> Option<R> flatMap(Function<T, Option<R>> mapper) {
        return new None<>();
    }

    @Override
    public T orElseGet(Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public Option<T> or(Supplier<Option<T>> supplier) {
        return supplier.get();
    }
}
