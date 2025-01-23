import magma.api.result.Result;import java.util.Optional;struct Stream<T>{
	struct VTable{
		((Any, BiFunction<T, T, T>) => Optional<T>) foldLeft;
		<R>((Any, R, BiFunction<R, T, R>) => R) foldLeft;
		<R>((Any, [Any, ((Any, T) => R)]) => Stream<R>) map;
		<R, X>((Any, R, BiFunction<R, T, Result<R, X>>) => Result<R, X>) foldLeftToResult;
	}
	struct VTable vtable;
	struct Stream new(struct VTable table){
		struct Stream this;
		this.table=table;
		return this;
	}
}