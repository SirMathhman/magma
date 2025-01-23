import magma.api.result.Result;import java.util.Optional;struct Stream<T>{
	struct VTable{
		((BiFunction<T, T, T>) => Optional<T>) foldLeft;
		<R>((R, BiFunction<R, T, R>) => R) foldLeft;
		<R>((((T) => R)) => Stream<R>) map;
		<R, X>((R, BiFunction<R, T, Result<R, X>>) => Result<R, X>) foldLeftToResult;
	}
	struct Stream new(){
		struct Stream this;
		return this;
	}
}