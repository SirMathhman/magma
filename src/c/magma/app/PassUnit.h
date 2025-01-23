import java.util.Optional;struct PassUnit<T>{
	struct VTable{
		<R>PassUnit<R> (*)(void*, [void*, R (*)(void*, T)]) mapValue;
		Optional<PassUnit<T>> (*)(void*, Predicate<T>) filter;
		<R>PassUnit<R> (*)(void*, R) withValue;
		PassUnit<T> (*)(void*) enter;
		T (*)(void*) value;
		<R>Optional<PassUnit<R>> (*)(void*, Predicate<T>, [void*, R (*)(void*, T)]) filterAndMapToValue;
		<R>PassUnit<R> (*)(void*, [void*, R (*)(void*, State, T)]) flattenNode;
		PassUnit<T> (*)(void*) exit;
	}
	Box<void*> ref;
	struct VTable vtable;
	struct PassUnit new(Box<void*> ref, struct VTable vtable){
		struct PassUnit this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}