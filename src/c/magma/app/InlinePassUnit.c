import magma.api.Tuple;import java.util.ArrayList;import java.util.List;import java.util.Optional;struct InlinePassUnit<T>(
        State state,
        List<Node> cache,
        T value
) implements PassUnit<T>{
	public InlinePassUnit(T value);
	Optional<PassUnit<T>> filter(Predicate<T> predicate);
	<R>PassUnit<R> withValue(R value);
	PassUnit<T> enter();
	<R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, ((T) => R) mapper);
	<R>Optional<PassUnit<R>> filterAndSupply(Predicate<T> predicate, (() => PassUnit<R>) supplier);
	Optional<PassUnit<T>> filterAndMapToCached(Predicate<T> predicate, ((T) => Tuple<List<Node>, T>) mapper);
	<R>PassUnit<R> flattenNode(BiFunction<State, T, R> mapper);
	PassUnit<T> exit();
	<R>PassUnit<R> mapValue(((T) => R) mapper);
}