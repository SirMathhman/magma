import java.util.Optional;struct PassUnit<T>{
	<R>PassUnit<R> mapValue(((T) => R) mapper);
	Optional<PassUnit<T>> filter(Predicate<T> predicate);
	<R>PassUnit<R> withValue(R value);
	PassUnit<T> enter();
	T value();
	<R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, ((T) => R) mapper);
	<R>PassUnit<R> flattenNode(BiFunction<State, T, R> mapper);
	PassUnit<T> exit();
	struct PassUnit new(){
		struct PassUnit this;
		return this;
	}
}