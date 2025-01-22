struct None<T> implements Option<T> {<R>Option<R> map(Function<T, R> mapper){
	return new None<>();}T orElseGet(Supplier<T> other){
	return other.get();}}