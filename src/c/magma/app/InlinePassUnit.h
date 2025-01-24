import java.util.ArrayList;import java.util.List;import java.util.Optional;struct InlinePassUnit<T>(State state, List<Node> cache, T value){
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
	<R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, Tuple<any*, R (*)(T)> mapper){
		return filterAndSupply(predicate, ()->new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.value)));
	}
	<R>Optional<PassUnit<R>> filterAndSupply(Predicate<T> predicate, Tuple<any*, PassUnit<R> (*)()> supplier){
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
	<R>PassUnit<R> mapValue(Tuple<any*, R (*)(T)> mapper){
		var apply=mapper.apply(this.value);
		return new InlinePassUnit<>(this.state, this.cache, apply);
	}
	PassUnit<T> PassUnit(){
		return PassUnit.new();
	}
}