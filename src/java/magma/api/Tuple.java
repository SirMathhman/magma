package magma.api;

import java.util.function.Function;

public record Tuple<A, B>(A left, B right) {
    public <R> Tuple<A, R> mapRight(Function<B, R> mapper) {
        final var newRight = mapper.apply(right);
        return new Tuple<>(left, newRight);
    }
}
