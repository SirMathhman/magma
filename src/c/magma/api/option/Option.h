import java.util.function.Function;
import java.util.function.Supplier;
public struct Option<T> {
	<R>(([Capture, ((Capture, T) => R)]) => Option<R>) map;
	(([Capture, ((Capture) => T)]) => T) orElseGet;
}