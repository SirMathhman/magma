import java.util.Optional;struct PassUnit<T>{
	struct VTable{
		<R>((void*, [void*, ((void*, T) => R)]) => PassUnit<R>) mapValue;
		((void*, Predicate<T>) => Optional<PassUnit<T>>) filter;
		<R>((void*, R) => PassUnit<R>) withValue;
		((void*) => PassUnit<T>) enter;
		((void*) => T) value;
		<R>((void*, Predicate<T>, [void*, ((void*, T) => R)]) => Optional<PassUnit<R>>) filterAndMapToValue;
		<R>((void*, BiFunction<State, T, R>) => PassUnit<R>) flattenNode;
		((void*) => PassUnit<T>) exit;
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