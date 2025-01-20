import java.util.function.Function;
import java.util.function.Supplier;
public struct None<T> implements Option<T> {
	<R>(([Capture, ((Capture, T) => R)]) => Option<R>) map=<R>Option<R> map([Capture, ((Capture, T) => R)] mapper){
		return new None<>();
	};
	(([Capture, ((Capture) => T)]) => T) orElseGet=T orElseGet([Capture, ((Capture) => T)] other){
		return other.get();
	};
}