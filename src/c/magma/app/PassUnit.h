import java.util.Optional;struct PassUnit<T>{
	struct VTable{
		<R>((Any, [Any, ((Any, T) => R)]) => PassUnit<R>) mapValue;
		((Any, Predicate<T>) => Optional<PassUnit<T>>) filter;
		<R>((Any, R) => PassUnit<R>) withValue;
		((Any) => PassUnit<T>) enter;
		((Any) => T) value;
		<R>((Any, Predicate<T>, [Any, ((Any, T) => R)]) => Optional<PassUnit<R>>) filterAndMapToValue;
		<R>((Any, BiFunction<State, T, R>) => PassUnit<R>) flattenNode;
		((Any) => PassUnit<T>) exit;
	}
	Box<Any> ref;
	struct VTable vtable;
	struct PassUnit new(Box<Any> ref, struct VTable vtable){
		struct PassUnit this;
		this.ref=ref;
		this.vtable=vtable;
		return this;
	}
}