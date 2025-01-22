import java.util.function.Function;import java.util.function.Supplier;struct Some<T>(T value) implements Option<T> {@Override
public <R>Option<R> map(Function<T, R> mapper){return new Some<>(mapper.apply(this.value));}@Override
public T orElseGet(Supplier<T> other){return this.value;}}