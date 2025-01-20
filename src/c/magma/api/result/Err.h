import magma.api.Tuple;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Err<T, X>(X error) implements Result<T, X> {
	@Override
    public <R> Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper){return new Err<>(this.error);}
	@Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper){return new Err<>(this.error);}
	@Override
    public <R> Result<T, R> mapErr(Function<X, R> mapper){return new Err<>(mapper.apply(this.error));}
	@Override
    public <R> R match(Function<T, R> onOk,  Function<X, R> onErr){return onErr.apply(this.error);}
	@Override
    public <R> Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other){return new Err<>(this.error);}
	@Override
    public <R> Result<T, Tuple<X, R>> or(Supplier<Result<T, R>> other){return other.get().mapErr(otherErr -> new Tuple<>(this.error, otherErr));}
	@Override
    public Optional<T> findValue(){return Optional.empty();}
	@Override
    public boolean isOk(){return false;}
	@Override
    public Optional<X> findError(){return Optional.of(this.error);}
}