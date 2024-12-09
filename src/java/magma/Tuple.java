package magma;

import java.util.function.Function;

public record Tuple<A, B>(A left, B right) {
    public <R> Tuple<R,B> mapLeft(Function<A, R> mapper) {
        return new Tuple<>(mapper.apply(left), right);
    }
}
