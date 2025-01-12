import java.util.function.Function;
struct Err<T, X>(X error) implements Result<T, X> {
	R match(Function<T, R> valueMapper, Function<X, R> errorMapper){
		return errorMapper.apply(this.error);
	}
	X> mapValue(Function<T, R> mapper){
		return Err<>(this.error);
	}
}