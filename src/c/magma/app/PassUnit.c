import java.util.Optional;struct PassUnit<T>{
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
	struct PassUnit new(){
		struct PassUnit this;
		return this;
	}
}