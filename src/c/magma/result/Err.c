import java.util.function.Function;
struct Err<T, X>(X error) implements Result<T, X> {@Override
    public <R> R match(Function<T, R> valueMapper, Function<X, R> errorMapper) {
        return errorMapper.apply(this.error);
    }@Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Err<>(this.error);
    }
}