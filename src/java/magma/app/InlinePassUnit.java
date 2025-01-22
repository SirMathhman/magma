package magma.app;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record InlinePassUnit<T>(State state, T value) implements PassUnit<T> {
    public InlinePassUnit(T value) {
        this(new State(), value);
    }

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

    @Override
    public <R> Optional<PassUnit<R>> filterAndMapValue(Predicate<T> predicate, Function<T, R> mapper) {
        return predicate.test(this.value)
                ? Optional.of(new InlinePassUnit<>(this.state, mapper.apply(this.value)))
                : Optional.empty();
    }

    @Override
    public <R> PassUnit<R> flattenNode(BiFunction<State, T, R> mapper) {
        return new InlinePassUnit<>(this.state, mapper.apply(this.state, this.value));
    }

    @Override
    public PassUnit<T> exit() {
        return new InlinePassUnit<>(this.state.exit(), this.value);
    }

    @Override
    public <R> PassUnit<R> mapValue(Function<T, R> mapper) {
        final var value1 = value();
        final var apply = mapper.apply(value1);
        return new InlinePassUnit<>(this.state, apply);
    }
}