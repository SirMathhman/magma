struct Some<T>(T value) implements Option<T>{
	<R>Option<R> map(((T) => R) mapper){
		return Some<>.new();
	}
	T orElseGet((() => T) other){
		return this.value;
	}
}