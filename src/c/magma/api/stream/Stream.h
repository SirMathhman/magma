import magma.api.result.Result;import java.util.Optional;struct Stream<T>{
	Optional<T> foldLeft(Tuple<any*, T (*)(any*, T, T)> folder);
	<R>R foldLeft(R initial, Tuple<any*, R (*)(any*, R, T)> folder);
	<R>Stream<R> map(Tuple<any*, R (*)(any*, T)> mapper);
	<R, X>Result<R, X> foldLeftToResult(R initial, Tuple<any*, Result<R, X> (*)(any*, R, T)> folder);
	struct Impl{
		struct Impl new(){
			struct Impl this;
			return this;
		}
	}
}