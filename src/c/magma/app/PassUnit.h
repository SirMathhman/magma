import java.util.Optional;struct PassUnit<T>{
	<R>PassUnit<R> mapValue(any* _ref_, Tuple<any*, R (*)(any*, T)> mapper);
	Optional<PassUnit<T>> filter(any* _ref_, Predicate<T> predicate);
	<R>PassUnit<R> withValue(any* _ref_, R value);
	PassUnit<T> enter();
	T value();
	<R>Optional<PassUnit<R>> filterAndMapToValue(any* _ref_, Predicate<T> predicate, Tuple<any*, R (*)(any*, T)> mapper);
	<R>PassUnit<R> flattenNode(any* _ref_, Tuple<any*, R (*)(any*, State, T)> mapper);
	PassUnit<T> exit();
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}