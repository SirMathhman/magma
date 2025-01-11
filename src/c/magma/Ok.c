import java.util.function.Function;public record Ok<T, X>(T value) implements Result<T, X> {
    @Override
    public <R> R match(Function<T, R> valueMapper, Function<X, R> errorMapper) {
        return valueMapper.apply(this.value);
    }
}