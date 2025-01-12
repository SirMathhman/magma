import java.util.function.Function;
struct Ok<T, X>(T value) implements Result<T, X> {
	R match(Function<T, R> valueMapper, Function<X, R> errorMapper){
		return valueMapper.apply(this.value);
	}
	X> mapValue(Function<T, R> mapper){
		return Ok<>(mapper.apply(this.value));
	}
}