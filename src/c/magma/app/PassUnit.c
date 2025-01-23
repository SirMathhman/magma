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
	struct VTable vtable;
	struct PassUnit new(struct VTable table){
		struct PassUnit this;
		this.table=table;
		return this;
	}
}