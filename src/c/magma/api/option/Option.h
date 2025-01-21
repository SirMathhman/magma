import java.util.function.Function;
import java.util.function.Supplier;

<R>Option<R> map(Function<T, R> mapper);

T orElseGet(Supplier<T> other);
struct Option<T> {
}

