import java.util.function.Function;
import java.util.function.Supplier;
struct None<T> implements Option<T> {
	@Override
<R>Option<R> map(Function<T, R> mapper){
		return new None<>();
	}
	@Override
T orElseGet(Supplier<T> other){
		return other.get();
	}
}

