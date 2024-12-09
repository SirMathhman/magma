package magma;

import java.util.function.Function;

public record Tuple<A, B>(A left, B right) {
    public <R> Tuple<A, R> mapRight(Function<B, R> mapper) {
        return new Tuple<>(left, mapper.apply(right));
    }
}
