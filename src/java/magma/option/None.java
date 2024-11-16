package magma.option;

import java.util.function.Supplier;

public class None<T> implements Option<T> {
    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public T orElseGet(Supplier<T> other) {
        return other.get();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public T orElse(T other) {
        return other;
    }
}
