import java.util.function.Function;
import java.util.function.Supplier;
struct Some<T>(T value) implements Option<T> {
	@Override
<R>Option<R> map(Function<T, R> mapper){
		return new Some<>(mapper.apply(this.value));
	}
	@Override
T orElseGet(Supplier<T> other){
		return this.value;
	}
}

