package magma.app;

import magma.api.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface PassUnit<T> {
    <R> PassUnit<R> mapValue(Function<T, R> mapper);

    Optional<PassUnit<T>> filter(Predicate<T> predicate);

    <R> PassUnit<R> withValue(R value);

    PassUnit<T> enter();

    @Deprecated
    T value();

    <R> Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, Function<T, R> mapper);

    Optional<PassUnit<T>> filterAndMapToCached(Predicate<T> predicate, Function<T, Tuple<List<Node>, T>> mapper);

    <R> PassUnit<R> flattenNode(BiFunction<State, T, R> mapper);

    PassUnit<T> exit();
}
