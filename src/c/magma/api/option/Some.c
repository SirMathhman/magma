import java.util.function.Function;
import java.util.function.Supplier;

<R>Option<R> map(Function<T, R> mapper){
	return new Some<>(mapper.apply(this.value));
}

T orElseGet(Supplier<T> other){
	return this.value;
}
struct Some<T>(T value) implements Option<T> {
}

