import java.util.function.Function;
import java.util.function.Supplier;
public struct Some<T>(T value) implements Option<T> {
	<R>(([Capture, ((Capture, T) => R)]) => Option<R>) map=<R>Option<R> map([Capture, ((Capture, T) => R)] mapper){
		return new Some<>(mapper.apply(this.value));
	};
	(([Capture, ((Capture) => T)]) => T) orElseGet=T orElseGet([Capture, ((Capture) => T)] other){
		return this.value;
	};
}