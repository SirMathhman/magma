#include "magma/app/Node.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"
#include "java/util/Optional.h"
#include "java/util/function/BiFunction.h"
#include "java/util/function/Function.h"
#include "java/util/function/Predicate.h"
#include "java/util/function/Supplier.h"
struct InlinePassUnit<T>(State state, List<Node> cache, T value) implements PassUnit<T>{
	public InlinePassUnit(T value){
		this(new State(), new ArrayList<>(), value);
	}
	Optional<PassUnit<T>> filter(Predicate<T> predicate){
		if(predicate.test(this.value))return Optional.of(this);
		return Optional.empty();
	}
	<R>PassUnit<R> withValue(R value){
		return new InlinePassUnit<>(this.state, this.cache, value);
	}
	PassUnit<T> enter(){
		return new InlinePassUnit<>(this.state.enter(), this.cache, this.value);
	}
	<R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, Function<T, R> mapper){
		return filterAndSupply(predicate, ()->new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.value)));
	}
	<R>Optional<PassUnit<R>> filterAndSupply(Predicate<T> predicate, Supplier<PassUnit<R>> supplier){
		return predicate.test(this.value)
                ? Optional.of(supplier.get())
                : Optional.empty();
	}
	<R>PassUnit<R> flattenNode(BiFunction<State, T, R> mapper){
		return new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.state, this.value));
	}
	PassUnit<T> exit(){
		return new InlinePassUnit<>(this.state.exit(), this.cache, this.value);
	}
	<R>PassUnit<R> mapValue(Function<T, R> mapper){
		var apply=mapper.apply(this.value);
		return new InlinePassUnit<>(this.state, this.cache, apply);
	}
}