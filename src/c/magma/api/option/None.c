import java.util.function.Function;
import java.util.function.Supplier;

<R>Option<R> map(Function<T, R> mapper){
	return new None<>();
}

T orElseGet(Supplier<T> other){
	return other.get();
}
struct None<T> implements Option<T> {
}

