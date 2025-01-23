import magma.api.result.Result;import java.util.Optional;struct Stream<T>{
	struct VTable{
		Optional<T> (*)(void*, [void*, T (*)(void*, T, T)]) foldLeft;
		<R>R (*)(void*, R, [void*, R (*)(void*, R, T)]) foldLeft;
		<R>Stream<R> (*)(void*, [void*, R (*)(void*, T)]) map;
		<R, X>Result<R, X> (*)(void*, R, [void*, Result<R, X> (*)(void*, R, T)]) foldLeftToResult;
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