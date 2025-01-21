import java.util.function.Function;
import java.util.function.Supplier;

@Override
<R>Option<R> map(Function<T, R> mapper){
	return new None<>();
}

@Override
T orElseGet(Supplier<T> other){
	return other.get();
}
struct None<T> implements Option<T> {
}

