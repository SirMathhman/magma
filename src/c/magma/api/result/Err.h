import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
struct Err<T, X>(X error) implements Result<T, X> {
	@Override
<R>Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper){
		return new Err<>(this.error);
	}
	@Override
<R>Result<R, X> mapValue(Function<T, R> mapper){
		return new Err<>(this.error);
	}
	@Override
<R>Result<T, R> mapErr(Function<X, R> mapper){
		return new Err<>(mapper.apply(this.error));
	}
	@Override
<R>R match(Function<T, R> onOk, Function<X, R> onErr){
		return onErr.apply(this.error);
	}
	@Override
<R>Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other){
		return new Err<>(this.error);
	}
	@Override
<R>Result<T, Tuple<X, R>> or(Supplier<Result<T, R>> other){
		return other.get().mapErr(otherErr -> new Tuple<>(this.error, otherErr));
	}
	@Override
boolean isOk(){
		return false;
	}
	Optional<X> findError0(){
		return Optional.of(this.error);
	}
	@Override
Option<X> findError(){
		return findError0().<Option<X>>map(Some::new).orElseGet(None::new);
	}
}

