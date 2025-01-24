#include "./None.h"
struct None<T> implements Option<T>{
	<R>Option<R> map(Function<T, R> mapper){
		return new None<>();
	}
	T orElseGet(Supplier<T> other){
		return other.get();
	}
	T orElse(T other){
		return other;
	}
	<R>Option<R> flatMap(Function<T, Option<R>> mapper){
		return new None<>();
	}
	void ifPresent(Consumer<T> consumer){}
	Option<T> or(Supplier<Option<T>> other){
		return other.get();
	}
	boolean isPresent(){
		return false;
	}
	Option<T> filter(Predicate<T> predicate){
		return new None<>();
	}
	boolean isEmpty(){
		return true;
	}
	Tuple<Boolean, T> toTuple(T other){
		return new Tuple<>(false, other);
	}
}
