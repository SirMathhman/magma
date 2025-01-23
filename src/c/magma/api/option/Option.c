struct Option<T>{
	<R>Option<R> map(((T) => R) mapper);
	T orElseGet((() => T) other);struct Option<T> new(){struct Option<T> this;return this;}
}