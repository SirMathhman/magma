struct None<T> implements Option<T>{
	struct Table{
		<R>Option<R> map(Function<T, R> mapper){
			return new None<>();
		}
		T orElseGet(Supplier<T> other){
			return other.get();
		}
	}
	struct Impl{}
}