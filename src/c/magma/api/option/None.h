struct None<T> implements Option<T> {@Override
public <R>Option<R> map(Function<T, R> mapper){return new None<>();}@Override
public T orElseGet(Supplier<T> other){return other.get();}}