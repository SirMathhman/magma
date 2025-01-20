import magma.api.Tuple;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
public struct Ok<T, X>(T value) implements Result<T, X> {
	@Override
    public <R> Result<R, X> flatMapValue(((T) => Result<R, X>) mapper){
		return mapper.apply(this.value);
	}
	@Override
    public <R> Result<R, X> mapValue(((T) => R) mapper){
		return new Ok<>(mapper.apply(this.value));
	}
	@Override
    public <R> Result<T, R> mapErr(((X) => R) mapper){
		return new Ok<>(this.value);
	}
	@Override
    public <R> R match(((T) => R) onOk,  ((X) => R) onErr){
		return onOk.apply(this.value);
	}
	@Override
    public <R> Result<Tuple<T, R>, X> and((() => Result<R, X>) other){
		return other.get().mapValue(otherValue -> new Tuple<>(this.value, otherValue));
	}
	@Override
    public <R> Result<T, Tuple<X, R>> or((() => Result<T, R>) other){
		return new Ok<>(this.value);
	}
	@Override
    public Optional<T> findValue(){
		return Optional.of(this.value);
	}
	@Override
    public boolean isOk(){
		return true;
	}
	@Override
    public Optional<X> findError(){
		return Optional.empty();
	}
}