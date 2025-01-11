import java.util.function.Function;public record Err<T, X>(X error) implements Result<T, X> {
    @Override
    public <R> R match(Function<T, R> valueMapper, Function<X, R> errorMapper) {
        return errorMapper.apply(this.error);
    }
}