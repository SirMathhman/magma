package magma.app;

import java.util.Optional;
import java.util.function.Predicate;

public record InlinePassUnit<T>(State state, T value) implements PassUnit<T> {
    @Override
    public Optional<PassUnit<T>> filter(Predicate<T> predicate) {
        if (predicate.test(this.value)) return Optional.of(this);
        return Optional.empty();
    }

    @Override
    public <R> PassUnit<R> withValue(R value) {
        return new InlinePassUnit<>(this.state, value);
    }

    @Override
    public PassUnit<T> enter() {
        return new InlinePassUnit<>(this.state.enter(), this.value);
    }
}
