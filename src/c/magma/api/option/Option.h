import java.util.function.Function;
import java.util.function.Supplier;
public struct Option<T> {
	<R>Option<R> map(((T) => R) mapper);
	T orElseGet((() => T) other);
}