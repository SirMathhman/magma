package magma;

import java.util.function.Supplier;

public record Some<T>(T value) implements Option<T> {
    @Override
    public T orElseGet(Supplier<T> other) {
        return value;
    }
}
