package magma;

import java.util.function.Supplier;

public class None<T> implements Option<T> {
    @Override
    public T orElseGet(Supplier<T> other) {
        return other.get();
    }
}
