package magma;

import java.util.function.Consumer;

public class None<T> implements Option<T> {
    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
    }

    @Override
    public T orElse(T other) {
        return other;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}
