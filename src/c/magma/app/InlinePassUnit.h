import java.util.ArrayList;import java.util.List;import java.util.Optional;struct InlinePassUnit<T>(State state, List<Node> cache, T value) implements PassUnit<T>{
	public InlinePassUnit(T value){
		this(State.new(), ArrayList<>.new(), value);
	}
	Optional<PassUnit<T>> filter(Predicate<T> predicate){
		if(predicate.test(this.value))return Optional.of(this);
		return Optional.empty();
	}
	<R>PassUnit<R> withValue(R value){
		return InlinePassUnit<>.new();
	}
	PassUnit<T> enter(){
		return InlinePassUnit<>.new();
	}
	<R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, ((T) => R) mapper){
		return filterAndSupply(predicate, ()->InlinePassUnit<>.new());
	}
	<R>Optional<PassUnit<R>> filterAndSupply(Predicate<T> predicate, (() => PassUnit<R>) supplier){
		return predicate.test(this.value)
                ? Optional.of(supplier.get())
                : Optional.empty();
	}
	<R>PassUnit<R> flattenNode(BiFunction<State, T, R> mapper){
		return InlinePassUnit<>.new();
	}
	PassUnit<T> exit(){
		return InlinePassUnit<>.new();
	}
	<R>PassUnit<R> mapValue(((T) => R) mapper){
		var apply=mapper.apply(this.value);
		return InlinePassUnit<>.new();
	}
	struct InlinePassUnit new(){
		struct InlinePassUnit this;
		return this;
	}
}