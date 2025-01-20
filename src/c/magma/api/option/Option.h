import java.util.function.Function;
import java.util.function.Supplier;
public struct Option<T> {
	<R>((((T) => R)) => Option<R>) map;
	(((() => T)) => T) orElseGet;
}