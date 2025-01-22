import magma.api.Tuple;import magma.api.option.None;import magma.api.option.Option;import magma.api.option.Some;import java.util.Optional;struct Ok<T, X>(T value) implements Result<T, X>{
	<R>Result<R, X> flatMapValue(((T) => Result<R, X>) mapper);
	<R>Result<R, X> mapValue(((T) => R) mapper);
	<R>Result<T, R> mapErr(((X) => R) mapper);
	<R>R match(((T) => R) onOk, ((X) => R) onErr);
	<R>Result<Tuple<T, R>, X> and((() => Result<R, X>) other);
	<R>Result<T, Tuple<X, R>> or((() => Result<T, R>) other);
	boolean isOk();
	Optional<X> findError0();
	Option<X> findError();
}