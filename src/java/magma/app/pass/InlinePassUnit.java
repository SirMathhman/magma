package magma.app.pass;

import magma.app.Node;
import magma.java.JavaList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record InlinePassUnit<T>(
        State state,
        List<Node> cache,
        T value,
        List<String> namespace) implements PassUnit<T> {
    public InlinePassUnit(T value, JavaList<String> namespace) {
        this(new State(), new ArrayList<>(), value, namespace.unwrap());
    }

    @Override
    public Optional<PassUnit<T>> filter(Predicate<T> predicate) {
        if (predicate.test(this.value)) return Optional.of(this);
        return Optional.empty();
    }

    @Override
    public <R> PassUnit<R> withValue(R value) {
        return new InlinePassUnit<>(this.state, this.cache, value, this.namespace);
    }

    @Override
    public PassUnit<T> enter() {
        return new InlinePassUnit<>(this.state.enter(), this.cache, this.value, this.namespace);
    }

    @Override
    public <R> Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, Function<T, R> mapper) {
        return filterAndSupply(predicate, () -> new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.value), this.namespace));
    }

    private <R> Optional<PassUnit<R>> filterAndSupply(Predicate<T> predicate, Supplier<PassUnit<R>> supplier) {
        return predicate.test(this.value)
                ? Optional.of(supplier.get())
                : Optional.empty();
    }

    @Override
    public <R> PassUnit<R> flattenNode(BiFunction<State, T, R> mapper) {
        return new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.state, this.value), this.namespace);
    }

    @Override
    public PassUnit<T> exit() {
        return new InlinePassUnit<>(this.state.exit(), this.cache, this.value, this.namespace);
    }

    @Override
    public List<String> findNamespace() {
        return this.namespace;
    }

    @Override
    public <R> PassUnit<R> mapValue(Function<T, R> mapper) {
        final var apply = mapper.apply(this.value);
        return new InlinePassUnit<>(this.state, this.cache, apply, this.namespace);
    }
}