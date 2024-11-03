package magma;

import java.util.function.Consumer;

public record Some<T>(T value) implements Option<T> {
    @Override
    public void ifPresent(Consumer<T> consumer) {
        consumer.accept(value);
    }
}
