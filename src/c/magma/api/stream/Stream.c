import magma.api.result.Result;import java.util.Optional;struct Stream<T>{
	struct VTable{
		((void*, BiFunction<T, T, T>) => Optional<T>) foldLeft;
		<R>((void*, R, BiFunction<R, T, R>) => R) foldLeft;
		<R>((void*, [void*, ((void*, T) => R)]) => Stream<R>) map;
		<R, X>((void*, R, BiFunction<R, T, Result<R, X>>) => Result<R, X>) foldLeftToResult;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct Stream new(Box<void*> ref, struct VTable vtable){
		struct Stream this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}