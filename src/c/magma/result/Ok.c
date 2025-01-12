import java.util.function.Function;
struct Ok<T, X>(T value) implements Result<T, X> {@Override
    public <R> R match(Function<T, R> valueMapper, Function<X, R> errorMapper) {
        return valueMapper.apply(this.value);
    }@Override
    public <R> Result<R, X> mapValue(Function<T, R> mapper) {
        return new Ok<>(mapper.apply(this.value));
    }
}