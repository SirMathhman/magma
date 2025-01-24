import magma.api.result.Result;import java.util.Optional;struct Stream<T>{
	Optional<T> foldLeft(any* _ref_, Tuple<any*, T (*)(any*, T, T)> folder);
	<R>R foldLeft(any* _ref_, R initial, Tuple<any*, R (*)(any*, R, T)> folder);
	<R>Stream<R> map(any* _ref_, Tuple<any*, R (*)(any*, T)> mapper);
	<R, X>Result<R, X> foldLeftToResult(any* _ref_, R initial, Tuple<any*, Result<R, X> (*)(any*, R, T)> folder);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}