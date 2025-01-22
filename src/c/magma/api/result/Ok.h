import magma.api.Tuple;import magma.api.option.None;import magma.api.option.Option;import magma.api.option.Some;import java.util.Optional;struct Ok<T, X>(T value) implements Result<T, X> {@Override
public <R>Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper){return mapper.apply(this.value);}@Override
public <R>Result<R, X> mapValue(Function<T, R> mapper){return new Ok<>(mapper.apply(this.value));}@Override
public <R>Result<T, R> mapErr(Function<X, R> mapper){return new Ok<>(this.value);}@Override
public <R>R match(Function<T, R> onOk, Function<X, R> onErr){return onOk.apply(this.value);}@Override
public <R>Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other){return other.get().mapValue(()->new Tuple<>(this.value, otherValue));}@Override
public <R>Result<T, Tuple<X, R>> or(Supplier<Result<T, R>> other){return new Ok<>(this.value);}@Override
public boolean isOk(){return true;}private Optional<X> findError0(){return Optional.empty();}@Override
public Option<X> findError(){return findError0().<Option<X>>map(Some::new).orElseGet(None::new);}}