#include "./Option.h"
struct Option<T>{
	<R>Option<R> map(Function<T, R> mapper);
	T orElseGet(Supplier<T> other);
	T orElse(T other);
	<R>Option<R> flatMap(Function<T, Option<R>> mapper);
	void ifPresent(Consumer<T> consumer);
	Option<T> or(Supplier<Option<T>> other);
	boolean isPresent();
	Option<T> filter(Predicate<T> predicate);
	boolean isEmpty();
	Tuple<Boolean, T> toTuple(T other);
}
