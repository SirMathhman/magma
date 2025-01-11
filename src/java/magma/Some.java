package magma;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record Some<T>(T value) implements Option<T> {
    @Override
    public <R> R match(Function<T, R> ifPresent, Supplier<R> ifEmpty) {
        return ifPresent.apply(this.value);
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
        consumer.accept(this.value);
    }
}
