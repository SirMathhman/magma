import java.util.function.Function;
import java.util.function.Supplier;

@Override
<R>Option<R> map(Function<T, R> mapper){
	return new Some<>(mapper.apply(this.value));
}

@Override
T orElseGet(Supplier<T> other){
	return this.value;
}
struct Some<T>(T value) implements Option<T> {
}

