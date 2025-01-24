import java.util.Optional;struct PassUnit<T>{
	struct Table{
		<R>PassUnit<R> mapValue(Function<T, R> mapper);
		Optional<PassUnit<T>> filter(Predicate<T> predicate);
		<R>PassUnit<R> withValue(R value);
		PassUnit<T> enter();
		T value();
		<R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, Function<T, R> mapper);
		<R>PassUnit<R> flattenNode(BiFunction<State, T, R> mapper);
		PassUnit<T> exit();
	}
	struct Impl{}
}