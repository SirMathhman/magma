import java.util.Optional;struct PassUnit<T>{
	<R>PassUnit<R> mapValue(Tuple<any*, R (*)(any*, T)> mapper);
	Optional<PassUnit<T>> filter(Predicate<T> predicate);
	<R>PassUnit<R> withValue(R value);
	PassUnit<T> enter();
	T value();
	<R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, Tuple<any*, R (*)(any*, T)> mapper);
	<R>PassUnit<R> flattenNode(BiFunction<State, T, R> mapper);
	PassUnit<T> exit();
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}