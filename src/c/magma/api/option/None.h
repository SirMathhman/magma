import java.util.function.Function;
import java.util.function.Supplier;
public struct None<T> implements Option<T> {
	<R>Option<R> map=<R>Option<R> map(((T) => R) mapper){
		return new None<>();
	};
	T orElseGet=T orElseGet((() => T) other){
		return other.get();
	};
}