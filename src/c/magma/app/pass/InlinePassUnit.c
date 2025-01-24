struct InlinePassUnit<T>(State state, List<Node> cache, T value, List<String> namespace) implements PassUnit<T>{
	public InlinePassUnit(T value, JavaList<String> namespace){
		this(new State(), new ArrayList<>(), value, namespace.unwrap());
	}
	Optional<PassUnit<T>> filter(Predicate<T> predicate){
		if(predicate.test(this.value))return Optional.of(this);
		return Optional.empty();
	}
	<R>PassUnit<R> withValue(R value){
		return new InlinePassUnit<>(this.state, this.cache, value, this.namespace);
	}
	PassUnit<T> enter(){
		return new InlinePassUnit<>(this.state.enter(), this.cache, this.value, this.namespace);
	}
	<R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, Function<T, R> mapper){
		return filterAndSupply(predicate, ()->new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.value), this.namespace));
	}
	<R>Optional<PassUnit<R>> filterAndSupply(Predicate<T> predicate, Supplier<PassUnit<R>> supplier){
		return predicate.test(this.value)
                ? Optional.of(supplier.get())
                : Optional.empty();
	}
	<R>PassUnit<R> flattenNode(BiFunction<State, T, R> mapper){
		return new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.state, this.value), this.namespace);
	}
	PassUnit<T> exit(){
		return new InlinePassUnit<>(this.state.exit(), this.cache, this.value, this.namespace);
	}
	List<String> findNamespace(){
		return this.namespace;
	}
	<R>PassUnit<R> mapValue(Function<T, R> mapper){
		var apply=mapper.apply(this.value);
		return new InlinePassUnit<>(this.state, this.cache, apply, this.namespace);
	}
}
