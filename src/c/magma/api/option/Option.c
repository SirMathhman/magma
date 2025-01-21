import java.util.function.Function;
import java.util.function.Supplier;
public struct Option<T> {<R>Option<R> map(Function<T, R> mapper);T orElseGet(Supplier<T> other);}

