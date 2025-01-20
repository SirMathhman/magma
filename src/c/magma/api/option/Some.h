import java.util.function.Function;
import java.util.function.Supplier;
public struct Some<T>(T value) implements Option<T> {
	<R>Option<R> map=<R>Option<R> map(((T) => R) mapper){
		return new Some<>(mapper.apply(this.value));
	};
	T orElseGet=T orElseGet((() => T) other){
		return this.value;
	};
}