struct None<T> implements Option<T>{
	<R>Option<R> map(((T) => R) mapper);
	T orElseGet((() => T) other);
}