struct Option<T>{
	<R>Option<R> map(((T) => R) mapper);
	T orElseGet((() => T) other);struct Option new(){struct Option this;return this;}
}