import java.util.Optional;struct PassUnit<Capture, T>{
	struct VTable{
		<R>((((T) => R)) => PassUnit<R>) mapValue;
		((Predicate<T>) => Optional<PassUnit<T>>) filter;
		<R>((R) => PassUnit<R>) withValue;
		(() => PassUnit<T>) enter;
		(() => T) value;
		<R>((Predicate<T>, ((T) => R)) => Optional<PassUnit<R>>) filterAndMapToValue;
		<R>((BiFunction<State, T, R>) => PassUnit<R>) flattenNode;
		(() => PassUnit<T>) exit;
	}
	struct VTable vtable;
	struct PassUnit new(struct VTable table){
		struct PassUnit this;
		this.table=table;
		return this;
	}
}