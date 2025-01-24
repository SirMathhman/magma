#include "../../../magma/api/Tuple.h"
#include "../../../java/util/function/Consumer.h"
#include "../../../java/util/function/Function.h"
#include "../../../java/util/function/Predicate.h"
#include "../../../java/util/function/Supplier.h"
struct Some<T>(T value) implements Option<T>{
	<R>Option<R> map(Function<T, R> mapper){
		return new Some<>(mapper.apply(this.value));
	}
	T orElseGet(Supplier<T> other){
		return this.value;
	}
	T orElse(T other){
		return this.value;
	}
	<R>Option<R> flatMap(Function<T, Option<R>> mapper){
		return mapper.apply(this.value);
	}
	void ifPresent(Consumer<T> consumer){
		consumer.accept(this.value);
	}
	Option<T> or(Supplier<Option<T>> other){
		return this;
	}
	boolean isPresent(){
		return true;
	}
	Option<T> filter(Predicate<T> predicate){
		return predicate.test(this.value) ? this : new None<>();
	}
	boolean isEmpty(){
		return false;
	}
	Tuple<Boolean, T> toTuple(T other){
		return new Tuple<>(true, this.value);
	}
}
